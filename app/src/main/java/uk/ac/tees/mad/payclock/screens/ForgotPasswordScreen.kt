package uk.ac.tees.mad.payclock.screens

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.payclock.ui.theme.PayClockTheme
import uk.ac.tees.mad.payclock.viewmodel.ForgotPasswordState
import uk.ac.tees.mad.payclock.viewmodel.ForgotPasswordViewModel

@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    forgotPasswordViewModel: ForgotPasswordViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    val state by forgotPasswordViewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(state) {
        when (val currentState = state) {
            is ForgotPasswordState.Success -> {
                Toast.makeText(context, "Password reset link sent!", Toast.LENGTH_LONG).show()
                navController.popBackStack()
                forgotPasswordViewModel.resetState()
            }
            is ForgotPasswordState.Error -> {
                Toast.makeText(context, currentState.message, Toast.LENGTH_LONG).show()
                forgotPasswordViewModel.resetState()
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
        Text("Reset Password", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Enter your email to receive a password reset link.")
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is ForgotPasswordState.Loading
        )
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { forgotPasswordViewModel.requestReset(email) },
            modifier = Modifier.fillMaxWidth(),
            enabled = state !is ForgotPasswordState.Loading
        ) {
            if (state is ForgotPasswordState.Loading) {
                CircularProgressIndicator(modifier = Modifier.height(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Send Reset Link")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Back to Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ForgotPasswordScreenPreview() {
    PayClockTheme {
        ForgotPasswordScreen(navController = rememberNavController())
    }
}
