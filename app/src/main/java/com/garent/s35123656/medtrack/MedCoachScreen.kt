package com.garent.s35123656.medtrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.garent.s35123656.medtrack.data.entity.MedCoachTip
import com.garent.s35123656.medtrack.data.viewModel.MedCoachViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * MedCoach screen providing drug information from OpenFDA and GenAI medication tips.
 */
@Composable
fun MedCoach(
    patientId: String,
    viewModel: MedCoachViewModel,
    modifier: Modifier = Modifier
) {
    val searchQuery = viewModel.searchQuery
    val drugInfo = viewModel.drugInfo
    val isLoadingDrug = viewModel.isLoadingDrug
    val drugErrorMessage = viewModel.drugErrorMessage
    val patientMeds = viewModel.patientMedications

    val currentTip = viewModel.currentTip
    val isLoadingTip = viewModel.isLoadingTip
    val tipErrorMessage = viewModel.tipErrorMessage
    val tipHistory = viewModel.tipHistory
    val showHistoryDialog = viewModel.showHistoryDialog

    var expanded by remember { mutableStateOf(false) }

    LaunchedEffect(patientId) {
        viewModel.loadPatientMedications(patientId)
        viewModel.loadTipHistory(patientId)
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

        // --- Drug Information Section (Top Half) ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1.2f),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Drug Information (OpenFDA)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

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

                Box(modifier = Modifier.fillMaxSize()) {
                    if (isLoadingDrug) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (drugErrorMessage != null) {
                        Text(
                            text = drugErrorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
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
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GenAI Medication Tips",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    
                    IconButton(onClick = { viewModel.showHistoryDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Show All Tips",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (isLoadingTip) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (tipErrorMessage != null) {
                        Text(
                            text = tipErrorMessage,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    } else if (currentTip != null) {
                        Column(
                            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = currentTip,
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    } else {
                        Text(
                            text = "Tap the button below for a personalized tip!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.align(Alignment.Center),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.generateNewTip(patientId) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoadingTip
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Get Daily Tip")
                }
            }
        }
    }

    // History Dialog
    if (showHistoryDialog) {
        TipHistoryDialog(
            tips = tipHistory,
            onDismiss = { viewModel.showHistoryDialog = false }
        )
    }
}

@Composable
fun TipHistoryDialog(tips: List<MedCoachTip>, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Tip History",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                if (tips.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No tips generated yet.", color = Color.Gray)
                    }
                } else {
                    LazyColumn(modifier = Modifier.weight(1f)) {
                        items(tips) { tip ->
                            TipHistoryItem(tip)
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}

@Composable
fun TipHistoryItem(tip: MedCoachTip) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val dateString = dateFormat.format(Date(tip.timestamp))
    
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = dateString,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = tip.tipMessage,
            style = MaterialTheme.typography.bodyMedium
        )
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
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
