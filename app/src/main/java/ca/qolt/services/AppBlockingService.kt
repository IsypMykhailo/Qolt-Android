package ca.qolt.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import ca.qolt.R
import ca.qolt.data.local.SessionManager
import ca.qolt.data.repository.UsageSessionRepository
import ca.qolt.data.repository.SettingsRepository
import ca.qolt.ui.MainActivity
import ca.qolt.ui.home.BlockingOverlay
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class AppBlockingService : Service() {

    @Inject
    lateinit var usageSessionRepository: UsageSessionRepository

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var blockedApps: Set<String> = emptySet()
    private var monitoringJob: Job? = null
    private var timerJob: Job? = null
    private var lastBlockedApp: String? = null
    private var lastBlockTime: Long = 0
    private var blockingOverlay: BlockingOverlay? = null
    private var timerStartTime: Long = 0L
    @Volatile
    private var blockTimerEnabled: Boolean = false

    @Volatile
    private var liveActivityEnabled: Boolean = true

    companion object {
        private const val TAG = "AppBlockingService"
        private const val NOTIFICATION_CHANNEL_ID = "app_blocking_channel"
        private const val NOTIFICATION_ID = 1001
        private const val BLOCK_COOLDOWN_MS = 2000L
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        blockingOverlay = BlockingOverlay(this)

        // Close any orphaned sessions from previous crashes
        serviceScope.launch {
            usageSessionRepository.closeAnyOrphanedSessions()
        }

        serviceScope.launch {
            try {
                blockTimerEnabled = settingsRepository.getBlockTimerEnabled()
                liveActivityEnabled = settingsRepository.getLiveActivityEnabled()
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Failed to load settings from DataStore")
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Try to get apps from intent first (normal startup)
        val appsFromIntent = intent?.getStringArrayListExtra("blocked_apps")?.toSet()

        // If intent is null or empty, restore from SharedPreferences (service restart)
        blockedApps = if (!appsFromIntent.isNullOrEmpty()) {
            Timber.tag(TAG).d("Service started with intent: ${appsFromIntent.size} apps")
            appsFromIntent
        } else {
            val restored = AppBlockingManager.getBlockedApps(this)
            Timber.tag(TAG).d("Service restarted - restored ${restored.size} apps from preferences")
            restored
        }

        // Verify we should still be running
        if (!AppBlockingManager.isBlockingActive(this)) {
            Timber.tag(TAG).w("Service started but blocking not active - stopping")
            stopSelf()
            return START_NOT_STICKY
        }

        // If no apps to block, stop the service
        if (blockedApps.isEmpty()) {
            Timber.tag(TAG).w("No apps to block - stopping service")
            stopSelf()
            return START_NOT_STICKY
        }

        if (blockTimerEnabled) {
            timerStartTime = System.currentTimeMillis()
        }
        val initialTimerText = if (blockTimerEnabled) "00:00:00" else null
        val notification = createNotification(initialTimerText)
        startForeground(NOTIFICATION_ID, notification)

        startMonitoring()

        // Start or continue tracking session
        serviceScope.launch {
            val activeSession = usageSessionRepository.getActiveSession()
            if (activeSession == null) {
                val sessionId = usageSessionRepository.startSession(blockedApps.size)
                sessionManager.saveCurrentSessionId(sessionId)
                Timber.tag(TAG).d("Started new session: $sessionId")
            } else {
                Timber.tag(TAG).d("Continuing existing session: ${activeSession.id}")
                sessionManager.saveCurrentSessionId(activeSession.id)
            }
        }

        if (blockTimerEnabled) {
            startTimerUpdates()
        }

        return START_STICKY
    }

    private fun startTimerUpdates() {
        timerJob?.cancel()
        timerJob = serviceScope.launch {
            while (isActive && blockTimerEnabled) {
                val elapsedMillis = System.currentTimeMillis() - timerStartTime
                val totalSeconds = (elapsedMillis / 1000).toInt()

                val hours = totalSeconds / 3600
                val minutes = (totalSeconds % 3600) / 60
                val seconds = totalSeconds % 60

                val timerText = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                val updatedNotification = createNotification(timerText)
                val manager = getSystemService(NotificationManager::class.java)
                manager.notify(NOTIFICATION_ID, updatedNotification)

                delay(1000)
            }
        }
    }

    private fun startMonitoring() {
        monitoringJob?.cancel()
        monitoringJob = serviceScope.launch {
            while (isActive) {
                checkAndBlockApps()
                delay(200)
            }
        }
    }

    private suspend fun checkAndBlockApps() {
        if (!AppBlockingManager.isBlockingActive(this)) {
            stopSelf()
            return
        }

        try {
            val usageStatsManager = getSystemService(USAGE_STATS_SERVICE) as UsageStatsManager
            val currentTime = System.currentTimeMillis()

            val events = usageStatsManager.queryEvents(currentTime - 1000, currentTime)
            val currentEvent = UsageEvents.Event()
            var foregroundPackage = ""

            while (events.hasNextEvent()) {
                events.getNextEvent(currentEvent)
                if (currentEvent.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                    foregroundPackage = currentEvent.packageName
                }
            }

            if (foregroundPackage.isNotEmpty()) {
                Timber.tag(TAG).d("Detected foreground app: $foregroundPackage")
            }

            if (foregroundPackage.isNotEmpty() &&
                foregroundPackage in blockedApps &&
                foregroundPackage != packageName
            ) {

                Timber.tag(TAG).d("Blocked app detected: $foregroundPackage")

                val shouldBlock =
                    (foregroundPackage != lastBlockedApp) ||
                            (currentTime - lastBlockTime > BLOCK_COOLDOWN_MS)

                if (shouldBlock) {
                    lastBlockedApp = foregroundPackage
                    lastBlockTime = currentTime

                    Timber.tag(TAG).d("Showing blocking overlay for: $foregroundPackage")

                    withContext(Dispatchers.Main) {
                        blockingOverlay?.show(foregroundPackage)
                    }
                } else {
                    Timber.tag(TAG).d("Blocked app detected but cooldown active")
                }
            } else if (foregroundPackage.isNotEmpty() && foregroundPackage !in blockedApps) {
                withContext(Dispatchers.Main) {
                    blockingOverlay?.dismiss()
                }

                lastBlockedApp = null
                lastBlockTime = 0
            }

            // Update last check timestamp for orphaned session recovery
            sessionManager.saveLastServiceCheckTime(currentTime)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error checking foreground app")
        }
    }

    private fun getAppName(packageName: String): String {
        return try {
            val pm = packageManager
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (_: PackageManager.NameNotFoundException) {
            packageName.substringAfterLast(".")
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "App Blocking",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Monitors and blocks selected apps"
        }

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)
    }

    private fun createNotification(timerText: String?): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationText = if (blockedApps.isEmpty()) {
            "No apps are currently blocked"
        } else {
            "${blockedApps.size} apps blocked: " +
                    blockedApps.take(3).joinToString(", ") { getAppName(it) } +
                    if (blockedApps.size > 3) "..." else ""
        }

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Qolt App Blocking Active")
            .setContentText(notificationText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .setVisibility(
                if (liveActivityEnabled)
                    NotificationCompat.VISIBILITY_PUBLIC
                else
                    NotificationCompat.VISIBILITY_SECRET
            )

        if (timerText != null) {
            builder.setSubText(timerText)
        }

        return builder.build()
    }

    override fun onDestroy() {
        // End current session before stopping service
        serviceScope.launch {
            val sessionId = sessionManager.getCurrentSessionId()
            if (sessionId != null) {
                usageSessionRepository.endSession(sessionId)
                sessionManager.clearCurrentSessionId()
                Timber.tag(TAG).d("Ended session: $sessionId")
            }
        }.invokeOnCompletion {
            monitoringJob?.cancel()
            timerJob?.cancel()
            serviceScope.cancel()
            blockingOverlay?.dismiss()
            blockingOverlay = null
        }
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
