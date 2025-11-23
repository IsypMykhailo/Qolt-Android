package ca.qolt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import ca.qolt.ui.theme.QoltTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            QoltTheme {

                var currentScreen by remember { mutableStateOf("onboarding") }

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
                            onLogin = { /* TODO */ }
                        )

                        "createAccount" -> CreateAccountScreen(
                            onBack = { currentScreen = "login" },
                            onContinue = { /* TODO */ }
                        )

                        "forgotPassword" -> ForgotPasswordScreen(
                            onBack = { currentScreen = "login" },
                            onSendReset = { /* TODO */ },
                            onLoginClick = { currentScreen = "login" }
                        )

                        "presets" -> PresetsScreen()
                    }
                }
            }
        }
    }
}
