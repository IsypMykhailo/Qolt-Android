package ca.qolt

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import ca.qolt.ui.theme.QoltTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QoltTheme {
                val context = LocalContext.current
                val initialScreen = "main"
                var currentScreen by remember { mutableStateOf(initialScreen) }

                Crossfade(targetState = currentScreen) { screen ->

                    when (screen) {

                        "onboarding" ->
                            OnboardingPager(
                                onFinished = { currentScreen = "qoltTag" }
                            )

                        "qoltTag" ->
                            QoltTagScreen(
                                onHaveTag = { currentScreen = "login" },
                                onNoTag = { currentScreen = "login" }
                            )

                        "login" -> LoginScreen(
                            onBack = { currentScreen = "qoltTag" },
                            onCreateAccount = { currentScreen = "createAccount" },
                            onForgotPassword = { currentScreen = "forgotPassword" },
                            onLogin = {
                                PreferencesManager.setLoggedIn(context, true)
                                currentScreen = "main"
                            }
                        )

                        "createAccount" -> CreateAccountScreen(
                            onBack = { currentScreen = "login" },
                            onContinue = { }
                        )

                        "forgotPassword" -> ForgotPasswordScreen(
                            onBack = { currentScreen = "login" },
                            onSendReset = { },
                            onLoginClick = { currentScreen = "login" }
                        )
                        
                        "statistics" -> ca.qolt.ui.statistics.StatisticsScreen()

                        "main" -> MainScreen(
                            onLogout = {
                                PreferencesManager.clearLoginState(context)
                                currentScreen = "onboarding"
                            }
                        )

                        "presets" -> PresetsScreen()
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}
