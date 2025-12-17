package uk.ac.tees.mad.payclock.features.timelog

import android.Manifest
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationServices
import java.time.Duration
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.delay
import uk.ac.tees.mad.payclock.core.Graph
import uk.ac.tees.mad.payclock.features.breaks.BreakViewModel
import uk.ac.tees.mad.payclock.features.jobs.JobViewModel

/**
 * A route composable for the time log control screen.
 * This composable connects the [JobViewModel], [TimeLogViewModel], and [BreakViewModel] to the [TimeLogControlScreen].
 *
 * @param navController The navigation controller.
 */
@Composable
fun TimeLogControlScreenRoute(
    navController: NavHostController,
) {
    val context = LocalContext.current
    val jobViewModel: JobViewModel = Graph.jobViewModel
    val timeLogViewModel: TimeLogViewModel = Graph.timeLogViewModel
    val breakViewModel: BreakViewModel = Graph.breakViewModel
    val job by jobViewModel.activeJob.collectAsState()

    val activeTimeLog by timeLogViewModel.activeTimeLog.collectAsState()
    val activeBreak by breakViewModel.activeBreak.collectAsState()

    val locationClient = LocationServices.getFusedLocationProviderClient(context)

    // Pending callback to be invoked once permission result and (optionally) location are available
    val pendingLocationCallback = remember { mutableStateOf<((lat: Double?, lng: Double?) -> Unit)?>(null) }

    // Permission launcher; when result arrives, we fetch location if granted, otherwise invoke with nulls
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
                            Toast.makeText(context, "Location unavailable; proceeding without location.", Toast.LENGTH_SHORT).show()
                            callback(null, null)
                        }
                    }.addOnFailureListener {
                        Toast.makeText(context, "Failed to get location; proceeding without location.", Toast.LENGTH_SHORT).show()
                        callback(null, null)
                    }
                } catch (_: SecurityException) {
                    // Defensive: permission was reported granted, but still security exception
                    callback(null, null)
                }
            } else {
                Toast.makeText(context, "Location permission denied; proceeding without location.", Toast.LENGTH_SHORT).show()
                callback(null, null)
            }

            // Clear pending callback after invocation
            pendingLocationCallback.value = null
        }
    )

    // Helper to request last known location and call a callback with coords (or nulls)
    fun fetchLocationAndThen(onResult: (lat: Double?, lng: Double?) -> Unit) {
        // Check permission; if not granted, request it and store the callback to be invoked later
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

        if (!hasPermission) {
            // Store callback and request permission; when the user responds, permissionLauncher's onResult will run
            pendingLocationCallback.value = onResult
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }

        // If we have permission, try to get last location now
        try {
            locationClient.lastLocation.addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onResult(location.latitude, location.longitude)
                } else {
                    Toast.makeText(context, "Location unavailable; proceeding without location.", Toast.LENGTH_SHORT).show()
                    onResult(null, null)
                }
            }.addOnFailureListener {
                Toast.makeText(context, "Failed to get location; proceeding without location.", Toast.LENGTH_SHORT).show()
                onResult(null, null)
            }
        } catch (_: SecurityException) {
            // Shouldn't happen because we checked permissions, but handle defensively
            onResult(null, null)
        }
    }


    // This is the primary state we care about for this screen
    val isThisJobActive = activeTimeLog?.timeLog?.jobId == job?.id

    // *** SOLUTION: Refined LaunchedEffect Logic ***
    LaunchedEffect(isThisJobActive, job) {
        if (job == null) {
            navController.popBackStack()
        }
    }

    job?.let { currentJob ->
        TimeLogControlScreen(
            jobName = currentJob.name,
            startTime = if (isThisJobActive) activeTimeLog?.timeLog?.startTime else null,
            isBreakActive = activeBreak != null,
            onStartClick = {
                fetchLocationAndThen { lat, lng ->
                    timeLogViewModel.startNewShift(currentJob.id, lat, lng)
                }
            },
            onStopClick = {
                fetchLocationAndThen { lat, lng ->
                    timeLogViewModel.endCurrentShift(lat, lng)
                    navController.navigate("jobs") {
                        popUpTo("jobs") {
                            inclusive = true
                        }
                    }
                }
            },
            onStartBreakClick = {
                activeTimeLog?.timeLog?.id?.let { timeLogId ->
                    breakViewModel.startNewBreak(timeLogId)
                }
            },
            onEndBreakClick = { breakViewModel.endCurrentBreak() }
        )
    }
}


/**
 * A composable function that displays the time log control screen.
 *
 * @param jobName The name of the job.
 * @param startTime The start time of the shift.
 * @param isBreakActive Whether a break is currently active.
 * @param onStartClick A callback that is invoked when the user clicks the "Start" button.
 * @param onStopClick A callback that is invoked when the user clicks the "Stop" button.
 * @param onStartBreakClick A callback that is invoked when the user clicks the "Start Unpaid Break" button.
 * @param onEndBreakClick A callback that is invoked when the user clicks the "End Break" button.
 */
@Composable
fun TimeLogControlScreen(
    jobName: String,
    startTime: Date?,
    isBreakActive: Boolean,
    onStartClick: () -> Unit,
    onStopClick: () -> Unit,
    onStartBreakClick: () -> Unit,
    onEndBreakClick: () -> Unit
) {
    var elapsedTime by remember { mutableStateOf(Duration.ZERO) }

    // This LaunchedEffect will re-launch if startTime changes (from null to a Date or vice-versa)
    LaunchedEffect(key1 = startTime) {
        if (startTime != null) {
            // Timer loop
            while (true) {
                val now = Date()
                elapsedTime = Duration.ofMillis(now.time - startTime.time)
                delay(1000)
            }
        } else {
            // If startTime is null, reset the timer display
            elapsedTime = Duration.ZERO
        }
    }

    Scaffold {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = jobName, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))

            if (startTime != null) {
                Text(text = "Shift In Progress", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(32.dp))
                Text(
                    text = formatDuration(elapsedTime),
                    style = MaterialTheme.typography.displayLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                if (isBreakActive) {
                    Button(onClick = onEndBreakClick) {
                        Text("End Break", style = MaterialTheme.typography.titleLarge)
                    }
                } else {
                    Button(onClick = onStartBreakClick) {
                        Text("Start Unpaid Break", style = MaterialTheme.typography.titleLarge)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onStopClick) {
                    Text("Stop", style = MaterialTheme.typography.titleLarge)
                }
            } else {
                Text(
                    text = "Ready to start your shift?",
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(onClick = onStartClick) {
                    Text("Start", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}

/**
 * Formats a [Duration] object into a string with the format HH:mm:ss.
 *
 * @param duration The [Duration] to format.
 * @return A string representation of the duration in HH:mm:ss format.
 */
private fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    val seconds = duration.seconds % 60
    return String.format(Locale.US, "%02d:%02d:%02d", hours, minutes, seconds)
}

@Preview(showBackground = true, name = "Shift Not Started")
@Composable
fun TimeLogControlScreenPreview_NotStarted() {
    TimeLogControlScreen(
        jobName = "Sample Job",
        startTime = null,
        isBreakActive = false,
        onStartClick = {},
        onStopClick = {},
        onStartBreakClick = {},
        onEndBreakClick = {}
    )
}

@Preview(showBackground = true, name = "Shift In Progress")
@Composable
fun TimeLogControlScreenPreview_InProgress() {
    TimeLogControlScreen(
        jobName = "Sample Job",
        startTime = Date(System.currentTimeMillis() - 3600 * 1000), // 1 hour ago
        isBreakActive = false,
        onStartClick = {},
        onStopClick = {},
        onStartBreakClick = {},
        onEndBreakClick = {}
    )
}

@Preview(showBackground = true, name = "On Break")
@Composable
fun TimeLogControlScreenPreview_OnBreak() {
    TimeLogControlScreen(
        jobName = "Sample Job",
        startTime = Date(System.currentTimeMillis() - 3600 * 2 * 1000), // 2 hours ago
        isBreakActive = true,
        onStartClick = {},
        onStopClick = {},
        onStartBreakClick = {},
        onEndBreakClick = {}
    )
}