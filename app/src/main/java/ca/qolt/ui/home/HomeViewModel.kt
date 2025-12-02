package ca.qolt.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.qolt.data.local.entity.PresetEntity
import ca.qolt.data.repository.AppBlockingRepository
import ca.qolt.data.repository.PresetRepository
import ca.qolt.data.repository.SettingsRepository
import ca.qolt.data.repository.UsageSessionRepository
import ca.qolt.domain.SessionTrackingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val usageSessionRepository: UsageSessionRepository,
    private val sessionTrackingManager: SessionTrackingManager,
    private val settingsRepository: SettingsRepository,
    private val presetRepository: PresetRepository,
    private val appBlockingRepository: AppBlockingRepository
) : ViewModel() {

    companion object {
        const val TAG = "HomeViewModel"
    }

    private val _currentStreak = MutableStateFlow(0)
    val currentStreak: StateFlow<Int> = _currentStreak.asStateFlow()
    private val _emergencyUnlockEnabled = MutableStateFlow(false)
    val emergencyUnlockEnabled: StateFlow<Boolean> = _emergencyUnlockEnabled

    private val _currentPreset = MutableStateFlow<PresetEntity?>(null)
    val currentPreset: StateFlow<PresetEntity?> = _currentPreset.asStateFlow()

    private val _isBlockingActive = MutableStateFlow(false)
    val isBlockingActive: StateFlow<Boolean> = _isBlockingActive.asStateFlow()

    init {
        refreshStreak()
        loadCurrentPreset()
        observeBlockingState()
        viewModelScope.launch {
            _emergencyUnlockEnabled.value = settingsRepository.getEmergencyUnlockEnabled()
        }
    }

    /**
     * Refresh the current streak value from the database.
     */
    fun refreshStreak() {
        viewModelScope.launch {
            try {
                val streak = usageSessionRepository.calculateStreak()
                _currentStreak.value = streak
                Timber.tag(TAG).d("Refreshed streak: $streak days")
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error refreshing streak")
                _currentStreak.value = 0
            }
        }
    }

    /**
     * End the current session (called when user unblocks via NFC or emergency).
     * Also refreshes the streak after ending the session.
     */
    suspend fun endSession() {
        try {
            sessionTrackingManager.endCurrentSession()
            refreshStreak()
            Timber.tag(TAG).d("Session ended and streak refreshed")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error ending session")
        }
    }

    /**
     * Load the current preset from the repository.
     */
    private fun loadCurrentPreset() {
        viewModelScope.launch {
            try {
                val presetId = presetRepository.getCurrentPresetId()
                if (presetId != null) {
                    val preset = presetRepository.getPresetById(presetId)
                    _currentPreset.value = preset
                    Timber.tag(TAG).d("Loaded current preset: ${preset?.name}")
                } else {
                    _currentPreset.value = null
                    Timber.tag(TAG).d("No current preset selected")
                }
            } catch (e: Exception) {
                Timber.tag(TAG).e(e, "Error loading current preset")
                _currentPreset.value = null
            }
        }
    }

    /**
     * Observe the blocking state from the repository.
     */
    private fun observeBlockingState() {
        viewModelScope.launch {
            appBlockingRepository.isBlockingActiveFlow().collect { isActive ->
                _isBlockingActive.value = isActive
            }
        }
    }

    /**
     * Start blocking apps from the current preset.
     * Returns Result.success() if blocking started successfully,
     * or Result.failure() with error message if something went wrong.
     */
    suspend fun startBlockingCurrentPreset(): Result<Unit> {
        return try {
            val preset = _currentPreset.value
                ?: return Result.failure(Exception("No preset selected"))

            if (preset.blockedApps.isEmpty()) {
                return Result.failure(Exception("Preset has no apps to block"))
            }

            // Check permissions
            if (!appBlockingRepository.hasUsageStatsPermission()) {
                return Result.failure(Exception("USAGE_STATS_PERMISSION_REQUIRED"))
            }

            if (!appBlockingRepository.hasOverlayPermission()) {
                return Result.failure(Exception("OVERLAY_PERMISSION_REQUIRED"))
            }

            // Start blocking
            appBlockingRepository.startBlocking(preset.blockedApps.toSet())

            Timber.tag(TAG).d("Started blocking ${preset.blockedApps.size} apps from preset: ${preset.name}")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error starting blocking")
            Result.failure(e)
        }
    }

    /**
     * Stop blocking and end the current session.
     */
    suspend fun stopBlocking() {
        try {
            appBlockingRepository.stopBlocking()
            endSession()
            Timber.tag(TAG).d("Stopped blocking")
        } catch (e: Exception) {
            Timber.tag(TAG).e(e, "Error stopping blocking")
        }
    }

    /**
     * Refresh the current preset (e.g., after returning from Presets page).
     */
    fun refreshCurrentPreset() {
        loadCurrentPreset()
    }

    /**
     * Request usage stats permission.
     */
    fun requestUsageStatsPermission() {
        appBlockingRepository.requestUsageStatsPermission()
    }

    /**
     * Request overlay permission.
     */
    fun requestOverlayPermission() {
        appBlockingRepository.requestOverlayPermission()
    }
}
