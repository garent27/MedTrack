package com.garent.s35123656.medtrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garent.s35123656.medtrack.data.viewModel.MedCoachViewModel

/**
 * MedCoach screen providing drug information from OpenFDA and placeholder for GenAI tips.
 */
@Composable
fun MedCoach(
    patientId: String,
    viewModel: MedCoachViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery = viewModel.searchQuery
    val drugInfo = viewModel.drugInfo
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val patientMeds = viewModel.patientMedications

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(patientId) {
        viewModel.loadPatientMedications(patientId)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "MedCoach",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Drug Information Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Drug Information (OpenFDA)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Search Row
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery = it },
                    label = { Text("Search Drug Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { viewModel.searchDrug(searchQuery) }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Dropdown for Patient Medications
                if (patientMeds.isNotEmpty()) {
                    Box {
                        OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Select From My Meds")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            patientMeds.forEach { med ->
                                DropdownMenuItem(
                                    text = { Text(med) },
                                    onClick = {
                                        viewModel.searchDrug(med)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Results / Loading / Errors
                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (drugInfo != null) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                DrugDetailItem("Purpose", drugInfo.purpose?.firstOrNull() ?: "N/A")
                                DrugDetailItem("Warnings", drugInfo.warnings?.firstOrNull() ?: "N/A")
                                DrugDetailItem("Dosage & Admin", drugInfo.dosage_and_administration?.firstOrNull() ?: "N/A")
                                DrugDetailItem("Indications", drugInfo.indications_and_usage?.firstOrNull() ?: "N/A")
                            }
                        }
                    } else {
                        Text(
                            text = "Enter a drug name above to see details.",
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- GenAI Tips Section (Bottom Half) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "GenAI Health Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "TODO: AI-generated personalized advice based on your health logs.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun DrugDetailItem(label: String, content: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = label,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodySmall,
            lineHeight = 18.sp
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}
