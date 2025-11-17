package uk.ac.tees.mad.payclock.features.jobs

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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.navigation.NavHostController
import uk.ac.tees.mad.payclock.features.jobs.data.Job
import uk.ac.tees.mad.payclock.features.timelog.TimeLogViewModel

@Composable
fun JobScreenRoute(
    navController: NavHostController,
    jobViewModel: JobViewModel = viewModel(),
    timeLogViewModel: TimeLogViewModel = viewModel(),
) {
    val jobs by jobViewModel.jobs.collectAsState()
    val activeTimeLog by timeLogViewModel.activeTimeLog.collectAsState()

    JobScreen(
        jobs = jobs,
        onAddJob = { name, rate, breakTime -> jobViewModel.addJob(name, rate, breakTime) },
        onUpdateJob = { jobViewModel.updateJob(it) },
        onRemoveJob = { jobViewModel.removeJob(it) },
        onStartTimelog = { jobId ->
            timeLogViewModel.startNewShift(jobId)
            navController.navigate("active_time_log") // Navigate to see the active shift
        },
        isShiftActive = activeTimeLog != null
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobScreen(
    jobs: List<Job>,
    onAddJob: (String, Double, Int) -> Unit,
    onUpdateJob: (Job) -> Unit,
    onRemoveJob: (Job) -> Unit,
    onStartTimelog: (String) -> Unit,
    isShiftActive: Boolean,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var jobToEdit by remember { mutableStateOf<Job?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Job Profiles") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Job")
            }
        }
    ) { padding ->
        if (showAddDialog) {
            AddJobDialog(
                onDismiss = { showAddDialog = false },
                onJobAdd = { name, rate, breakTime ->
                    onAddJob(name, rate, breakTime)
                    showAddDialog = false
                }
            )
        }

        jobToEdit?.let { job ->
            UpdateJobDialog(
                job = job,
                onDismiss = { jobToEdit = null },
                onJobUpdate = {
                    onUpdateJob(it)
                    jobToEdit = null
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
                    onEdit = { jobToEdit = job },
                    onDelete = { onRemoveJob(job) },
                    onStartTimelog = { onStartTimelog(job.id) },
                    isShiftActive = isShiftActive
                )
            }
        }
    }
}

@Composable
fun JobItem(
    job: Job,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStartTimelog: () -> Unit,
    isShiftActive: Boolean
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(text = job.name, fontWeight = FontWeight.Bold)
                Text(text = "£${job.hourlyRate}/hr")
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onStartTimelog, enabled = !isShiftActive) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start Shift")
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Job")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Job")
                }
            }
        }
    }
}

@Composable
fun AddJobDialog(
    onDismiss: () -> Unit,
    onJobAdd: (String, Double, Int) -> Unit
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
                    label = { Text("Hourly Rate") },
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
                    onJobAdd(name, rate, breakMinutes)
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

@Composable
fun UpdateJobDialog(
    job: Job,
    onDismiss: () -> Unit,
    onJobUpdate: (Job) -> Unit
) {
    var name by remember { mutableStateOf(job.name) }
    var hourlyRate by remember { mutableStateOf(job.hourlyRate.toString()) }
    var breakTime by remember { mutableStateOf(job.breakTimeInMinutes.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Update Job") },
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
                    val rate = hourlyRate.toDoubleOrNull() ?: job.hourlyRate
                    val breakMinutes = breakTime.toIntOrNull() ?: job.breakTimeInMinutes
                    onJobUpdate(job.copy(name = name, hourlyRate = rate, breakTimeInMinutes = breakMinutes))
                }
            ) {
                Text("Update")
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
        onUpdateJob = { _ -> },
        onRemoveJob = {},
        onStartTimelog = {},
        isShiftActive = false
    )
}
