package uk.ac.tees.mad.payclock.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.payclock.viewmodel.AuthViewModel
import uk.ac.tees.mad.payclock.viewmodel.AuthState

@Composable
fun SplashScreen(
    navController: NavHostController,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    // This effect will react to changes in authState
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> {
                // Navigate to the main app screen
                navController.navigate("jobs") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is AuthState.Unauthenticated -> {
                // Navigate to the login screen
                navController.navigate("login") {
                    popUpTo("splash") { inclusive = true }
                }
            }
            is AuthState.Unknown -> {
                // The ViewModel is still checking. The UI below will show a loading indicator.
            }
        }
    }

    // This is the UI that is shown while the auth state is being determined.
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("PayClock", style = MaterialTheme.typography.headlineLarge)
        CircularProgressIndicator()
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen(navController = rememberNavController())
}
