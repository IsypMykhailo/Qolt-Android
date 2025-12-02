package ca.qolt.ui.splash

import android.content.Context
import androidx.lifecycle.ViewModel
import ca.qolt.ui.navigation.Destination
import ca.qolt.ui.navigation.Navigator
import ca.qolt.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    val navigator: Navigator,
    @ApplicationContext
    val context: Context
) : ViewModel() {
    companion object {
        const val TAG = "SplashViewModel"
    }

    fun navigateFromSplash() {
        val route = if (PreferencesManager.isLoggedIn(context)) {
            Destination.Main.route
        } else {
            Destination.Onboarding.route
        }
        navigator.navigateTo(route, true)
    }
}