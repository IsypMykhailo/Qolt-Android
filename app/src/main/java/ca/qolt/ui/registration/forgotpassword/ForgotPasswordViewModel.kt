package ca.qolt.ui.registration.forgotpassword

import androidx.lifecycle.ViewModel
import ca.qolt.ui.navigation.Destination
import ca.qolt.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    val navigator: Navigator
): ViewModel() {
    companion object {
        const val TAG = "ForgotPasswordViewModel"
    }

    fun onBack() {
        navigator.navigateBack()
    }
    fun onLoginClick() {
        navigator.navigateTo(Destination.Login.route)
    }
}