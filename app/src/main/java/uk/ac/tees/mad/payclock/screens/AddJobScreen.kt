package uk.ac.tees.mad.payclock.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import uk.ac.tees.mad.payclock.data.Job
import uk.ac.tees.mad.payclock.viewmodel.JobViewModel
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJobScreen(navController: NavController, jobViewModel: JobViewModel) {
    var jobTitle by remember { mutableStateOf("") }
    var hourlyRate by remember { mutableStateOf("") }
    var breakTimeInMinutes by remember { mutableStateOf("") }
    var isTitleEmpty by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Job") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Enter Job Details",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Job Title Input Field
            OutlinedTextField(
                value = jobTitle,
                onValueChange = {
                    jobTitle = it
                    isTitleEmpty = it.isBlank() // Check if the title is blank
                },
                label = { Text("Job Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = isTitleEmpty // Show error if title is empty
            )
            if (isTitleEmpty) {
                Text(
                    text = "Job title cannot be empty",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.error,
                    style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.Start)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hourly Rate Input Field
            OutlinedTextField(
                value = hourlyRate,
                onValueChange = { hourlyRate = it },
                label = { Text("Hourly Rate (£)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // breakTimeInMinutes Input Field
            OutlinedTextField(
                value = breakTimeInMinutes,
                onValueChange = { breakTimeInMinutes = it },
                label = { Text("Break Time (minutes)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(32.dp))


            // Save Button
            Button(
                onClick = {
                    // logic to save the job data using a ViewModel
                    // This is the logic you wanted to add
                    if (jobTitle.isNotBlank()) {
                        val newJob = Job(
                            id = Random.nextInt(),
                            name = jobTitle,
                            hourlyRate = hourlyRate.toDoubleOrNull() ?: 0.0,
                            breakTimeInMinutes = breakTimeInMinutes.toIntOrNull() ?: 0,
                        )
                        jobViewModel.addJob(newJob)
                        // pop back to the previous screen
                        navController.popBackStack()
                    } else {
                        // Set the error state to true to show the user
                        isTitleEmpty = true
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                // Disable the button if the title is blank
                enabled = jobTitle.isNotBlank()
            ) {
                Text("Save Job")
            }
        }
    }
}