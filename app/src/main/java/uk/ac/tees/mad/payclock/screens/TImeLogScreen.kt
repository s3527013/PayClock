package uk.ac.tees.mad.payclock.screens

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
import uk.ac.tees.mad.payclock.data.models.TimeLog
import uk.ac.tees.mad.payclock.data.models.TimeLogWithJob
import uk.ac.tees.mad.payclock.viewmodel.TimeLogViewModel
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TimeLogScreenRoute(
    navController: NavHostController,
    timeLogViewModel: TimeLogViewModel = viewModel()
) {
    val allTimeLogs by timeLogViewModel.allTimeLogs.collectAsState()
    val activeTimeLog by timeLogViewModel.activeTimeLog.collectAsState()

    TimeLogScreen(
        timeLogs = allTimeLogs,
        activeLog = activeTimeLog,
        onStartTimeLog = { jobId -> timeLogViewModel.startNewShift(jobId) }, // The ViewModel now handles the user ID
        onEndTimeLog = { timeLogViewModel.endCurrentShift() },
        onDeleteTimeLog = { log -> timeLogViewModel.deleteTimeLog(log) },
        navController = navController
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimeLogScreen(
    timeLogs: List<TimeLogWithJob>,
    activeLog: TimeLogWithJob?,
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
                    }
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
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM HH:mm").withZone(ZoneId.systemDefault()) }
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
                Text(text = "Started: ${formatter.format(log.startTime)}")

                if (log.endTime != null) {
                    Text(text = "Ended:   ${formatter.format(log.endTime)}")
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

fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    return String.format("%d hours, %d minutes", hours, minutes)
}

@Composable
fun AddTimeLogDialog(onDismiss: () -> Unit, onTimeLogAdd: (String) -> Unit) {
    var jobId by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Start New Shift") },
        text = {
            OutlinedTextField(
                value = jobId,
                onValueChange = { jobId = it },
                label = { Text("Enter Job ID") },
                placeholder = { Text("e.g., a-b-c-d") }
            )
        },
        confirmButton = {
            Button(
                onClick = { onTimeLogAdd(jobId) },
                enabled = jobId.isNotBlank()
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
                startTime = Instant.now().minusSeconds(7200),
                endTime = Instant.now().minusSeconds(3600),
                jobId = "1",
                workBreak = emptyList(),
                duration = Duration.ofHours(1),
                userId = "1"
            ),
            jobName = "Android Developer"
        ),
        TimeLogWithJob(
            timeLog = TimeLog(
                id = "2",
                startTime = Instant.now(),
                endTime = null,
                jobId = "2",
                workBreak = emptyList(),
                duration = null,
                userId = "1"
            ),
            jobName = "UX Designer"
        )
    )

    TimeLogScreen(
        timeLogs = sampleLogs,
        activeLog = sampleLogs.first { it.timeLog.endTime == null },
        onStartTimeLog = {},
        onEndTimeLog = {},
        onDeleteTimeLog = {},
        navController = navController
    )
}
