package ca.qolt.ui

import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import ca.qolt.ui.home.HomeBottomBar
import ca.qolt.ui.navigation.NavGraph
import ca.qolt.ui.navigation.Navigator
import ca.qolt.ui.theme.QoltTheme

@Composable
fun QoltApp(navigator: Navigator, finish: () -> Unit) {
    QoltTheme {
        val navController = rememberNavController()
        Scaffold(
            modifier = Modifier.imePadding(),
            bottomBar = { HomeBottomBar(navController = navController) }
        ) { innerPaddingModifier ->
            NavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPaddingModifier),
                navigator = navigator,
                finish = finish,
            )
        }
    }
}