package ca.qolt.ui.registration.createaccount

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import android.app.DatePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.ui.platform.LocalContext
import ca.qolt.R
import java.util.Calendar

@Composable
fun CreateAccount(modifier: Modifier = Modifier, viewModel: CreateAccountViewModel) {
    BackHandler { viewModel.navigator.navigateBack() }
    CreateAccountScreen(
        onBack = viewModel::onBack,
        onContinue = { /* Handle continue action */ }
    )
}

@Composable
fun CreateAccountScreen(
    onBack: () -> Unit = {},
    onContinue: () -> Unit = {}
) {
    val orange = Color(0xFFFF6A1A)
    val lightGray = Color(0xFF888888)

    var email by remember { mutableStateOf("") }
    var birthday by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }
    val passwordValid = isPasswordValid(password)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1C1C1E))
            .padding(24.dp)
    ) {

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
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
                text = "Create Account",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text("Email", color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = { Text("Enter Email", color = lightGray) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(40.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Birthday", color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))


            // for date picker
            val context = LocalContext.current
            val calendar = Calendar.getInstance()

            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePicker = DatePickerDialog(
                context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    birthday = "${selectedMonth + 1}/$selectedDay/$selectedYear"
                },
                year,
                month,
                day
            )

            OutlinedTextField(
                value = birthday,
                onValueChange = { birthday = it },
                placeholder = { Text("MM/DD/YYYY", color = lightGray) },
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = R.drawable.calendar_icon),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { datePicker.show() }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(40.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Password", color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = { Text("Enter Password", color = lightGray) },
                visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = if (showPassword) android.R.drawable.ic_menu_close_clear_cancel else android.R.drawable.ic_partial_secure),
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
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text("Confirm Password", color = Color.White, fontSize = 14.sp, modifier = Modifier.align(Alignment.Start))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = { Text("Enter Password", color = lightGray) },
                visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    Icon(
                        painter = painterResource(id = if (showConfirmPassword) android.R.drawable.ic_menu_close_clear_cancel else android.R.drawable.ic_partial_secure),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier
                            .size(22.dp)
                            .clickable { showConfirmPassword = !showConfirmPassword }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(40.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.Gray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(
                        id = if (passwordValid)
                            R.drawable.check_icon
                        else
                            R.drawable.error_icon
                    ),
                    contentDescription = null,
                    tint = if (passwordValid) Color(0xFF00C853) else Color(0xFFFF6A1A),
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Must be at least 8 characters, with uppercase\nand lowercase letters.",
                    color = if (passwordValid) Color(0xFF00C853) else Color(0xFFAAAAAA),
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(containerColor = orange),
                shape = RoundedCornerShape(50),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    "Continue",
                    color = Color.White,
                    fontSize = 18.sp
                )
            }
        }
    }
}

fun isPasswordValid(password: String): Boolean {
    val hasUpper = password.any { it.isUpperCase() }
    val hasLower = password.any { it.isLowerCase() }
    val hasMinLength = password.length >= 8

    return hasUpper && hasLower && hasMinLength
}
