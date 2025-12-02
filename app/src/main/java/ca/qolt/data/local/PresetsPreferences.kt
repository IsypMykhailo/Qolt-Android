package ca.qolt.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PresetsPreferences @Inject constructor(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val CURRENT_PRESET_ID = stringPreferencesKey("CURRENT_PRESET_ID")
    }

    suspend fun getCurrentPresetId(): String? = dataStore.data.map { it[CURRENT_PRESET_ID] }.firstOrNull()

    suspend fun setCurrentPresetId(presetId: String) = dataStore.edit { it[CURRENT_PRESET_ID] = presetId }

    suspend fun removeCurrentPresetId() = dataStore.edit{it.remove(CURRENT_PRESET_ID)}
}