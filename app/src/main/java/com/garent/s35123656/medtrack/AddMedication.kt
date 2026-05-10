package com.garent.s35123656.medtrack

import android.annotation.SuppressLint
import android.app.TimePickerDialog
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.garent.s35123656.medtrack.data.viewModel.AddMedicationViewModel
import com.garent.s35123656.medtrack.ui.theme.MedTrackTheme
import java.util.Calendar

@Composable
fun AddMedication(
    patientId: String,
    navController: NavController,
    viewModel: AddMedicationViewModel, // <-- USING THE NEW VIEWMODEL
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState
) {
    val context = LocalContext.current

    // UI state for form fields
    val medName = viewModel.medName
    val dosage = viewModel.dosage
    val scheduledTime = viewModel.scheduledTime
    val notes = viewModel.notes
    val selectedFreq = viewModel.selectedFreq
    val selectedType = viewModel.selectedType
    val showErrors = viewModel.showErrors

    val frequencies = listOf("Once daily", "Twice daily", "Three times daily", "As needed")
    val medTypes = listOf("Tablet", "Capsule", "Liquid", "Injection", "Topical", "Other")

    // --- NEW MVVM EVENT LISTENER ---
    LaunchedEffect(Unit) {
        viewModel.addMedResult.collect { success ->
            if (success) {
                Toast.makeText(context, "Success: Medication Added", Toast.LENGTH_SHORT).show()
                navController.popBackStack()
            } else {
                snackbarHostState.showSnackbar("Please fix errors above")
            }
        }
    }

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
                isError = showErrors && medName.isBlank(),
                supportingText =  {
                    if (showErrors && medName.isBlank()) {
                        Text("Medication name is required")
                    }
                }
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Note: The UI just checks for blanks now, the deep regex validation lives in the ViewModel!
            OutlinedTextField(
                value = dosage,
                onValueChange = { viewModel.dosage = it },
                label = { Text("Dosage (e.g. 250mg) *") },
                modifier = Modifier.fillMaxWidth(),
                isError = showErrors && dosage.isBlank(),
                supportingText = {
                    if (showErrors && dosage.isBlank()) {
                        Text(text = "Dosage is required", color = MaterialTheme.colorScheme.error)
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
                border = if (showErrors && scheduledTime.isBlank()) BorderStroke(1.dp, MaterialTheme.colorScheme.error) else ButtonDefaults.outlinedButtonBorder(enabled = true)
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

                // --- PURE MVVM BUTTON ---
                Button(
                    onClick = { viewModel.saveMedication(patientId) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save")
                }

                OutlinedButton(
                    onClick = { viewModel.clearForm() },
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