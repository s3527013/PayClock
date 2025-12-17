package uk.ac.tees.mad.payclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.core.Graph
import uk.ac.tees.mad.payclock.drawer.AppDrawer
import uk.ac.tees.mad.payclock.features.auth.AuthViewModel
import uk.ac.tees.mad.payclock.features.auth.ForgotPasswordScreen
import uk.ac.tees.mad.payclock.features.auth.LoginScreen
import uk.ac.tees.mad.payclock.features.auth.SignUpScreen
import uk.ac.tees.mad.payclock.features.auth.SplashScreen
import uk.ac.tees.mad.payclock.features.jobs.JobScreenRoute
import uk.ac.tees.mad.payclock.features.report.ReportScreenRoute
import uk.ac.tees.mad.payclock.features.settings.SettingsScreen
import uk.ac.tees.mad.payclock.features.settings.SettingsViewModel
import uk.ac.tees.mad.payclock.features.timelog.TimeLogControlScreenRoute
import uk.ac.tees.mad.payclock.features.timelog.TimeLogScreenRoute
import uk.ac.tees.mad.payclock.ui.theme.PayClockTheme

/**
 * The main activity for the PayClock application.
 * This activity is the entry point of the app and sets up the main UI content.
 */
class MainActivity : ComponentActivity() {

    /**
     * Called when the activity is first created.
     * This function sets up the edge-to-edge display and composes the main app theme and layout.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     * this Bundle contains the data it most recently supplied in [onSaveInstanceState]. Otherwise it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val settingsViewModel: SettingsViewModel = Graph.settingsViewModel
            val themeChoice by settingsViewModel.themeChoice.collectAsState()
            val useDynamicColor by settingsViewModel.useDynamicColor.collectAsState()

            PayClockTheme(
                themeChoice = themeChoice,
                useDynamicColor = useDynamicColor
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PayClockApp()
                }
            }
        }
    }
}

/**
 * The main composable function for the PayClock application.
 *
 * This function sets up the entire application's UI, including the navigation drawer,
 * top and bottom app bars, and the navigation host for all the different screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayClockApp() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomNavItems = listOf("jobs", "time_log", "reports")
    val showBottomAndTopBar = currentDestination?.route in bottomNavItems

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            // Use the extracted AppDrawer composable
            AppDrawer(
                drawerState = drawerState,
                scope = scope,
                navController = navController,
                authViewModel = authViewModel
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                if (showBottomAndTopBar) {
                    TopAppBar(
                        title = { Text("PayClock") },
                        navigationIcon = {
                            IconButton(onClick = {
                                scope.launch { drawerState.apply { if (isClosed) open() else close() } }
                            }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu")
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            titleContentColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            },
            bottomBar = {
                if (showBottomAndTopBar) {
                    BottomAppBar {
                        bottomNavItems.forEach { screen ->
                            NavigationBarItem(
                                icon = {
                                    when (screen) {
                                        "jobs" -> Icon(
                                            Icons.Filled.Home,
                                            contentDescription = null
                                        )

                                        "time_log" -> Icon(
                                            Icons.AutoMirrored.Filled.List,
                                            contentDescription = null
                                        )

                                        "reports" -> Icon(
                                            Icons.Filled.Assessment,
                                            contentDescription = null
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        screen.split('_')
                                            .joinToString(" ") { it.replaceFirstChar(Char::uppercase) })
                                },
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
                composable(route = "settings") {
                    SettingsScreen(navController = navController)
                }
            }
        }
    }
}

/**
 * A preview composable for the [PayClockApp].
 * This allows for easy previewing of the entire application layout in Android Studio.
 */
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PayClockApp()
}