package ca.qolt.ui.registration.login

import android.content.Context
import androidx.lifecycle.ViewModel
import ca.qolt.ui.navigation.Destination
import ca.qolt.ui.navigation.Navigator
import ca.qolt.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    val navigator: Navigator,
    @ApplicationContext
    val context: Context
): ViewModel() {
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
        PreferencesManager.setLoggedIn(context, true)
        navigator.navigateTo(Destination.Main.route, popBackstack = true)
    }
}