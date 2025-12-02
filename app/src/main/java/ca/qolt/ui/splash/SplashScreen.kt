package ca.qolt.ui.splash

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier

@Composable
fun Splash(
    modifier: Modifier = Modifier,
    viewModel: SplashViewModel
) {
    BackHandler { viewModel.onBackPressed() }

    LaunchedEffect(Unit) {
        viewModel.navigateFromSplash()
    }
}
