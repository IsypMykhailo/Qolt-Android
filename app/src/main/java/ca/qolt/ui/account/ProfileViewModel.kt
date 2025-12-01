package ca.qolt.ui.account

import android.content.Context
import androidx.lifecycle.ViewModel
import ca.qolt.ui.navigation.Destination
import ca.qolt.ui.navigation.Navigator
import ca.qolt.util.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    @ApplicationContext
    val context: Context,
    val navigator: Navigator
): ViewModel() {
    companion object {
        const val TAG = "ProfileViewModel"
    }

    fun onLogout() {
        PreferencesManager.clearLoginState(context)
        navigator.navigateTo(Destination.Onboarding.route, popBackstack = true)
    }
}