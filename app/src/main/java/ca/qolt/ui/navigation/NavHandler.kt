package ca.qolt.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController

@Composable
internal fun NavHandler(
    navController: NavController,
    navigator: Navigator,
    finish: () -> Unit
) {
    LaunchedEffect("navigation") {
        navigator.navigate.collect {
            if (it.popBackstack) navController.popBackStack()
            navController.navigate(it.route)
        }
    }

    LaunchedEffect("navigation") {

        navigator.deeplink.collect {
            if (navController.graph.hasDeepLink(it.uri)) {
                val reached = navController.currentDestination?.hasDeepLink(it.uri) ?: false
                if (!reached) {
                    if (it.popBackstack) navController.popBackStack()
                    navController.navigate(it.uri)
                }
            }
        }
    }

    LaunchedEffect("navigation") {
        navigator.back.collect {
            if (it.recreate) {
                navController.previousBackStackEntry?.destination?.route?.let { route ->
                    navController.navigate(route) {
                        popUpTo(route) { inclusive = true }
                    }
                }
            } else {
                navController.navigateUp()
            }
        }
    }

    LaunchedEffect("navigation") {
        navigator.end.collect {
            finish()
        }
    }
}