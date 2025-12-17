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
import androidx.navigation.NavHostController
import uk.ac.tees.mad.payclock.core.Graph
import uk.ac.tees.mad.payclock.features.jobs.data.Job
import uk.ac.tees.mad.payclock.features.timelog.TimeLogViewModel

/**
 * A route composable for the job screen.
 * This composable connects the [JobViewModel] and [TimeLogViewModel] to the [JobScreen].
 *
 * @param navController The navigation controller.
 */
@Composable
fun JobScreenRoute(
    navController: NavHostController,
) {
    val jobViewModel: JobViewModel = Graph.jobViewModel
    val timeLogViewModel: TimeLogViewModel = Graph.timeLogViewModel
    val jobs by jobViewModel.jobs.collectAsState()
    val activeTimeLog by timeLogViewModel.activeTimeLog.collectAsState()

    JobScreen(
        jobs = jobs,
        onAddJob = { name, rate -> jobViewModel.addJob(name, rate) },
        onUpdateJob = { jobViewModel.updateJob(it) },
        onRemoveJob = { jobViewModel.removeJob(it) },
        onGoToTimeLogControl = {
            jobViewModel.setActiveJob(it)
            navController.navigate("time_log_control")
        },
        isShiftActive = activeTimeLog != null
    )
}

/**
 * A composable function that displays the main job screen.
 *
 * @param jobs The list of jobs.
 * @param onAddJob A callback that is invoked when a new job is added.
 * @param onUpdateJob A callback that is invoked when a job is updated.
 * @param onRemoveJob A callback that is invoked when a job is removed.
 * @param onGoToTimeLogControl A callback that is invoked when the user wants to start a shift for a job.
 * @param isShiftActive Whether a shift is currently active.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobScreen(
    jobs: List<Job>,
    onAddJob: (String, Double) -> Unit,
    onUpdateJob: (Job) -> Unit,
    onRemoveJob: (Job) -> Unit,
    onGoToTimeLogControl: (Job) -> Unit,
    isShiftActive: Boolean,
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var jobToEdit by remember { mutableStateOf<Job?>(null) }
    var jobToDelete by remember { mutableStateOf<Job?>(null) } // New state for delete confirmation

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Job Profiles") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary
            )
        )
    }, floatingActionButton = {
        FloatingActionButton(onClick = { showAddDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = "Add Job")
        }
    }) { padding ->
        if (showAddDialog) {
            AddJobDialog(
                onDismiss = { showAddDialog = false },
                onJobAdd = { name, rate ->
                    onAddJob(name, rate)
                    showAddDialog = false
                })
        }

        jobToEdit?.let { job ->
            UpdateJobDialog(job = job, onDismiss = { jobToEdit = null }, onJobUpdate = {
                onUpdateJob(it)
                jobToEdit = null
            })
        }

        // Show delete confirmation dialog
        jobToDelete?.let { job ->
            DeleteJobDialog(
                job = job,
                onDismiss = { jobToDelete = null },
                onConfirm = {
                    onRemoveJob(job)
                    jobToDelete = null
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
                    onDelete = { jobToDelete = job }, // Updated to show dialog
                    onGoToTimeLogControl = { onGoToTimeLogControl(job) },
                    isShiftActive = isShiftActive
                )
            }
        }
    }
}

/**
 * A composable that displays a dialog to confirm the deletion of a job.
 *
 * @param job The job to be deleted.
 * @param onDismiss A callback that is invoked when the dialog is dismissed.
 * @param onConfirm A callback that is invoked when the user confirms the deletion.
 */
@Composable
fun DeleteJobDialog(
    job: Job,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Job") },
        text = { Text("Are you sure you want to delete the job \"${job.name}\"? This action cannot be undone.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Delete")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


/**
 * A composable that displays a single job item.
 *
 * @param job The job to display.
 * @param onEdit A callback that is invoked when the user wants to edit the job.
 * @param onDelete A callback that is invoked when the user wants to delete the job.
 * @param onGoToTimeLogControl A callback that is invoked when the user wants to start a shift for the job.
 * @param isShiftActive Whether a shift is currently active.
 */
@Composable
fun JobItem(
    job: Job,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onGoToTimeLogControl: () -> Unit,
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
                IconButton(onClick = onGoToTimeLogControl) {
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

/**
 * A composable that displays a dialog for adding a new job.
 *
 * @param onDismiss A callback that is invoked when the dialog is dismissed.
 * @param onJobAdd A callback that is invoked when a new job is added.
 */
@Composable
fun AddJobDialog(
    onDismiss: () -> Unit, onJobAdd: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Add New Job") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Job Name") })
            OutlinedTextField(
                value = hourlyRate,
                onValueChange = { hourlyRate = it },
                label = { Text("Hourly Rate") },
            )
        }
    }, confirmButton = {
        Button(
            onClick = {
                val rate = hourlyRate.toDoubleOrNull() ?: 0.0
                onJobAdd(name, rate)
            }) {
            Text("Add")
        }
    }, dismissButton = {
        Button(onClick = onDismiss) {
            Text("Cancel")
        }
    })
}


/**
 * A composable that displays a dialog for updating a job.
 *
 * @param job The job to be updated.
 * @param onDismiss A callback that is invoked when the dialog is dismissed.
 * @param onJobUpdate A callback that is invoked when the job is updated.
 */
@Composable
fun UpdateJobDialog(
    job: Job, onDismiss: () -> Unit, onJobUpdate: (Job) -> Unit
) {
    var name by remember { mutableStateOf(job.name) }
    var hourlyRate by remember { mutableStateOf(job.hourlyRate.toString()) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Update Job") }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Job Name") })
            OutlinedTextField(
                value = hourlyRate,
                onValueChange = { hourlyRate = it },
                label = { Text("Hourly Rate") })
        }
    }, confirmButton = {
        Button(
            onClick = {
                val rate = hourlyRate.toDoubleOrNull() ?: job.hourlyRate
                onJobUpdate(
                    job.copy(
                        name = name, hourlyRate = rate
                    )
                )
            }) {
            Text("Update")
        }
    }, dismissButton = {
        Button(onClick = onDismiss) {
            Text("Cancel")
        }
    })
}

/**
 * A preview for the [JobScreen] composable.
 */
@Preview(showBackground = true)
@Composable
fun JobScreenPreview() {
    val sampleJobs = listOf(
        Job(
            id = "1",
            userId = "user1",
            name = "Android Developer",
            hourlyRate = 25.50
        ),
        Job(
            id = "2",
            userId = "user1",
            name = "UX Designer",
            hourlyRate = 30.0
        ),
    )
    JobScreen(
        jobs = sampleJobs,
        onAddJob = { _, _ -> },
        onUpdateJob = { _ -> },
        onRemoveJob = {},
        onGoToTimeLogControl = {},
        isShiftActive = false
    )
}