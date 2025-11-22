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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import uk.ac.tees.mad.payclock.core.Graph
import java.time.format.DateTimeFormatter

@Composable
fun ReportScreenRoute(
    navController: NavHostController,
) {
    val reportViewModel: ReportViewModel = Graph.reportViewModel
    val jobReports by reportViewModel.jobReports.collectAsState()
    val dailyReports by reportViewModel.dailyReports.collectAsState()

    ReportScreen(
        jobReports = jobReports,
        dailyReports = dailyReports,
        navController = navController
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(
    jobReports: List<JobReport>,
    dailyReports: List<DailyReport>,
    navController: NavHostController
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("By Job", "Daily")

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Reports") })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            PrimaryTabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }
            when (selectedTabIndex) {
                0 -> JobReportList(reports = jobReports)
                1 -> DailyReportList(reports = dailyReports)
            }
        }
    }
}

@Composable
fun JobReportList(reports: List<JobReport>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(reports) { report ->
            JobReportItem(report = report)
        }
    }
}

@Composable
fun DailyReportList(reports: List<DailyReport>) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(reports) { report ->
            DailyReportItem(report = report)
        }
    }
}


@Composable
fun JobReportItem(report: JobReport) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = report.jobName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Hours")
                Text("%.2f".format(report.totalHours))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Earnings")
                Text("£%.2f".format(report.totalEarnings))
            }
        }
    }
}

@Composable
fun DailyReportItem(report: DailyReport) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd MMMM yyyy") }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = report.date.format(formatter),
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Hours")
                Text("%.2f".format(report.totalHours))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Earnings")
                Text("£%.2f".format(report.totalEarnings))
            }
        }
    }
}
