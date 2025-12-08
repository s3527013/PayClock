@file:Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VARIABLE", "UNUSED_VALUE")

package uk.ac.tees.mad.payclock.features.timelog

import android.Manifest
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch
import uk.ac.tees.mad.payclock.core.Graph
import uk.ac.tees.mad.payclock.features.jobs.JobViewModel
import uk.ac.tees.mad.payclock.features.jobs.data.Job
import uk.ac.tees.mad.payclock.features.timelog.data.TimeLog
import uk.ac.tees.mad.payclock.features.timelog.data.TimeLogWithJob
import uk.ac.tees.mad.payclock.features.timelog.util.reverseGeocodeWithBigDataCloud
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun TimeLogScreenRoute(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val timeLogViewModel: TimeLogViewModel = Graph.timeLogViewModel
    val allTimeLogs by timeLogViewModel.allTimeLogs.collectAsState()
    val activeTimeLog by timeLogViewModel.activeTimeLog.collectAsState()

    // Collect jobs from Graph.jobViewModel
    val jobViewModel = Graph.jobViewModel
    val jobs by jobViewModel.jobs.collectAsState()

    val scope = rememberCoroutineScope()

    // Location client and permission handling (pending callback pattern)
    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    val pendingLocationCallback =
        remember { mutableStateOf<((lat: Double?, lng: Double?) -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted: Boolean ->
            val callback = pendingLocationCallback.value
            if (callback == null) return@rememberLauncherForActivityResult

            if (isGranted) {
                try {
                    locationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            callback(location.latitude, location.longitude)
                        } else {
                            Toast.makeText(
                                context,
                                "Location unavailable; proceeding without location.",
                                Toast.LENGTH_SHORT
                            ).show()
                            callback(null, null)
                        }
                    }.addOnFailureListener {
                        Toast.makeText(
                            context,
                            "Failed to get location; proceeding without location.",
                            Toast.LENGTH_SHORT
                        ).show()
                        callback(null, null)
                    }
                } catch (_: SecurityException) {
                    callback(null, null)
                }
            } else {
                Toast.makeText(
                    context,
                    "Location permission denied; proceeding without location.",
                    Toast.LENGTH_SHORT
                ).show()
                callback(null, null)
            }

            pendingLocationCallback.value = null
        }
    )

    fun fetchLocationAndThen(onResult: (lat: Double?, lng: Double?) -> Unit) {
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            pendingLocationCallback.value = onResult
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        try {
            locationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onResult(location.latitude, location.longitude)
                } else {
                    Toast.makeText(
                        context,
                        "Location unavailable; proceeding without location.",
                        Toast.LENGTH_SHORT
                    ).show()
                    onResult(null, null)
                }
            }.addOnFailureListener {
                Toast.makeText(
                    context,
                    "Failed to get location; proceeding without location.",
                    Toast.LENGTH_SHORT
                ).show()
                onResult(null, null)
            }
        } catch (_: SecurityException) {
            onResult(null, null)
        }
    }

    TimeLogScreen(
        timeLogs = allTimeLogs,
        activeLog = activeTimeLog,
        jobs = jobs,
        onStartTimeLog = { jobId ->
            fetchLocationAndThen { lat, lng ->
                if (lat != null && lng != null) {
                    scope.launch {
                        val address = reverseGeocodeWithBigDataCloud(context, lat, lng)
                        timeLogViewModel.startNewShift(jobId, lat, lng, address)
                    }
                } else {
                    timeLogViewModel.startNewShift(jobId, null, null, null)
                }
            }
        },
        onEndTimeLog = {
            fetchLocationAndThen { lat, lng ->
                if (lat != null && lng != null) {
                    scope.launch {
                        val address = reverseGeocodeWithBigDataCloud(context, lat, lng)
                        timeLogViewModel.endCurrentShift(lat, lng, address)
                    }
                } else {
                    timeLogViewModel.endCurrentShift(null, null, null)
                }
            }
        },
        onDeleteTimeLog = { log -> timeLogViewModel.deleteTimeLog(log) },
        navController = navController
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("UNUSED_PARAMETER")
@Composable
fun TimeLogScreen(
    timeLogs: List<TimeLogWithJob>,
    activeLog: TimeLogWithJob?,
    jobs: List<Job>,
    onStartTimeLog: (String) -> Unit,
    onEndTimeLog: () -> Unit,
    onDeleteTimeLog: (TimeLog) -> Unit,
    navController: NavHostController,
) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Time Logs") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (activeLog == null) {
                FloatingActionButton(onClick = { showDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Start New Shift")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (showDialog) {
                AddTimeLogDialog(
                    onDismiss = { showDialog = false },
                    onTimeLogAdd = {
                        onStartTimeLog(it)
                        showDialog = false
                    },
                    jobs = jobs,
                    navController = navController
                )
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(timeLogs) { logWithJob ->
                    TimeLogItem(
                        logWithJob = logWithJob,
                        onDelete = { onDeleteTimeLog(logWithJob.timeLog) },
                        onEndShift = { onEndTimeLog() }
                    )
                }
            }
        }
    }
}

@Composable
fun TimeLogItem(logWithJob: TimeLogWithJob, onDelete: () -> Unit, onEndShift: () -> Unit) {
    val formatter =
        remember { DateTimeFormatter.ofPattern("dd/MM HH:mm").withZone(ZoneId.systemDefault()) }
    val log = logWithJob.timeLog

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
            Column(modifier = Modifier.weight(1f)) {
                Text(text = logWithJob.jobName ?: "Unknown Job", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                log.startTime?.let { Text(text = "Started: ${formatter.format(it.toInstant())}") }
                // Show stored start address if present
                log.startAddress?.let { addr ->
                    Text(text = "Start location: $addr", style = MaterialTheme.typography.bodySmall)
                }

                if (log.endTime != null) {
                    log.endTime?.let { Text(text = "Ended:   ${formatter.format(it.toInstant())}") }
                    // Show stored end address if present
                    log.endAddress?.let { addr ->
                        Text(
                            text = "End location: $addr",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    log.duration?.let {
                        Text(text = "Duration: ${formatDuration(it)}")
                    }
                } else {
                    Text("Status: In Progress")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (log.endTime == null) {
                    Button(onClick = onEndShift) {
                        Text("End Shift")
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Log")
                }
            }
        }
    }
}

fun formatDuration(durationInMinutes: Long): String {
    val hours = durationInMinutes / 60
    val minutes = durationInMinutes % 60
    return String.format(Locale.US, "%d hours, %d minutes", hours, minutes)
}

@Composable
fun AddTimeLogDialog(
    jobViewModel: JobViewModel = Graph.jobViewModel,
    onDismiss: () -> Unit,
    onTimeLogAdd: (String) -> Unit,
    jobs: List<Job>,
    navController: NavHostController
) {
    var manualJobId by remember { mutableStateOf("") }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start New Shift") },
        text = {
            Column {
                if (jobs.isEmpty()) {
                    Text("No jobs available. Enter Job ID manually.")
                } else {
                    Text("Select a job:")
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn(
                        modifier = Modifier
                            .height(300.dp)
                    ) {
                        items(jobs) { job ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        // Make the selected job active, start the shift, then navigate to control
                                        jobViewModel.setActiveJob(job)
                                        onTimeLogAdd(job.id)
                                        onDismiss()
                                        navController.navigate("time_log_control")
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.fillMaxWidth()) {
                                        Text(text = job.name, fontWeight = FontWeight.Bold)
                                        Text(
                                            text = job.id,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Or enter job ID manually:")
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = manualJobId,
                    onValueChange = { manualJobId = it },
                    label = { Text("Job ID") },
                    placeholder = { Text("e.g., a-b-c-d") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (manualJobId.isNotBlank()) {
                        // Try to resolve manual ID to an existing job
                        val found = jobs.firstOrNull { it.id == manualJobId }
                        if (found != null) {
                            jobViewModel.setActiveJob(found)
                            onTimeLogAdd(manualJobId)
                            onDismiss()
                            navController.navigate("time_log_control")
                        } else {
                            // If not found, inform the user (they may need to create the job first)
                            Toast.makeText(context, "Job not found. Please create the job first.", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                enabled = manualJobId.isNotBlank()
            ) {
                Text("Start")
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
fun TimeLogScreenPreview() {
    val navController = rememberNavController()
    val sampleLogs = listOf(
        TimeLogWithJob(
            timeLog = TimeLog(
                id = "1",
                startTime = Date(System.currentTimeMillis() - 7200000),
                endTime = Date(System.currentTimeMillis() - 3600000),
                jobId = "1",
                duration = 60,
                userId = "1"
            ),
            jobName = "Android Developer"
        ),
        TimeLogWithJob(
            timeLog = TimeLog(
                id = "2",
                startTime = Date(),
                endTime = null,
                jobId = "2",
                duration = null,
                userId = "1"
            ),
            jobName = "UX Designer"
        )
    )

    val sampleJobs = listOf(
        Job(id = "1", userId = "1", name = "Android Developer", hourlyRate = 20.0),
        Job(id = "2", userId = "1", name = "UX Designer", hourlyRate = 18.0)
    )

    TimeLogScreen(
        timeLogs = sampleLogs,
        activeLog = sampleLogs.first { it.timeLog.endTime == null },
        jobs = sampleJobs,
        onStartTimeLog = {},
        onEndTimeLog = {},
        onDeleteTimeLog = {},
        navController = navController
    )
}
