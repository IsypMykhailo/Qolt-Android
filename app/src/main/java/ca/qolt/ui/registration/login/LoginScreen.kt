package ca.qolt.ui.registration.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.R

@Composable
fun Login(
    modifier: Modifier = Modifier,
    viewModel: LoginViewModel
) {
    BackHandler { viewModel.onBack() }

    LoginScreen(
        onBack = viewModel::onBack,
        onLogin = viewModel::onLogin,
        onCreateAccount = viewModel::onCreateAccount,
        onForgotPassword = viewModel::onForgotPassword
    )
}

@Composable
fun LoginScreen(
    onBack: () -> Unit = {},
    onLogin: () -> Unit = {},
    onCreateAccount: () -> Unit = {},
    onGoogle: () -> Unit = {},
    onFacebook: () -> Unit = {},
    onApple: () -> Unit = {},
    onForgotPassword: () -> Unit = {},
    onSignUp: () -> Unit = {}
) {
    val orange = Color(0xFFFF6A1A)
    val lightGray = Color(0xFF888888)

    val loginEmail = "faa30@sfu.ca"
    val loginPassword = "password"

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .padding(24.dp)
    ) {

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier
                        .size(28.dp)
                        .clickable { onBack() }
                )
            }

            Text(
                text = "Log In",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                "Email",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Enter Email", color = lightGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(40.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isError) Color.Red else Color.White,
                    unfocusedBorderColor = if (isError) Color.Red else Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                "Password",
                color = Color.White,
                fontSize = 14.sp,
                modifier = Modifier.align(Alignment.Start)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Enter Password", color = lightGray) },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Icon(
                        painter = painterResource(
                            if (showPassword) R.drawable.ic_menu_close_clear_cancel
                            else R.drawable.ic_partial_secure
                        ),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { showPassword = !showPassword }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(40.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (isError) Color.Red else Color.White,
                    unfocusedBorderColor = if (isError) Color.Red else Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Color.White
                )
            )


            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Forgot Password?",
                color = Color(0xFF5DA3FA),
                fontSize = 14.sp,
                modifier = Modifier
                    .align(Alignment.Start)
                    .clickable { onForgotPassword() }
            )

            Spacer(modifier = Modifier.height(26.dp))

            Button(
                onClick = {
                    if (email == loginEmail && password == loginPassword) {
                        isError = false
                        onLogin()
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = orange),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    "Log In",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = { onCreateAccount() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                shape = RoundedCornerShape(50),
                border = ButtonDefaults.outlinedButtonBorder,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    "Create Account",
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Divider(
                    color = Color.DarkGray,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    "  or continue with  ",
                    color = lightGray,
                    fontSize = 14.sp
                )

                Divider(
                    color = Color.DarkGray,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                SocialLoginButton(
                    icon = ca.qolt.R.drawable.google_icon,
                    borderColor = Color(0xFFDB4437),
                    onClick = onGoogle
                )

                SocialLoginButton(
                    icon = ca.qolt.R.drawable.facebook_icon,
                    borderColor = Color(0xFF1877F2),
                    onClick = onFacebook
                )

                SocialLoginButton(
                    icon = ca.qolt.R.drawable.apple_icon,
                    borderColor = Color.White,
                    onClick = onApple
                )
            }
        }

        // error popup
        if (isError) {
            Box(
                modifier = Modifier
                    .width(300.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color(0xFFCC0000), RoundedCornerShape(50))
                    .padding(start = 24.dp, top = 6.dp, end = 24.dp, bottom = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(ca.qolt.R.drawable.error_icon),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Incorrect email or password",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SocialLoginButton(
    icon: Int,
    borderColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(90.dp)
            .height(54.dp)
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
    }
}

