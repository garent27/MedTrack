package com.garent.s35123656.medtrack

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.garent.s35123656.medtrack.data.entity.Symptom
import com.garent.s35123656.medtrack.data.viewModel.SymptomsViewModel
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Symptoms screen allowing users to log new symptoms and view history.
 * Original Feature Extension: Provides navigation to a dedicated Trend Chart screen.
 */
@Composable
fun Symptoms(
    patientId: String,
    viewModel: SymptomsViewModel,
    snackbarHostState: SnackbarHostState,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // UI state for the logging form
    val categories = listOf("Pain", "Nausea", "Dizziness", "Fatigue", "Headache", "Skin Reaction", "Other")
    
    val selectedCategory = viewModel.selectedCategory
    val severity = viewModel.severity
    val notes = viewModel.notes
    val dateTime = viewModel.dateTime
    val isExpanded = viewModel.isExpanded

    // Observe symptoms history from ViewModel
    val symptomList by viewModel.getSymptoms(patientId).collectAsState(initial = emptyList())

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
        ) {
            item {
                Text(
                    text = "Log Symptom",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Category", style = MaterialTheme.typography.labelLarge)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    OutlinedCard(
                        onClick = { viewModel.isExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = selectedCategory)
                            Icon(
                                painter = painterResource(id = android.R.drawable.arrow_down_float),
                                contentDescription = null
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = isExpanded,
                        onDismissRequest = { viewModel.isExpanded = false },
                        modifier = Modifier.fillMaxWidth(0.8f)
                    ) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    viewModel.selectedCategory = category
                                    viewModel.isExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val sliderColor = when {
                    severity < 4f -> Color(0xFF4CAF50) // Mild
                    severity < 7f -> Color(0xFFFFC107) // Moderate
                    else -> Color(0xFFF44336)          // Severe
                }

                Text(
                    "Severity: ${severity.toInt()}/10",
                    style = MaterialTheme.typography.labelLarge,
                    color = sliderColor,
                    fontWeight = FontWeight.Bold
                )

                Slider(
                    value = severity,
                    onValueChange = { viewModel.severity = it },
                    valueRange = 1f..10f,
                    steps = 8,
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = sliderColor,
                        activeTrackColor = sliderColor,
                        activeTickColor = Color.Transparent,
                        inactiveTrackColor = sliderColor.copy(alpha = 0.24f)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { viewModel.notes = it },
                    label = { Text("Additional notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )
                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Date & Time", style = MaterialTheme.typography.labelLarge)

                OutlinedButton(
                    onClick = {
                        showDateTimePicker(context) { pickedValue ->
                            viewModel.dateTime = pickedValue
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    Text(text = if (dateTime == "Select Date & Time") "Select Date & Time" else "TIME: $dateTime")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (selectedCategory == "Select Category") {
                            scope.launch { snackbarHostState.showSnackbar("Please select a category!") }
                        } else if (dateTime == "Select Date & Time") {
                            scope.launch { snackbarHostState.showSnackbar("When did this symptom occur?") }
                        } else {
                            val newSymptom = Symptom(
                                patientId = patientId,
                                category = selectedCategory,
                                severity = severity.toInt().toString(),
                                notes = notes,
                                dateTime = dateTime
                            )
                            viewModel.addSymptom(newSymptom) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Success: symptom saved ^-^")
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }

            // --- Original Feature Extension: Navigation Button ---
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { navController.navigate("symptom_trends") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("View Symptom Trends")
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Symptom History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (symptomList.isEmpty()) {
                item {
                    Text("No history found.", color = Color.Gray)
                }
            } else {
                items(symptomList) { symptom ->
                    SymptomCard(symptom)
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

fun showDateTimePicker(context: Context, onDateTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()
    DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val datePart = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    val timePart = String.format("%02d:%02d", hour, minute)
                    onDateTimeSelected("$datePart $timePart")
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                false
            ).show()
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    ).show()
}

@Composable
fun SymptomCard(symptom: Symptom) {
    val sevValue = symptom.severity.toIntOrNull() ?: 0
    val (label, statusColor) = when (sevValue) {
        in 1..3 -> "Mild" to Color(0xFF4CAF50)
        in 4..6 -> "Moderate" to Color(0xFFFFC107)
        in 7..10 -> "Severe" to Color(0xFFF44336)
        else -> "Unknown" to Color.Gray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = symptom.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, statusColor)
                ) {
                    Text(
                        text = "$label ($sevValue)",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Text(
                text = symptom.dateTime,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )
            if (symptom.notes != "N/A" && symptom.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = symptom.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
