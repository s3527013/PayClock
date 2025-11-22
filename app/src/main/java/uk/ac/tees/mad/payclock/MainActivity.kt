package uk.ac.tees.mad.payclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.payclock.features.auth.ForgotPasswordScreen
import uk.ac.tees.mad.payclock.features.auth.LoginScreen
import uk.ac.tees.mad.payclock.features.auth.SignUpScreen
import uk.ac.tees.mad.payclock.features.auth.SplashScreen
import uk.ac.tees.mad.payclock.features.jobs.JobScreenRoute
import uk.ac.tees.mad.payclock.features.report.ReportScreenRoute
import uk.ac.tees.mad.payclock.features.timelog.TimeLogControlScreenRoute
import uk.ac.tees.mad.payclock.features.timelog.TimeLogScreenRoute
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
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val items = listOf("jobs", "time_log", "reports")
                if (currentDestination?.route in items) {
                    BottomAppBar {
                        items.forEach { screen ->
                            NavigationBarItem(
                                icon = {
                                    when (screen) {
                                        "jobs" -> Icon(Icons.Filled.Home, contentDescription = null)
                                        "time_log" -> Icon(Icons.Filled.List, contentDescription = null)
                                        "reports" -> Icon(Icons.Filled.Assessment, contentDescription = null)
                                    }
                                },
                                label = { Text(screen.replaceFirstChar { it.uppercase() }) },
                                selected = currentDestination?.hierarchy?.any { it.route == screen } == true,
                                onClick = {
                                    navController.navigate(screen) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "splash",
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
                composable(route = "time_log_control") {
                    TimeLogControlScreenRoute(navController = navController)
                }
                composable(route = "reports") {
                    ReportScreenRoute(navController = navController)
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
