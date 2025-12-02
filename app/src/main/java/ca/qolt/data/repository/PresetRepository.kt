package ca.qolt.data.repository

import ca.qolt.data.local.PresetsPreferences
import ca.qolt.data.local.QoltDatabase
import ca.qolt.data.local.entity.PresetEntity
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class PresetRepository @Inject constructor(
    private val qoltDatabase: QoltDatabase,
    private val presetsPreferences: PresetsPreferences
) {
    fun getAllPresets(): Flow<List<PresetEntity>> = qoltDatabase.presetDao().getAllPresets().flowOn(IO)

    suspend fun getPresetById(presetId: String): PresetEntity? =
        qoltDatabase.presetDao().getPresetById(presetId)

    suspend fun insertPreset(preset: PresetEntity) =
         qoltDatabase.presetDao().insertPreset(preset)

    suspend fun updatePreset(preset: PresetEntity): Int =
            qoltDatabase.presetDao().updatePreset(preset)

    suspend fun deletePreset(preset: PresetEntity): Int {
        // Check if this is the current preset
        val currentId = getCurrentPresetId()
        if (currentId == preset.id) {
            // Clear current preset if it's being deleted
            removeCurrentPresetId()
        }
        return qoltDatabase.presetDao().deletePreset(preset)
    }

    suspend fun getCurrentPresetId(): String? = presetsPreferences.getCurrentPresetId()
    suspend fun setCurrentPresetId(presetId: String) = presetsPreferences.setCurrentPresetId(presetId)
    suspend fun removeCurrentPresetId() = presetsPreferences.removeCurrentPresetId()
}