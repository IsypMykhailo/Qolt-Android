package ca.qolt.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import ca.qolt.data.repository.AppBlockingRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import timber.log.Timber

class AppBlockingRestartReceiver : BroadcastReceiver() {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface AppBlockingRestartReceiverEntryPoint {
        fun appBlockingRepository(): AppBlockingRepository
    }

    companion object {
        private const val TAG = "AppBlockingRestart"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Timber.tag(TAG).d("Received broadcast: ${intent.action}")

        val appContext = context.applicationContext
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            appContext,
            AppBlockingRestartReceiverEntryPoint::class.java
        )
        val repository = hiltEntryPoint.appBlockingRepository()

        // Check if blocking should be active
        val isActive = runBlocking { repository.isBlockingActive() }
        if (!isActive) {
            Timber.tag(TAG).d("Blocking not active - ignoring broadcast")
            return
        }

        val blockedApps = runBlocking { repository.getBlockedApps() }
        if (blockedApps.isEmpty()) {
            Timber.tag(TAG).w("Blocking active but no apps configured")
            return
        }

        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED,
            Intent.ACTION_MY_PACKAGE_REPLACED -> {
                Timber.tag(TAG).i("Restarting app blocking service")
                startAppBlockingService(context, blockedApps)
            }
        }
    }

    private fun startAppBlockingService(context: Context, blockedApps: Set<String>) {
        val serviceIntent = Intent(context, AppBlockingService::class.java).apply {
            putStringArrayListExtra("blocked_apps", ArrayList(blockedApps))
        }

        try {
            context.startForegroundService(serviceIntent)
            Timber.tag(TAG).d("Service started successfully")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Failed to start service")
        }
    }
}
