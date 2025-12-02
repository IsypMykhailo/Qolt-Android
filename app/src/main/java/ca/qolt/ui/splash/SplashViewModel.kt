package ca.qolt.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ca.qolt.data.repository.SettingsRepository
import ca.qolt.ui.navigation.Destination
import ca.qolt.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val navigator: Navigator,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    companion object {
        const val TAG = "SplashViewModel"
    }

    fun navigateFromSplash() {
        viewModelScope.launch {
            val isLoggedIn = settingsRepository.isLoggedIn()
            val route = if (isLoggedIn) {
                Destination.Main.route
            } else {
                Destination.Onboarding.route
            }
            navigator.navigateTo(route, popBackstack = true)
        }
    }

    fun onBackPressed() {
        navigator.finish()
    }
}
