package uk.ac.tees.mad.payclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.payclock.screens.ForgotPasswordScreen
import uk.ac.tees.mad.payclock.screens.JobScreenRoute
import uk.ac.tees.mad.payclock.screens.LoginScreen
import uk.ac.tees.mad.payclock.screens.SignUpScreen
import uk.ac.tees.mad.payclock.screens.SplashScreen
import uk.ac.tees.mad.payclock.screens.TimeLogScreenRoute
import uk.ac.tees.mad.payclock.ui.theme.PayClockTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PayClockApp()
        }
    }
}

@Composable
fun PayClockApp() {
    PayClockTheme {
        val navController = rememberNavController()
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "splash", // The app now starts at the splash screen
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = "splash") {
                    SplashScreen(navController = navController)
                }
                composable(route = "login") {
                    LoginScreen(navController = navController)
                }
                composable(route = "signup") {
                    SignUpScreen(navController = navController)
                }
                composable(route = "forgot_password") {
                    ForgotPasswordScreen(navController = navController)
                }
                composable(route = "jobs") {
                    JobScreenRoute(navController = navController)
                }
                composable(route = "time_log") {
                    TimeLogScreenRoute(navController = navController)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PayClockApp()
}
