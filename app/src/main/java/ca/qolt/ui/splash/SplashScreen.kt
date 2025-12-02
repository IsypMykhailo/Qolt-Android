package ca.qolt.ui.splash

import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
fun Splash(modifier: Modifier, viewModel: SplashViewModel) {
    BackHandler { viewModel.navigator.finish() }
    LaunchedEffect(Unit) {
        viewModel.navigateFromSplash()
    }
}