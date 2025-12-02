package ca.qolt.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import ca.qolt.ui.account.HelpCenter
import ca.qolt.ui.account.Profile
import ca.qolt.ui.account.ProfileViewModel
import ca.qolt.ui.account.HelpCenterViewModel
import ca.qolt.ui.blocks.Presets
import ca.qolt.ui.blocks.PresetsViewModel
import ca.qolt.ui.home.Home
import ca.qolt.ui.home.HomeViewModel
import ca.qolt.ui.registration.createaccount.CreateAccount
import ca.qolt.ui.registration.createaccount.CreateAccountViewModel
import ca.qolt.ui.registration.forgotpassword.ForgotPassword
import ca.qolt.ui.registration.forgotpassword.ForgotPasswordViewModel
import ca.qolt.ui.registration.login.Login
import ca.qolt.ui.registration.login.LoginViewModel
import ca.qolt.ui.registration.onboarding.Onboarding
import ca.qolt.ui.registration.onboarding.OnboardingViewModel
import ca.qolt.ui.registration.qolttag.QoltTag
import ca.qolt.ui.registration.qolttag.QoltTagViewModel
import ca.qolt.ui.splash.Splash
import ca.qolt.ui.splash.SplashViewModel
import ca.qolt.ui.statistics.Statistics
import ca.qolt.ui.statistics.StatisticsViewModel

@Composable
fun NavGraph(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    startDestination: String = Destination.Splash.route,
    navigator: Navigator,
    finish: () -> Unit = {},
) {
    NavHandler(navController = navController, navigator = navigator, finish = finish)
    NavHost(navController = navController, startDestination = startDestination) {
        composable(Destination.Splash.route) {
            val viewModel: SplashViewModel = hiltViewModel(key = SplashViewModel.TAG)
            Splash(modifier, viewModel)
        }
        composable(Destination.Onboarding.route) {
            val viewModel: OnboardingViewModel = hiltViewModel(key = OnboardingViewModel.TAG)
            Onboarding(modifier, viewModel)
        }
        composable(Destination.QoltTag.route) {
            val viewModel: QoltTagViewModel = hiltViewModel(key = QoltTagViewModel.TAG)
            QoltTag(modifier, viewModel)
        }
        composable(Destination.Login.route) {
            val viewModel: LoginViewModel = hiltViewModel(key = LoginViewModel.TAG)
            Login(modifier, viewModel)
        }
        composable(Destination.CreateAccount.route) {
            val viewModel: CreateAccountViewModel = hiltViewModel(key = CreateAccountViewModel.TAG)
            CreateAccount(modifier, viewModel)
        }
        composable(Destination.ForgotPassword.route) {
            val viewModel: ForgotPasswordViewModel = hiltViewModel(key = ForgotPasswordViewModel.TAG)
            ForgotPassword(modifier, viewModel)
        }

        navigation(
            route = Destination.Main.route,
            startDestination = Destination.Main.Home.route
        ) {
            composable(Destination.Main.Home.route) {
                val viewModel: HomeViewModel = hiltViewModel(key = HomeViewModel.TAG)
                Home(modifier, viewModel)
            }
            composable(Destination.Main.Blocks.route) {
                val viewModel: PresetsViewModel = hiltViewModel(key = PresetsViewModel.TAG)
                Presets(modifier, viewModel)
            }
            composable(Destination.Main.Statistics.route) {
                val viewModel: StatisticsViewModel = hiltViewModel(key = StatisticsViewModel.TAG)
                Statistics(modifier, viewModel)
            }
            composable(Destination.Main.Profile.route) {
                val viewModel: ProfileViewModel = hiltViewModel(key = ProfileViewModel.TAG)
                Profile(modifier, viewModel)
            }
            composable(Destination.Main.Profile.HelpCenter.route) {
                val viewModel: HelpCenterViewModel = hiltViewModel(key = HelpCenterViewModel.TAG)
                HelpCenter(modifier, viewModel)
            }
        }
    }
}