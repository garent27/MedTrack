package com.garent.s35123656.medtrack

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garent.s35123656.medtrack.data.viewModel.ClinicianViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClinicianDashboard(
    viewModel: ClinicianViewModel,
    onBack: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.loadStatistics()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Clinician Dashboard", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (viewModel.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Text(
                        text = "Aggregate Statistics",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Real-time insights across all patients in the database.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                item {
                    StatCard(
                        title = "Total Patients",
                        value = "${viewModel.totalPatients}",
                        icon = Icons.Default.Group,
                        color = MaterialTheme.colorScheme.primaryContainer
                    )
                }

                item {
                    StatCard(
                        title = "Avg Meds per Patient",
                        value = String.format("%.1f", viewModel.avgMedsPerPatient),
                        icon = Icons.Default.Medication,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    )
                }

                item {
                    StatCard(
                        title = "Most Common Symptom",
                        value = viewModel.commonSymptom,
                        icon = Icons.Default.Warning,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }

                item {
                    StatCard(
                        title = "Avg Symptom Severity",
                        value = String.format("%.1f/10", viewModel.avgSymptomSeverity),
                        icon = Icons.Default.Analytics,
                        color = MaterialTheme.colorScheme.errorContainer
                    )
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: ImageVector, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(20.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.labelLarge)
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
