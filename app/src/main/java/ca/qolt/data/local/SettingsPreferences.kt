package ca.qolt.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val KEY_IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val KEY_BLOCK_TIMER = booleanPreferencesKey("block_timer")
        private val KEY_EMERGENCY_UNLOCK = booleanPreferencesKey("emergency_unlock")
        private val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        private val KEY_LIVE_ACTIVITY = booleanPreferencesKey("live_activity")
        private val KEY_NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        private val KEY_LANGUAGE = stringPreferencesKey("language")

        private val KEY_PROFILE_NAME = stringPreferencesKey("profile_name")
        private val KEY_PROFILE_EMAIL = stringPreferencesKey("profile_email")
        private val KEY_PROFILE_IMAGE_URI = stringPreferencesKey("profile_image_uri")
    }

    // -----------------------------------------------------------
    // Login State
    // -----------------------------------------------------------

    suspend fun isLoggedIn(): Boolean =
        dataStore.data
            .map { prefs -> prefs[KEY_IS_LOGGED_IN] ?: false }
            .first()

    suspend fun setLoggedIn(value: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = value
        }
    }

    suspend fun clearLoginState() {
        dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    // -----------------------------------------------------------
    // Settings
    // -----------------------------------------------------------

    suspend fun getBlockTimerEnabled(): Boolean =
        dataStore.data
            .map { prefs -> prefs[KEY_BLOCK_TIMER] ?: true }
            .first()

    suspend fun setBlockTimerEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_BLOCK_TIMER] = enabled
        }
    }

    suspend fun getEmergencyUnlockEnabled(): Boolean =
        dataStore.data
            .map { prefs -> prefs[KEY_EMERGENCY_UNLOCK] ?: false }
            .first()

    suspend fun setEmergencyUnlockEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_EMERGENCY_UNLOCK] = enabled
        }
    }

    suspend fun getDarkModeEnabled(): Boolean =
        dataStore.data
            .map { prefs -> prefs[KEY_DARK_MODE] ?: false }
            .first()

    suspend fun setDarkModeEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_DARK_MODE] = enabled
        }
    }

    suspend fun getLiveActivityEnabled(): Boolean =
        dataStore.data
            .map { prefs -> prefs[KEY_LIVE_ACTIVITY] ?: true }
            .first()

    suspend fun setLiveActivityEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_LIVE_ACTIVITY] = enabled
        }
    }

    suspend fun getNotificationsEnabled(): Boolean =
        dataStore.data
            .map { prefs -> prefs[KEY_NOTIFICATIONS_ENABLED] ?: true }
            .first()

    suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[KEY_NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun getLanguage(): String =
        dataStore.data
            .map { prefs -> prefs[KEY_LANGUAGE] ?: "English" }
            .first()

    suspend fun setLanguage(language: String) {
        dataStore.edit { prefs ->
            prefs[KEY_LANGUAGE] = language
        }
    }

    // -----------------------------------------------------------
    // Profile Data
    // -----------------------------------------------------------

    suspend fun getProfileName(): String =
        dataStore.data
            .map { prefs -> prefs[KEY_PROFILE_NAME] ?: "Franklin Au" }
            .first()

    suspend fun setProfileName(name: String) {
        dataStore.edit { prefs ->
            prefs[KEY_PROFILE_NAME] = name
        }
    }

    suspend fun getProfileEmail(): String =
        dataStore.data
            .map { prefs -> prefs[KEY_PROFILE_EMAIL] ?: "faa30@sfu.ca" }
            .first()

    suspend fun setProfileEmail(email: String) {
        dataStore.edit { prefs ->
            prefs[KEY_PROFILE_EMAIL] = email
        }
    }

    suspend fun getProfileImageUri(): String? =
        dataStore.data
            .map { prefs -> prefs[KEY_PROFILE_IMAGE_URI] }
            .first()

    suspend fun setProfileImageUri(uri: String?) {
        dataStore.edit { prefs ->
            if (uri == null) {
                prefs.remove(KEY_PROFILE_IMAGE_URI)
            } else {
                prefs[KEY_PROFILE_IMAGE_URI] = uri
            }
        }
    }
}
