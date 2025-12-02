package ca.qolt.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import ca.qolt.ui.navigation.Navigator
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(val navigator: Navigator) : ViewModel() {
    fun handleDeepLink(uri: Uri?) {
        uri?.let { navigator.navigateTo(uri) }
    }
}