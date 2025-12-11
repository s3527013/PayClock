@file:Suppress("ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE", "UNUSED_VARIABLE", "UNUSED_VALUE")

package uk.ac.tees.mad.payclock.features.timelog

import android.Manifest
import android.location.Location
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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
import java.time.Instant
import java.time.LocalDate
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
    val activeTimeLog by timeLogViewModel.activeTimeLog.collectAsState()
    val jobsViewModel = Graph.jobViewModel
    val jobs by jobsViewModel.jobs.collectAsState()
    val filteredLogs by timeLogViewModel.filteredTimeLogs.collectAsState()
    val scope = rememberCoroutineScope()

    // Location client and permission handling (pending callback pattern)
    val locationClient = LocationServices.getFusedLocationProviderClient(context)
    val pendingLocationCallback =
        remember { mutableStateOf<((lat: Double?, lng: Double?) -> Unit)?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(), onResult = { isGranted: Boolean ->
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
        })

    fun fetchLocationAndThen(onResult: (lat: Double?, lng: Double?) -> Unit) {
        val hasPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
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
        timeLogs = filteredLogs,
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
                    timeLogViewModel.endCurrentShift(null, null)
                }
            }
        },
        onDeleteTimeLog = { log -> timeLogViewModel.deleteTimeLog(log) },
        navController = navController,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
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
    val showDialogState = remember { mutableStateOf(false) }
    val showStartDatePicker = remember { mutableStateOf(false) }
    val showEndDatePicker = remember { mutableStateOf(false) }

    // Filter toggle state
    var filterExpanded by remember { mutableStateOf(false) }

    // Date range filter state
    var startDateInput by remember { mutableStateOf("") }
    var endDateInput by remember { mutableStateOf("") }

    // Selected dates for DatePicker
    var selectedStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var selectedEndDate by remember { mutableStateOf<LocalDate?>(null) }

    // Track if we should apply filters
    var filtersActive by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Date formatters
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val displayFormatter = DateTimeFormatter.ofPattern("dd/MM HH:mm")

    // Helper function to convert LocalDate to milliseconds
    fun localDateToMillis(ld: LocalDate, startOfDay: Boolean): Long {
        val z = ZoneId.systemDefault()
        return if (startOfDay) {
            ld.atStartOfDay(z).toInstant().toEpochMilli()
        } else {
            ld.plusDays(1).atStartOfDay(z).toInstant().toEpochMilli()
        }
    }

    // Function to apply filters
    fun applyFilters() {
        try {
            // Use the selected dates directly
            val startDate = selectedStartDate
            val endDate = selectedEndDate

            // Update the text inputs based on selected dates
            startDateInput = startDate?.format(dateFormatter) ?: ""
            endDateInput = endDate?.format(dateFormatter) ?: ""

            // Validate date range
            if (startDate != null && endDate != null && startDate.isAfter(endDate)) {
                scope.launch {
                    snackbarHostState.showSnackbar("Start date must be before end date")
                }
                filtersActive = false
                return
            }

            filtersActive = startDate != null || endDate != null

            // Show matched count
            if (filtersActive) {
                val matched = timeLogs.count { tlw ->
                    val startTimeMillis = tlw.timeLog.startTime?.time ?: return@count false
                    val afterStart =
                        startDate?.let { startTimeMillis >= localDateToMillis(it, true) } ?: true
                    val beforeEnd =
                        endDate?.let { startTimeMillis <= localDateToMillis(it, false) } ?: true
                    afterStart && beforeEnd
                }
                scope.launch {
                    snackbarHostState.showSnackbar("$matched logs matched")
                }
            }
        } catch (e: Exception) {
            scope.launch {
                snackbarHostState.showSnackbar("Invalid date format")
            }
            filtersActive = false
        }
    }

    // Function to clear filters
    fun clearFilters() {
        startDateInput = ""
        endDateInput = ""
        selectedStartDate = null
        selectedEndDate = null
        filtersActive = false
        scope.launch {
            snackbarHostState.showSnackbar("Filters cleared")
        }
    }

    // Function to handle date selection
    fun onStartDateSelected(date: LocalDate) {
        selectedStartDate = date
        showStartDatePicker.value = false
        applyFilters()
    }

    fun onEndDateSelected(date: LocalDate) {
        selectedEndDate = date
        showEndDatePicker.value = false
        applyFilters()
    }

    // Filter logs
    val displayedLogs = remember(timeLogs, selectedStartDate, selectedEndDate, filtersActive) {
        if (!filtersActive) {
            timeLogs
        } else {
            timeLogs.filter { tlw ->
                val startTimeMillis = tlw.timeLog.startTime?.time ?: return@filter false
                val afterStart =
                    selectedStartDate?.let { startTimeMillis >= localDateToMillis(it, true) }
                        ?: true
                val beforeEnd =
                    selectedEndDate?.let { startTimeMillis <= localDateToMillis(it, false) } ?: true
                afterStart && beforeEnd
            }
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Time Logs") }, colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                titleContentColor = MaterialTheme.colorScheme.primary
            ), actions = {
                // Filter indicator in toolbar
                if (filtersActive) {
                    Badge(
                        modifier = Modifier.padding(end = 8.dp),
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    ) {
                        Text("Filtered")
                    }
                }
            })
    }, floatingActionButton = {
        if (activeLog == null) {
            FloatingActionButton(onClick = { showDialogState.value = true }) {
                Icon(Icons.Default.Add, contentDescription = "Start New Shift")
            }
        }
    }, snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filter toggle and controls
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize()
                ) {
                    // Toggle header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Filter by Date",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )

                            // Show active filter badge
                            if (filtersActive) {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                    contentColor = MaterialTheme.colorScheme.secondary
                                ) {
                                    Text(
                                        text = "Active", style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }
                        }

                        // Toggle button
                        IconButton(
                            onClick = { filterExpanded = !filterExpanded },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (filterExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (filterExpanded) "Hide filters" else "Show filters",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Filter controls (collapsible)
                    if (filterExpanded) {
                        HorizontalDivider(
                            modifier = Modifier.fillMaxWidth(),
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Date inputs side by side
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                // Start Date Field
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = startDateInput,
                                        onValueChange = {
                                            // Don't allow manual editing, only through date picker
                                        },
                                        label = { Text("Start Date") },
                                        placeholder = { Text("Select start date") },
                                        singleLine = true,
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { showStartDatePicker.value = true },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CalendarToday,
                                                    contentDescription = "Pick Start Date",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            focusedLabelColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = MaterialTheme.shapes.small
                                    )
                                    Text(
                                        text = "Tap to select",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                    )
                                }

                                // End Date Field
                                Column(
                                    modifier = Modifier.weight(1f)
                                ) {
                                    OutlinedTextField(
                                        value = endDateInput,
                                        onValueChange = {
                                            // Don't allow manual editing, only through date picker
                                        },
                                        label = { Text("End Date") },
                                        placeholder = { Text("Select end date") },
                                        singleLine = true,
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth(),
                                        trailingIcon = {
                                            IconButton(
                                                onClick = { showEndDatePicker.value = true },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.CalendarToday,
                                                    contentDescription = "Pick End Date",
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        },
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                                            focusedLabelColor = MaterialTheme.colorScheme.primary
                                        ),
                                        shape = MaterialTheme.shapes.small
                                    )
                                    Text(
                                        text = "Tap to select",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Selected dates summary when filters are active
                            if (filtersActive) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            color = MaterialTheme.colorScheme.secondaryContainer.copy(
                                                alpha = 0.3f
                                            ), shape = MaterialTheme.shapes.small
                                        )
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(style = SpanStyle(fontWeight = FontWeight.Medium)) {
                                                append("Filtering by: ")
                                            }
                                            if (selectedStartDate != null && selectedEndDate != null) {
                                                append(
                                                    "${selectedStartDate!!.format(dateFormatter)} to ${
                                                        selectedEndDate!!.format(
                                                            dateFormatter
                                                        )
                                                    }"
                                                )
                                            } else if (selectedStartDate != null) {
                                                append(
                                                    "From ${
                                                        selectedStartDate!!.format(
                                                            dateFormatter
                                                        )
                                                    }"
                                                )
                                            } else if (selectedEndDate != null) {
                                                append(
                                                    "Until ${
                                                        selectedEndDate!!.format(
                                                            dateFormatter
                                                        )
                                                    }"
                                                )
                                            }
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )

                                    IconButton(
                                        onClick = ::clearFilters, modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear filters",
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                            }

                            // Filter action buttons
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Button(
                                    onClick = ::clearFilters,
                                    enabled = filtersActive,
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Clear")
                                    }
                                }

                                Button(
                                    onClick = ::applyFilters,
                                    enabled = selectedStartDate != null || selectedEndDate != null,
                                    modifier = Modifier.weight(1f),
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Apply")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Current shift indicator
            activeLog?.let { log ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Currently Clocked In",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Column {
                            Text(
                                text = "Job: ${log.jobName ?: "Unknown"}",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Started: ${displayFormatter.format(log.timeLog.startTime!!.toInstant())}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            log.timeLog.startAddress?.let { addr ->
                                Text(
                                    text = "Location: ${addr.take(40)}${if (addr.length > 40) "..." else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = onEndTimeLog,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondary,
                                contentColor = MaterialTheme.colorScheme.onSecondary
                            )
                        ) {
                            Text("End Shift")
                        }
                    }
                }
            }

            // Time logs list header
            if (displayedLogs.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (filtersActive) "Filtered Logs" else "All Time Logs",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (filtersActive) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                                        append("Showing ")
                                    }
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
                                        append("${displayedLogs.size}")
                                    }
                                    withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
                                        append(" of ${timeLogs.size} logs")
                                    }
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Text(
                        text = "${displayedLogs.size} ${if (displayedLogs.size == 1) "entry" else "entries"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Time logs list
            if (displayedLogs.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (filtersActive) "No logs match your filters" else "No time logs yet",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (filtersActive) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Try adjusting your date range",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = ::clearFilters, colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("Clear Filters")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(displayedLogs) { logWithJob ->
                        TimeLogItem(
                            logWithJob = logWithJob,
                            onDelete = { onDeleteTimeLog(logWithJob.timeLog) },
                            onEndShift = onEndTimeLog
                        )
                    }
                }
            }
        }

        // Dialog for starting new shift
        if (showDialogState.value) {
            AddTimeLogDialog(
                onDismiss = { showDialogState.value = false }, onTimeLogAdd = {
                    onStartTimeLog(it)
                    showDialogState.value = false
                }, jobs = jobs, navController = navController
            )
        }

        // Date picker dialogs
        if (showStartDatePicker.value) {
            DatePickerDialog(
                onDismissRequest = { showStartDatePicker.value = false },
                onDateSelected = { date ->
                    onStartDateSelected(date)
                },
                initialDate = selectedStartDate ?: LocalDate.now(),
                title = "Select Start Date"
            )
        }

        if (showEndDatePicker.value) {
            DatePickerDialog(
                onDismissRequest = { showEndDatePicker.value = false },
                onDateSelected = { date ->
                    onEndDateSelected(date)
                },
                initialDate = selectedEndDate ?: selectedStartDate ?: LocalDate.now(),
                title = "Select End Date"
            )
        }
    }
}

// DatePickerDialog Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate = LocalDate.now(),
    title: String = "Select Date"
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
            .toEpochMilli()
    )

    AlertDialog(onDismissRequest = onDismissRequest, title = { Text(text = title) }, text = {
        DatePicker(
            state = datePickerState, title = null, showModeToggle = false
        )
    }, confirmButton = {
        TextButton(
            onClick = {
                datePickerState.selectedDateMillis?.let { millis ->
                    val date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    onDateSelected(date)
                }
            }) {
            Text("OK")
        }
    }, dismissButton = {
        TextButton(onClick = onDismissRequest) {
            Text("Cancel")
        }
    })
}

@Composable
fun TimeLogItem(logWithJob: TimeLogWithJob, onDelete: () -> Unit, onEndShift: () -> Unit) {
    val formatter =
        remember { DateTimeFormatter.ofPattern("dd/MM HH:mm").withZone(ZoneId.systemDefault()) }
    val log = logWithJob.timeLog

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = logWithJob.jobName ?: "Unknown Job", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(2.dp))
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
                            text = "End location: $addr", style = MaterialTheme.typography.bodySmall
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

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Start New Shift") }, text = {
        Column {
            if (jobs.isEmpty()) {
                Text("No jobs available. Enter Job ID manually.")
            } else {
                Text("Select a job:")
                Spacer(modifier = Modifier.height(4.dp))
                LazyColumn(
                    modifier = Modifier.height(150.dp)
                ) {
                    items(jobs) { job ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Button(
                                onClick = {
                                    // Make the selected job active, start the shift, then navigate to control
                                    jobViewModel.setActiveJob(job)
                                    onTimeLogAdd(job.id)
                                    onDismiss()
                                    navController.navigate("time_log_control")
                                }, modifier = Modifier.fillMaxWidth()
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

            Spacer(modifier = Modifier.height(6.dp))
            Text("Or enter job ID manually:")
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = manualJobId,
                onValueChange = { manualJobId = it },
                label = { Text("Job ID") },
                placeholder = { Text("e.g., a-b-c-d") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }, confirmButton = {
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
                        Toast.makeText(
                            context,
                            "Job not found. Please create the job first.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }, enabled = manualJobId.isNotBlank()
        ) {
            Text("Start")
        }
    }, dismissButton = {
        Button(onClick = onDismiss) {
            Text("Cancel")
        }
    })
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
            ), jobName = "Android Developer"
        ), TimeLogWithJob(
            timeLog = TimeLog(
                id = "2",
                startTime = Date(),
                endTime = null,
                jobId = "2",
                duration = null,
                userId = "1"
            ), jobName = "UX Designer"
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
        navController = navController,
    )
}
