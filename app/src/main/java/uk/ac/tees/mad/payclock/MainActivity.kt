package uk.ac.tees.mad.payclock

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.payclock.screens.AddJobScreen
import uk.ac.tees.mad.payclock.screens.JobScreen
import uk.ac.tees.mad.payclock.ui.theme.PayClockTheme
import uk.ac.tees.mad.payclock.viewmodel.JobViewModel

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
        // Instantiate the ViewModel
        val jobViewModel: JobViewModel = viewModel()
        // Collect the list of jobs as state
        val jobs by jobViewModel.jobs.collectAsState()
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = "jobs",
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = "jobs") {
                    JobScreen(
                        navController = navController,
                        jobs = jobs, // Pass the state from the ViewModel
                        onAddJob = {
//                            navController.navigate("addJobScreen")
                        },
                        onRemoveJob = { job -> jobViewModel.removeJob(job) }
                    )
                }
                composable(route = "addJobScreen") {
                    AddJobScreen(navController = navController, jobViewModel)
                }
                // You can add other screens to your navigation graph here
                // composable(route = "landing") { ... }
                // composable(route = "graph") { ... }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    PayClockApp()
}