package ca.qolt.ui.registration.onboarding

import androidx.lifecycle.ViewModel
import ca.qolt.ui.navigation.Destination
import ca.qolt.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor (
    val navigator: Navigator,
) : ViewModel() {
    companion object {
        const val TAG = "OnboardingViewModel"
    }

    fun onFinished() {
        navigator.navigateTo(Destination.QoltTag.route)
    }
}