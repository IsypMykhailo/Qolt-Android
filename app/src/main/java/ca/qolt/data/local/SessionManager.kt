package ca.qolt.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val prefs: DataStore<Preferences>
) {
    companion object {
        private val KEY_CURRENT_SESSION_ID = longPreferencesKey("current_session_id")
        private val KEY_LAST_SERVICE_CHECK = longPreferencesKey("last_service_check")
    }

    /**
     * Save the ID of the currently active session.
     */
    suspend fun saveCurrentSessionId(sessionId: Long) =
        prefs.edit { it[KEY_CURRENT_SESSION_ID] = sessionId }


    /**
     * Get the ID of the currently active session.
     * @return Session ID or null if no active session
     */
    suspend fun getCurrentSessionId(): Long? = prefs.data.map { it[KEY_CURRENT_SESSION_ID] }.firstOrNull()

    /**
     * Clear the current session ID.
     */
    suspend fun clearCurrentSessionId() =
        prefs.edit{it.remove(KEY_CURRENT_SESSION_ID)}


    /**
     * Save the timestamp of the last service health check.
     * This is used to estimate session duration if service is killed.
     */
    suspend fun saveLastServiceCheckTime(timestampMs: Long) =
        prefs.edit{it[KEY_LAST_SERVICE_CHECK] = timestampMs}


    /**
     * Get the timestamp of the last service health check.
     * @return Timestamp or null if never checked
     */
    suspend fun getLastServiceCheckTime(): Long? = prefs.data.map{it[KEY_LAST_SERVICE_CHECK]}.firstOrNull()
}
