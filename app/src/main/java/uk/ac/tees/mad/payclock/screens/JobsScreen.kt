package uk.ac.tees.mad.payclock.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.payclock.data.Job
import uk.ac.tees.mad.payclock.viewmodel.JobViewModel
import kotlin.random.Random

/**
 * Stateful composable that provides the data and logic to the JobScreen.
 */
@Composable
fun JobScreenRoute(
    navController: NavHostController,
    jobViewModel: JobViewModel = viewModel()
) {
    val jobs by jobViewModel.jobs.collectAsState()
    JobScreen(
        jobs = jobs,
        onAddJob = { jobViewModel.addJob(it) },
        onRemoveJob = { jobViewModel.removeJob(it) },
        navController = navController
    )
}

/**
 * Stateless composable that displays the list of jobs and handles UI events.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobScreen(
    navController: NavController,
    jobs: List<Job>,
    onAddJob: (Job) -> Unit,
    onRemoveJob: (Job) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        AddJobDialog(
            onDismiss = { showDialog = false },
            onJobAdd = {
                onAddJob(it) // ERROR: 'it' is not defined here
                showDialog = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Profiles") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Job")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showDialog) {
                AddJobDialog(
                    onDismiss = { showDialog = false },
                    onJobAdd = {
                        onAddJob(it)
                        showDialog = false
                    }
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding
            ) {
                items(jobs) { job ->
                    JobItem(job = job, onDelete = { onRemoveJob(it) })
                }
            }
        }
    }
}

@Composable
fun JobItem(
    job: Job,
    onDelete: (Job) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = job.name, fontWeight = FontWeight.Bold)
                Text(text = "£${job.hourlyRate}/hr")
            }
            IconButton(onClick = { onDelete(job) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Job")
            }
        }
    }
}

@Composable
fun AddJobDialog(
    onDismiss: () -> Unit,
    onJobAdd: (Job) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var breakTime by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Job") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Job Name") }
                )
                OutlinedTextField(
                    value = hourlyRate,
                    onValueChange = { hourlyRate = it },
                    label = { Text("Hourly Rate") }
                )
                OutlinedTextField(
                    value = breakTime,
                    onValueChange = { breakTime = it },
                    label = { Text("Break Time (minutes)") }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val rate = hourlyRate.toDoubleOrNull() ?: 0.0
                    val breakMinutes = breakTime.toIntOrNull() ?: 0
                    val newJob = Job(
                        id = Random.nextInt(), // Temporary unique ID
                        name = name,
                        hourlyRate = rate,
                        breakTimeInMinutes = breakMinutes
                    )
                    onJobAdd(newJob)
                }
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun JobScreenPreview() {
    val navController = rememberNavController()
    val sampleJobs = listOf(
        Job(1, "Android Developer", 25.50, 30),
        Job(2, "UX Designer", 30.0, 60),
        Job(3, "Project Manager", 45.25, 30)
    )

    // Preview the stateless screen with fake data and empty actions.
    JobScreen(
        jobs = sampleJobs,
        onAddJob = {},
        onRemoveJob = {},
        navController = navController
    )
}
