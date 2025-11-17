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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import java.time.Duration
import java.util.Date

@Composable
fun ActiveTimeLogScreenRoute(
    navController: NavHostController,
    timeLogViewModel: TimeLogViewModel = viewModel(),
) {
    val activeTimeLog by timeLogViewModel.activeTimeLog.collectAsState()

    // When activeTimeLog becomes null (shift ends), this will trigger a popBackStack.
    if (activeTimeLog == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
    }

    activeTimeLog?.let { logWithJob ->
        ActiveTimeLogScreen(
            jobName = logWithJob.jobName ?: "Unknown Job",
            startTime = logWithJob.timeLog.startTime ?: Date(),
            onStopClick = {
                timeLogViewModel.endCurrentShift()
            }
        )
    }
}

@Composable
fun ActiveTimeLogScreen(
    jobName: String,
    startTime: Date,
    onStopClick: () -> Unit
) {
    var elapsedTime by remember { mutableStateOf(Duration.ZERO) }

    LaunchedEffect(key1 = startTime) {
        while (true) {
            val now = Date()
            elapsedTime = Duration.ofMillis(now.time - startTime.time)
            delay(1000)
        }
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = jobName, style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = "Shift In Progress", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = formatDuration(elapsedTime),
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(onClick = onStopClick) {
                Text("Stop", style = MaterialTheme.typography.titleLarge)
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