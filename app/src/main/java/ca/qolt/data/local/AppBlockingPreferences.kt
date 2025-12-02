package ca.qolt.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppBlockingPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val BLOCKED_APPS_KEY = stringSetPreferencesKey("blocked_apps")
        private val BLOCKING_ACTIVE_KEY = booleanPreferencesKey("blocking_active")
    }

    suspend fun getBlockedApps(): Set<String> {
        return dataStore.data
            .map { prefs -> prefs[BLOCKED_APPS_KEY] ?: emptySet() }
            .first()
    }

    suspend fun saveBlockedApps(packageNames: Set<String>) {
        dataStore.edit { prefs ->
            prefs[BLOCKED_APPS_KEY] = packageNames
        }
    }

    suspend fun isBlockingActive(): Boolean {
        return dataStore.data
            .map { prefs -> prefs[BLOCKING_ACTIVE_KEY] ?: false }
            .first()
    }

    suspend fun setBlockingActive(active: Boolean) {
        dataStore.edit { prefs ->
            prefs[BLOCKING_ACTIVE_KEY] = active
        }
    }

    fun isBlockingActiveFlow(): Flow<Boolean> {
        return dataStore.data.map { prefs ->
            prefs[BLOCKING_ACTIVE_KEY] ?: false
        }
    }
}
