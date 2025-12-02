package ca.qolt.ui.registration.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.qolt.data.repository.SettingsRepository
import ca.qolt.ui.navigation.Destination
import ca.qolt.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val navigator: Navigator,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    companion object {
        const val TAG = "LoginViewModel"
    }

    fun onBack() {
        navigator.navigateBack()
    }

    fun onCreateAccount() {
        navigator.navigateTo(Destination.CreateAccount.route)
    }

    fun onForgotPassword() {
        navigator.navigateTo(Destination.ForgotPassword.route)
    }

    fun onLogin() {
        viewModelScope.launch {
            settingsRepository.setLoggedIn(true)
            navigator.navigateTo(Destination.Main.route, popBackstack = true)
        }
    }
}
