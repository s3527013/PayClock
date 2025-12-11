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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import uk.ac.tees.mad.payclock.core.Graph
import uk.ac.tees.mad.payclock.features.jobs.data.Job

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
        onReportTypeSelected = { reportViewModel.selectReportType(it) },
        navController = navController
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    jobs: List<Job>,
    selectedJobId: String?,
    selectedReportType: ReportType,
    timeSeriesReport: List<TimeSeriesReportItem>,
    onJobSelected: (String?) -> Unit,
    onReportTypeSelected: (ReportType) -> Unit,
    navController: NavHostController
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
                ReportType.values().forEach {
                    DropdownMenuItem(text = { Text(it.name) }, onClick = {
                        onReportTypeSelected(it)
                        reportTypeMenuExpanded = false
                    })
                }
            }
        }
    }
}

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
