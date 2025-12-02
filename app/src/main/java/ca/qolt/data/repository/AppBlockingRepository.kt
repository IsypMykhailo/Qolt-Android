package ca.qolt.data.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import ca.qolt.data.local.AppBlockingPreferences
import ca.qolt.services.AppBlockingManager
import ca.qolt.services.AppBlockingService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBlockingRepository @Inject constructor(
    private val appBlockingPreferences: AppBlockingPreferences,
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val TAG = "AppBlockingRepository"
        private const val MIGRATION_PREFS_NAME = "migration_prefs"
        private const val MIGRATION_COMPLETE_KEY = "app_blocking_migration_complete"
        private const val OLD_PREFS_NAME = "app_blocking_prefs"
        private const val OLD_BLOCKED_APPS_KEY = "blocked_apps"
        private const val OLD_BLOCKING_ACTIVE_KEY = "blocking_active"
    }

    private val migrationPrefs: SharedPreferences =
        context.getSharedPreferences(MIGRATION_PREFS_NAME, Context.MODE_PRIVATE)

    init {
        // One-time migration from SharedPreferences to DataStore
        CoroutineScope(Dispatchers.IO).launch {
            if (!migrationPrefs.getBoolean(MIGRATION_COMPLETE_KEY, false)) {
                migrateFromSharedPreferences()
            }
        }
    }

    private suspend fun migrateFromSharedPreferences() {
        try {
            val oldPrefs = context.getSharedPreferences(OLD_PREFS_NAME, Context.MODE_PRIVATE)
            val blockedApps = oldPrefs.getStringSet(OLD_BLOCKED_APPS_KEY, emptySet()) ?: emptySet()
            val blockingActive = oldPrefs.getBoolean(OLD_BLOCKING_ACTIVE_KEY, false)

            if (blockedApps.isNotEmpty()) {
                appBlockingPreferences.saveBlockedApps(blockedApps)
                Timber.tag(TAG).d("Migrated ${blockedApps.size} blocked apps from SharedPreferences")
            }

            if (blockingActive) {
                appBlockingPreferences.setBlockingActive(blockingActive)
                Timber.tag(TAG).d("Migrated blocking active state: $blockingActive")
            }

            // Mark migration complete
            migrationPrefs.edit().putBoolean(MIGRATION_COMPLETE_KEY, true).apply()

            Timber.tag(TAG).i("Migration complete: ${blockedApps.size} apps migrated")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Migration failed")
        }
    }

    // State management
    suspend fun getBlockedApps(): Set<String> =
        appBlockingPreferences.getBlockedApps()

    suspend fun saveBlockedApps(packageNames: Set<String>) =
        appBlockingPreferences.saveBlockedApps(packageNames)

    suspend fun isBlockingActive(): Boolean =
        appBlockingPreferences.isBlockingActive()

    fun isBlockingActiveFlow(): Flow<Boolean> =
        appBlockingPreferences.isBlockingActiveFlow()

    private suspend fun setBlockingActive(active: Boolean) =
        appBlockingPreferences.setBlockingActive(active)

    // Service control methods
    suspend fun startBlocking(packageNames: Set<String>) {
        require(packageNames.isNotEmpty()) { "Cannot block empty app list" }

        saveBlockedApps(packageNames)
        setBlockingActive(true)

        val intent = Intent(context, AppBlockingService::class.java)
        intent.putStringArrayListExtra("blocked_apps", ArrayList(packageNames))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }

        Timber.tag(TAG).d("Started blocking ${packageNames.size} apps")
    }

    suspend fun stopBlocking() {
        setBlockingActive(false)
        val intent = Intent(context, AppBlockingService::class.java)
        context.stopService(intent)

        Timber.tag(TAG).d("Stopped blocking")
    }

    // Permission checks (delegate to utility methods)
    fun hasUsageStatsPermission(): Boolean =
        AppBlockingManager.hasUsageStatsPermission(context)

    fun hasOverlayPermission(): Boolean =
        AppBlockingManager.hasOverlayPermission(context)

    fun requestUsageStatsPermission() =
        AppBlockingManager.requestUsageStatsPermission(context)

    fun requestOverlayPermission() =
        AppBlockingManager.requestOverlayPermission(context)
}
