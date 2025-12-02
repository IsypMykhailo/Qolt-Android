package ca.qolt.ui.registration.qolttag

import androidx.lifecycle.ViewModel
import ca.qolt.ui.navigation.Destination
import ca.qolt.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class QoltTagViewModel @Inject constructor(
    val navigator: Navigator
) : ViewModel() {
    companion object {
        const val TAG = "QoltTagViewModel"
    }

    fun onHaveTag() {
        navigator.navigateTo(Destination.Login.route)
    }

    fun onNoTag() {
        navigator.navigateTo(Destination.Login.route)
    }
}