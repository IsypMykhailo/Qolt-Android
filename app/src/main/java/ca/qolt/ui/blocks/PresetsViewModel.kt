package ca.qolt.ui.blocks

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.qolt.data.local.entity.PresetEntity
import ca.qolt.data.repository.PresetRepository
import ca.qolt.model.InstalledApp
import ca.qolt.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PresetsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    val navigator: Navigator,
    private val presetRepository: PresetRepository
) : ViewModel() {
    companion object {
        const val TAG = "PresetsViewModel"
    }

    private val _allApps = MutableStateFlow<List<InstalledApp>>(emptyList())
    val allApps: StateFlow<List<InstalledApp>> = _allApps.asStateFlow()

    private val _presets = MutableStateFlow<List<PresetEntity>>(emptyList())
    val presets: StateFlow<List<PresetEntity>> = _presets.asStateFlow()

    private val _currentPresetId = MutableStateFlow<String?>(null)
    val currentPresetId: StateFlow<String?> = _currentPresetId.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val apps = loadInstalledApps(context)
            _allApps.value = apps
        }

        viewModelScope.launch {
            presetRepository.getAllPresets().collect { presetsList ->
                _presets.value = presetsList
            }
        }

        viewModelScope.launch {
            val presetId = presetRepository.getCurrentPresetId()
            _currentPresetId.value = presetId
        }
    }

    fun savePreset(preset: PresetEntity, isNew: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            if (isNew) {
                presetRepository.insertPreset(preset)
            } else {
                presetRepository.updatePreset(preset)
            }
        }
    }

    fun deletePreset(preset: PresetEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            presetRepository.deletePreset(preset)
        }
    }

    fun setCurrentPresetId(presetId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            presetRepository.setCurrentPresetId(presetId)
            _currentPresetId.value = presetId
        }
    }

    private fun loadInstalledApps(context: Context): List<InstalledApp> {
        val pm = context.packageManager
        val ownPackageName = context.packageName // Get Qolt's package name

        return pm.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { appInfo ->
                // Exclude Qolt app itself and only include apps with launch intent
                pm.getLaunchIntentForPackage(appInfo.packageName) != null &&
                        appInfo.packageName != ownPackageName
            }
            .map { appInfo ->
                InstalledApp(
                    packageName = appInfo.packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    icon = pm.getApplicationIcon(appInfo),
                    category = appInfo.category
                )
            }
            .sortedBy { it.appName.lowercase() }
    }
}