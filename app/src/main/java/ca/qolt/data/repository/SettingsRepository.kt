package ca.qolt.data.repository

import ca.qolt.data.local.SettingsPreferences
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val settingsPreferences: SettingsPreferences
) {

    // -----------------------------------------------------------
    // Login State
    // -----------------------------------------------------------

    suspend fun isLoggedIn(): Boolean =
        settingsPreferences.isLoggedIn()

    suspend fun setLoggedIn(value: Boolean) =
        settingsPreferences.setLoggedIn(value)

    suspend fun clearLoginState() =
        settingsPreferences.clearLoginState()

    // -----------------------------------------------------------
    // Settings
    // -----------------------------------------------------------

    suspend fun getBlockTimerEnabled(): Boolean =
        settingsPreferences.getBlockTimerEnabled()

    suspend fun setBlockTimerEnabled(enabled: Boolean) =
        settingsPreferences.setBlockTimerEnabled(enabled)

    suspend fun getEmergencyUnlockEnabled(): Boolean =
        settingsPreferences.getEmergencyUnlockEnabled()

    suspend fun setEmergencyUnlockEnabled(enabled: Boolean) =
        settingsPreferences.setEmergencyUnlockEnabled(enabled)

    suspend fun getDarkModeEnabled(): Boolean =
        settingsPreferences.getDarkModeEnabled()

    suspend fun setDarkModeEnabled(enabled: Boolean) =
        settingsPreferences.setDarkModeEnabled(enabled)

    suspend fun getLiveActivityEnabled(): Boolean =
        settingsPreferences.getLiveActivityEnabled()

    suspend fun setLiveActivityEnabled(enabled: Boolean) =
        settingsPreferences.setLiveActivityEnabled(enabled)

    suspend fun getNotificationsEnabled(): Boolean =
        settingsPreferences.getNotificationsEnabled()

    suspend fun setNotificationsEnabled(enabled: Boolean) =
        settingsPreferences.setNotificationsEnabled(enabled)

    suspend fun getLanguage(): String =
        settingsPreferences.getLanguage()

    suspend fun setLanguage(language: String) =
        settingsPreferences.setLanguage(language)

    // -----------------------------------------------------------
    // Profile Data
    // -----------------------------------------------------------

    suspend fun getProfileName(): String =
        settingsPreferences.getProfileName()

    suspend fun setProfileName(name: String) =
        settingsPreferences.setProfileName(name)

    suspend fun getProfileEmail(): String =
        settingsPreferences.getProfileEmail()

    suspend fun setProfileEmail(email: String) =
        settingsPreferences.setProfileEmail(email)

    suspend fun getProfileImageUri(): String? =
        settingsPreferences.getProfileImageUri()

    suspend fun setProfileImageUri(uri: String?) =
        settingsPreferences.setProfileImageUri(uri)
}
