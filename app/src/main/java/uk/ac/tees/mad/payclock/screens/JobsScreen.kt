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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import uk.ac.tees.mad.payclock.data.models.Job
import uk.ac.tees.mad.payclock.viewmodel.JobViewModel
import uk.ac.tees.mad.payclock.viewmodel.MainViewModel

@Composable
fun JobScreenRoute(
    navController: NavHostController,
    jobViewModel: JobViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel()
) {
    val jobs by jobViewModel.jobs.collectAsState()
    JobScreen(
        jobs = jobs,
        onAddJob = { name, rate, breakTime -> jobViewModel.addJob(name, rate, breakTime) },
        onRemoveJob = { jobViewModel.removeJob(it) },
        onLogout = { mainViewModel.logout() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobScreen(
    jobs: List<Job>,
    onAddJob: (String, Double, Int) -> Unit,
    onRemoveJob: (Job) -> Unit,
    onLogout: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Profiles") },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
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
                    onAddJob(it.name, it.hourlyRate, it.breakTimeInMinutes)
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
                    onDelete = { onRemoveJob(job) }
                )
            }
        }
    }
}

@Composable
fun JobItem(job: Job, onDelete: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
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
            IconButton(onClick = onDelete) {
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
                        userId = "", // This is a temporary value, the ViewModel will supply the real one
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
        Job(id = "1", userId = "user1", name = "Android Developer", hourlyRate = 25.50, breakTimeInMinutes = 30),
        Job(id = "2", userId = "user1", name = "UX Designer", hourlyRate = 30.0, breakTimeInMinutes = 60),
    )
    JobScreen(
        jobs = sampleJobs,
        onAddJob = { _, _, _ -> },
        onRemoveJob = {},
        onLogout = {}
    )
}
