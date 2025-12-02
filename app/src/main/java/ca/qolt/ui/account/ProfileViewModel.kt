package ca.qolt.ui.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.qolt.data.local.SessionManager
import ca.qolt.data.repository.SettingsRepository
import ca.qolt.ui.navigation.Destination
import ca.qolt.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val name: String = "",
    val email: String = "",
    val profileImageUri: String? = null,

    val blockTimer: Boolean = true,
    val emergencyUnlock: Boolean = false,
    val darkMode: Boolean = false,
    val liveActivity: Boolean = true,
    val notifications: Boolean = true,
    val language: String = "English",

    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val settingsRepository: SettingsRepository,
    private val navigator: Navigator
) : ViewModel() {

    companion object {
        const val TAG = "ProfileViewModel"
    }

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            val name = settingsRepository.getProfileName()
            val email = settingsRepository.getProfileEmail()
            val imageUri = settingsRepository.getProfileImageUri()

            val blockTimer = settingsRepository.getBlockTimerEnabled()
            val emergencyUnlock = settingsRepository.getEmergencyUnlockEnabled()
            val darkMode = settingsRepository.getDarkModeEnabled()
            val liveActivity = settingsRepository.getLiveActivityEnabled()
            val notifications = settingsRepository.getNotificationsEnabled()
            val language = settingsRepository.getLanguage()

            _uiState.value = ProfileUiState(
                name = name,
                email = email,
                profileImageUri = imageUri,
                blockTimer = blockTimer,
                emergencyUnlock = emergencyUnlock,
                darkMode = darkMode,
                liveActivity = liveActivity,
                notifications = notifications,
                language = language,
                isLoading = false
            )
        }
    }

    // ---------------- Navigation ----------------

    fun onHelpCenterClick() {
        navigator.navigateTo(Destination.Main.Profile.HelpCenter.route)
    }

    fun onLogout() {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.clearLoginState()

            navigator.navigateTo(
                Destination.Onboarding.route,
                popBackstack = true
            )
        }
    }


    fun onBlockTimerChanged(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setBlockTimerEnabled(enabled)
            _uiState.update { it.copy(blockTimer = enabled) }
        }
    }

    fun onEmergencyUnlockChanged(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setEmergencyUnlockEnabled(enabled)
            _uiState.update { it.copy(emergencyUnlock = enabled) }
        }
    }

    fun onDarkModeChanged(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setDarkModeEnabled(enabled)
            _uiState.update { it.copy(darkMode = enabled) }
        }
    }

    fun onLiveActivityChanged(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setLiveActivityEnabled(enabled)
            _uiState.update { it.copy(liveActivity = enabled) }
        }
    }

    fun onNotificationsChanged(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setNotificationsEnabled(enabled)
            _uiState.update { it.copy(notifications = enabled) }
        }
    }

    fun onLanguageSelected(language: String) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setLanguage(language)
            _uiState.update { it.copy(language = language) }
        }
    }


    fun onProfileSaved(
        name: String,
        email: String,
        imageUri: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            settingsRepository.setProfileName(name)
            settingsRepository.setProfileEmail(email)
            settingsRepository.setProfileImageUri(imageUri)

            _uiState.update {
                it.copy(
                    name = name,
                    email = email,
                    profileImageUri = imageUri
                )
            }
        }
    }
}