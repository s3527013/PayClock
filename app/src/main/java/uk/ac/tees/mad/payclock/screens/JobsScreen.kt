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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import uk.ac.tees.mad.payclock.data.models.Job
import uk.ac.tees.mad.payclock.viewmodel.JobViewModel

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
        onRemoveJob = { jobViewModel.removeJob(it) }
    )
}

/**
 * Stateless composable that displays the list of jobs and handles UI events.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobScreen(
    jobs: List<Job>,
    onAddJob: (Job) -> Unit,
    onRemoveJob: (Job) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            items(jobs) { job ->
                JobItem(
                    job = job,
                    onDelete = { onRemoveJob(job) } // Simplified callback
                )
            }
        }
    }
}

@Composable
fun JobItem(
    job: Job,
    onDelete: () -> Unit // Simplified signature
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
            IconButton(onClick = onDelete) { // Simplified call
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
                    // Use the correct constructor for the Room entity
                    val newJob = Job(
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
    val sampleJobs = listOf(
        Job(id = 1, name = "Android Developer", hourlyRate = 25.50, breakTimeInMinutes = 30),
        Job(id = 2, name = "UX Designer", hourlyRate = 30.0, breakTimeInMinutes = 60),
        Job(id = 3, name = "Project Manager", hourlyRate = 45.25, breakTimeInMinutes = 30)
    )

    JobScreen(
        jobs = sampleJobs,
        onAddJob = {},
        onRemoveJob = {}
    )
}
