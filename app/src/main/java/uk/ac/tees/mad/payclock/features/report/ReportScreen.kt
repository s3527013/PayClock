package uk.ac.tees.mad.payclock.features.report

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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

/**
 * A route composable for the report screen.
 * This composable connects the [ReportViewModel] to the [ReportScreen].
 *
 * @param navController The navigation controller.
 */
@Composable
fun ReportScreenRoute(
    navController: NavHostController,
) {
    val reportViewModel: ReportViewModel = Graph.reportViewModel
    val jobs by reportViewModel.jobs.collectAsState()
    val selectedJobId by reportViewModel.selectedJobId.collectAsState()
    val selectedReportType by reportViewModel.selectedReportType.collectAsState()
    val timeSeriesReport by reportViewModel.timeSeriesReport.collectAsState()

    ReportScreen(
        jobs = jobs,
        selectedJobId = selectedJobId,
        selectedReportType = selectedReportType,
        timeSeriesReport = timeSeriesReport,
        onJobSelected = { reportViewModel.selectJob(it) },
        onReportTypeSelected = { reportViewModel.selectReportType(it) }
    )
}

/**
 * A composable function that displays the main report screen.
 *
 * @param jobs The list of jobs.
 * @param selectedJobId The ID of the selected job.
 * @param selectedReportType The selected report type.
 * @param timeSeriesReport The list of time series report items.
 * @param onJobSelected A callback that is invoked when a job is selected.
 * @param onReportTypeSelected A callback that is invoked when a report type is selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    jobs: List<Job>,
    selectedJobId: String?,
    selectedReportType: ReportType,
    timeSeriesReport: List<TimeSeriesReportItem>,
    onJobSelected: (String?) -> Unit,
    onReportTypeSelected: (ReportType) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            FilterControls(
                jobs = jobs,
                selectedJobId = selectedJobId,
                selectedReportType = selectedReportType,
                onJobSelected = onJobSelected,
                onReportTypeSelected = onReportTypeSelected
            )
            TimeSeriesReportList(reportItems = timeSeriesReport)
        }
    }
}

/**
 * A composable that displays filter controls for the report screen.
 *
 * @param jobs The list of jobs.
 * @param selectedJobId The ID of the selected job.
 * @param selectedReportType The selected report type.
 * @param onJobSelected A callback that is invoked when a job is selected.
 * @param onReportTypeSelected A callback that is invoked when a report type is selected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterControls(
    jobs: List<Job>,
    selectedJobId: String?,
    selectedReportType: ReportType,
    onJobSelected: (String?) -> Unit,
    onReportTypeSelected: (ReportType) -> Unit
) {
    var jobMenuExpanded by remember { mutableStateOf(false) }
    var reportTypeMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Job Filter
        ExposedDropdownMenuBox(
            expanded = jobMenuExpanded,
            onExpandedChange = { jobMenuExpanded = !jobMenuExpanded },
            modifier = Modifier.weight(1f)
        ) {
            TextField(
                value = jobs.find { it.id == selectedJobId }?.name ?: "All Jobs",
                onValueChange = {},
                readOnly = true,
                label = { Text("Job") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = jobMenuExpanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable), // Use the new overload

            )
            ExposedDropdownMenu(
                expanded = jobMenuExpanded,
                onDismissRequest = { jobMenuExpanded = false }
            ) {
                DropdownMenuItem(text = { Text("All Jobs") }, onClick = {
                    onJobSelected(null)
                    jobMenuExpanded = false
                })
                jobs.forEach {
                    DropdownMenuItem(text = { Text(it.name) }, onClick = {
                        onJobSelected(it.id)
                        jobMenuExpanded = false
                    })
                }
            }
        }

        // Report Type Filter
        ExposedDropdownMenuBox(
            expanded = reportTypeMenuExpanded,
            onExpandedChange = { reportTypeMenuExpanded = !reportTypeMenuExpanded },
            modifier = Modifier.weight(1f)
        ) {
            TextField(
                value = selectedReportType.name,
                onValueChange = {},
                readOnly = true,
                label = { Text("Period") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = reportTypeMenuExpanded) },
                modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
            )
            ExposedDropdownMenu(
                expanded = reportTypeMenuExpanded,
                onDismissRequest = { reportTypeMenuExpanded = false }
            ) {
                ReportType.entries.forEach {
                    DropdownMenuItem(text = { Text(it.name) }, onClick = {
                        onReportTypeSelected(it)
                        reportTypeMenuExpanded = false
                    })
                }
            }
        }
    }
}

/**
 * A composable that displays a list of time series report items.
 *
 * @param reportItems The list of report items to display.
 */
@Composable
fun TimeSeriesReportList(reportItems: List<TimeSeriesReportItem>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(reportItems) { item ->
            ReportCard(title = item.label, hours = item.totalHours, earnings = item.totalEarnings)
        }
    }
}

/**
 * A composable that displays a single report item in a card.
 *
 * @param title The title of the report item.
 * @param hours The total hours for this report item.
 * @param earnings The total earnings for this report item.
 */
@Composable
fun ReportCard(title: String, hours: Double, earnings: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Hours")
                Text("%.2f".format(hours))
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Total Earnings")
                Text("£%.2f".format(earnings))
            }
        }
    }
}

/**
 * A preview for the [ReportCard] composable.
 */
@Preview(showBackground = true, name = "Report Card Item")
@Composable
fun ReportCardPreview() {
    MaterialTheme {
        ReportCard(
            title = "October 2023",
            hours = 124.5,
            earnings = 1850.75
        )
    }
}

/**
 * A preview for the [ReportScreen] composable.
 */
@Preview(showBackground = true, name = "Full Report Screen")
@Composable
fun ReportScreenPreview() {
    // 1. Mock Data generation
    // Note: Adjust the Job constructor arguments to match your actual data class
    val sampleJobs = listOf(
        Job(id = "1", name = "Barista" /* hourlyRate = 12.0 */),
        Job(id = "2", name = "Web Dev" /* hourlyRate = 35.0 */)
    )

    val sampleReport = listOf(
        TimeSeriesReportItem(label = "Week 1", totalHours = 40.0, totalEarnings = 600.0),
        TimeSeriesReportItem(label = "Week 2", totalHours = 38.5, totalEarnings = 540.0),
        TimeSeriesReportItem(label = "Week 3", totalHours = 42.0, totalEarnings = 710.0)
    )

    // 2. Mock Enum (Assuming the first value available)
    val sampleType = ReportType.entries.firstOrNull() ?: ReportType.entries.toTypedArray()[0]

    MaterialTheme {
        ReportScreen(
            jobs = sampleJobs,
            selectedJobId = "1", // Simulate a selected job
            selectedReportType = sampleType,
            timeSeriesReport = sampleReport,
            onJobSelected = {},
            onReportTypeSelected = {}
        )
    }
}