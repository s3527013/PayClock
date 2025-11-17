package uk.ac.tees.mad.payclock.features.auth

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.payclock.ui.theme.PayClockTheme

@Composable
fun SignUpScreen(
    navController: NavHostController,
    signUpViewModel: SignUpViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val state by signUpViewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state) {
        when (val currentState = state) {
            is SignUpState.Success -> {
                Toast.makeText(context, "Sign-up successful!", Toast.LENGTH_SHORT).show()
                navController.navigate("jobs") { popUpTo("login") { inclusive = true } }
                signUpViewModel.resetState()
            }

            is SignUpState.Error -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                signUpViewModel.resetState()
            }

            else -> Unit
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create an Account", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is SignUpState.Loading
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is SignUpState.Loading
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is SignUpState.Loading,
            isError = password != confirmPassword
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { signUpViewModel.signUp(email, password) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is SignUpState.Loading && password == confirmPassword
        ) {
            if (state is SignUpState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Sign Up")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(
            onClick = { navController.navigate("login") },
            enabled = state !is SignUpState.Loading
        ) {
            Text("Already have an account? Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    PayClockTheme {
        SignUpScreen(navController = rememberNavController())
    }
}
