package uk.ac.tees.mad.payclock.features.timelog

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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import uk.ac.tees.mad.payclock.core.Graph
import uk.ac.tees.mad.payclock.features.breaks.BreakViewModel
import uk.ac.tees.mad.payclock.features.jobs.JobViewModel
import java.time.Duration
import java.util.Date

@Composable
fun TimeLogControlScreenRoute(
    navController: NavHostController,
) {
    val jobViewModel: JobViewModel = Graph.jobViewModel
    val timeLogViewModel: TimeLogViewModel = Graph.timeLogViewModel
    val breakViewModel: BreakViewModel = Graph.breakViewModel
    val job by jobViewModel.activeJob.collectAsState()

    val activeTimeLog by timeLogViewModel.activeTimeLog.collectAsState()
    val activeBreak by breakViewModel.activeBreak.collectAsState()


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
            onStartClick = { timeLogViewModel.startNewShift(currentJob.id) },
            onStopClick = {
                timeLogViewModel.endCurrentShift()
                navController.navigate("jobs") {
                    popUpTo("jobs") {
                        inclusive = true
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

private fun formatDuration(duration: Duration): String {
    val hours = duration.toHours()
    val minutes = duration.toMinutes() % 60
    val seconds = duration.seconds % 60
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}
