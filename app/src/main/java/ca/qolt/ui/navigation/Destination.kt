package ca.qolt.ui.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

object Destination {
    data object Splash : Screen("splash")
    data object Onboarding : Screen("onboarding")
    data object QoltTag : Screen("qoltTag")
    data object Login : Screen("login")
    data object CreateAccount : Screen("createAccount")
    data object ForgotPassword : Screen("forgotPassword")
    data object Main : Screen("main") {
        data object Home : Screen("main/home")
        data object Blocks : Screen("main/blocks")
        data object Statistics : Screen("main/statistics")
        data object Profile : Screen("main/profile")
    }

    abstract class Screen(baseRoute: String) {
        companion object {
            const val BASE_DEEPLINK_URL = "app://qolt"
        }

        open val route = baseRoute
        open val deeplink = "${BASE_DEEPLINK_URL}/$baseRoute"
    }

    abstract class DynamicScreen(
        private val baseRoute: String,
        val routeArgName: String,
    ) : Screen(baseRoute) {

        val navArguments = listOf(navArgument(routeArgName) { type = NavType.StringType })

        override val route = "$baseRoute/{$routeArgName}"
        override val deeplink = "${BASE_DEEPLINK_URL}/$baseRoute/{$routeArgName}"

        fun dynamicRoute(param: String) = "$baseRoute/$param"

        fun dynamicDeeplink(param: String) = "$BASE_DEEPLINK_URL/$baseRoute/${param}"
    }
    abstract class MultiArgScreen(
        protected val baseRoute: String,
        private val argNames: List<String>,
    ) : Screen(baseRoute) {

        val navArguments = argNames.map { name ->
            navArgument(name) { type = NavType.StringType }
        }

        override val route: String = buildString {
            append(baseRoute)
            argNames.forEach { append("/{$it}") }
        }

        override val deeplink: String = buildString {
            append("$BASE_DEEPLINK_URL/$baseRoute")
            argNames.forEach { append("/{$it}") }
        }
    }

    fun DynamicScreenArgs(name: String) = listOf(navArgument(name) { type = NavType.StringType })
}