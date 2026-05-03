package com.garent.s35123656.medtrack

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.garent.s35123656.medtrack.data.entity.Medication
import com.garent.s35123656.medtrack.data.viewModel.HomeViewModel
import com.garent.s35123656.medtrack.ui.theme.MedTrackTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Screen for adding a new medication.
 * Follows MVVM: Delegates data persistence to HomeViewModel.
 * UI state is managed by the ViewModel to survive configuration changes (e.g., rotation).
 */
@Composable
fun AddMedication(
    patientId: String,
    navController: NavController,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // UI state for form fields - Now managed by ViewModel
    val medName = viewModel.medName
    val dosage = viewModel.dosage
    val scheduledTime = viewModel.scheduledTime
    val notes = viewModel.notes
    val selectedFreq = viewModel.selectedFreq
    val selectedType = viewModel.selectedType
    val showErrors = viewModel.showErrors

    val frequencies = listOf("Once daily", "Twice daily", "Three times daily", "As needed")
    val medTypes = listOf("Tablet", "Capsule", "Liquid", "Injection", "Topical", "Other")

    // Validation
    val dosageRegex = Regex("""^\d+(\.\d+)?(mg|ml|g)$""")
    val isNameValid = medName.isNotBlank()
    val isDosageValid = dosageRegex.matches(dosage.trim())
    val isTimeValid = scheduledTime.isNotBlank()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                "Add Medication",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            OutlinedTextField(
                value = medName,
                onValueChange = { viewModel.medName = it },
                label = { Text("Medication Name *") },
                modifier = Modifier.fillMaxWidth(),
                isError = showErrors && !isNameValid,
                supportingText =  {
                    if (showErrors && !isNameValid) {
                        Text("Medication name is required")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = dosage,
                onValueChange = { viewModel.dosage = it },
                label = { Text("Dosage (e.g. 250mg) *") },
                modifier = Modifier.fillMaxWidth(),
                isError = showErrors && !isDosageValid,
                supportingText = {
                    if (showErrors && !isDosageValid) {
                        val errorMessage = if (dosage.isBlank()) "Dosage is required" else "Format error: Use number + unit"
                        Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
                    }
                }
            )
        }

        item {
            var freqExpanded by remember { mutableStateOf(false) }
            Text("Frequency", style = MaterialTheme.typography.labelLarge)
            Box {
                OutlinedCard(
                    onClick = { freqExpanded = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(selectedFreq, modifier = Modifier.padding(16.dp))
                }
                DropdownMenu(expanded = freqExpanded, onDismissRequest = { freqExpanded = false }) {
                    frequencies.forEach { f ->
                        DropdownMenuItem(
                            text = { Text(f) },
                            onClick = {
                                viewModel.selectedFreq = f
                                freqExpanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            Text(text = "Schedule Time", style = MaterialTheme.typography.labelLarge)
            OutlinedButton(
                onClick = {
                    showTimePicker(context) { pickedTime ->
                        viewModel.scheduledTime = pickedTime
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                border = if (showErrors && !isTimeValid) BorderStroke(1.dp, MaterialTheme.colorScheme.error) else ButtonDefaults.outlinedButtonBorder(enabled = true)
            ) {
                Text(text = if (scheduledTime.isEmpty()) "Select Time" else "SCHEDULE: $scheduledTime")
            }
        }

        item {
            var typeExpanded by remember { mutableStateOf(false) }
            Text("Medication Type", style = MaterialTheme.typography.labelLarge)
            Box {
                OutlinedCard(onClick = { typeExpanded = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(selectedType, modifier = Modifier.padding(16.dp))
                }
                DropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                    medTypes.forEach { t ->
                        DropdownMenuItem(
                            text = { Text(t) },
                            onClick = {
                                viewModel.selectedType = t
                                typeExpanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = notes,
                onValueChange = { viewModel.notes = it },
                label = { Text("Notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = {
                        if (isNameValid && isDosageValid && isTimeValid) {
                            val newMedication = Medication(
                                patientId = patientId,
                                medicationName = medName,
                                dosage = dosage,
                                frequency = selectedFreq,
                                scheduledTime = scheduledTime,
                                medicationType = selectedType,
                                notes = notes,
                                isTaken = false
                            )
                            // Use ViewModel to add medication
                            viewModel.addMedication(newMedication) {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Success: Medication Added")
                                    delay(900)
                                    navController.popBackStack()
                                }
                            }
                        } else {
                            viewModel.showErrors = true
                            scope.launch { snackbarHostState.showSnackbar("Please fix errors above") }
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }

                OutlinedButton(
                    onClick = { viewModel.clearAddMedicationForm() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                OutlinedButton(onClick = { navController.popBackStack() }) {
                    Text("Back to Home")
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@SuppressLint("DefaultLocale")
fun showTimePicker(mContext: Context, onTimePicked: (String) -> Unit) {
    val mCalendar = Calendar.getInstance()
    TimePickerDialog(
        mContext,
        { _, hour: Int, minute: Int -> onTimePicked(String.format("%02d:%02d", hour, minute)) },
        mCalendar.get(Calendar.HOUR_OF_DAY),
        mCalendar.get(Calendar.MINUTE),
        false
    ).show()
}

@Preview(showBackground = true)
@Composable
fun AddMedicationPreview() {
    MedTrackTheme {
        // Preview logic
    }
}