package com.garent.s35123656.medtrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.garent.s35123656.medtrack.ui.theme.MedTrackTheme
import kotlinx.coroutines.launch

class AddMedication : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val patientId = intent.getStringExtra("PATIENT_ID") ?: ""
        setContent {
            MedTrackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AddMedication(
                        patientId = patientId,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun AddMedication(patientId: String, modifier: Modifier = Modifier) {
    // recorded variables
    var medName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var medTime by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Dropdowns option for frequency
    val frequencies = listOf("Once daily", "Twice daily", "Three times daily", "As needed")
    var freqExpanded by remember { mutableStateOf(false) }
    var selectedFreq by remember { mutableStateOf(frequencies[0]) }

    // Dropdown option for type
    val medTypes = listOf("Tablet", "Capsule", "Liquid", "Injection", "Topical", "Other")
    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(medTypes[0]) }

    // 1. Create the state that manages the snackbar
    val snackbarHostState = remember { SnackbarHostState() }

    // 2. Create the scope needed to launch the snackbar (since it's an async "wait" task)
    val scope = rememberCoroutineScope()

    // MAIN SCAFFOLD
    Scaffold(
        snackbarHost = { (SnackbarHost(snackbarHostState)) },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        // for maybe future horizontal screen integration purpose
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. Page header title ---
            item {
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    "Add Medication",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            // --- 2. Medication Name and Dosage Text input  ---
            item {
                OutlinedTextField(
                    value = medName,
                    onValueChange = { medName = it },
                    label = { Text("Medication Name *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("Dosage (e.g. 250mg) *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // --- 3. Frequency Dropdown Option ---
            item {
                Text("Frequency", style = MaterialTheme.typography.labelLarge)
                Box {
                    OutlinedCard(
                        onClick = { freqExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedFreq, modifier = Modifier.padding(16.dp))
                    }
                    DropdownMenu(
                        expanded = freqExpanded,
                        onDismissRequest = { freqExpanded = false }) {
                        frequencies.forEach { f ->
                            DropdownMenuItem(
                                text = { Text(f) },
                                onClick = { selectedFreq = f; freqExpanded = false })
                        }
                    }
                }
            }
            // --- 4. Time input ---
            item {
                OutlinedTextField(
                    value = medTime,
                    onValueChange = { medTime = it },
                    label = { Text("Time (e.g. 08:00) *") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // --- 5. Medication Type Dropdown ---
            item {
                Text("Medication Type", style = MaterialTheme.typography.labelLarge)
                Box {
                    OutlinedCard(
                        onClick = { typeExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedType, modifier = Modifier.padding(16.dp))
                    }
                    DropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }) {
                        medTypes.forEach { t ->
                            DropdownMenuItem(
                                text = { Text(t) },
                                onClick = { selectedType = t; typeExpanded = false })
                        }
                    }
                }
            }

            // --- 6. Optional Notes ---
            item {
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
            }

            // --- 7. Save and Clear button ---
            item {
                // 1. You MUST wrap them in a Row for 'weight' to work
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // Adds a nice gap between buttons
                ) {
                    // SAVE BUTTON
                    Button(
                        onClick = {
                            if (medName.isBlank() || dosage.isBlank() || medTime.isBlank()) {
                                scope.launch { snackbarHostState.showSnackbar("Please fill in all required fields!") }
                            } else {
                                scope.launch { snackbarHostState.showSnackbar("Medication Added: $medName") }
                                // Clear form after save
                                medName = ""; dosage = ""; medTime = ""; notes = ""
                            }
                        },
                        modifier = Modifier.weight(1f) // Takes up 50% width
                    ) {
                        Text("Save")
                    }

                    // CLEAR BUTTON
                    // Using OutlinedButton makes it look distinct from the primary Save action
                    Button(
                        onClick = {
                            medName = ""
                            dosage = ""
                            medTime = ""
                            notes = ""
                            selectedFreq = frequencies[0]
                            selectedType = medTypes[0]
                        },
                        modifier = Modifier.weight(1f) // Takes up the other 50% width
                    ) {
                        Text("Clear")
                    }
                }
            }
        }
    }
}

// FOR PREVIEW ONLYYY
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddMedicationPreview() {
    MedTrackTheme {
        // We pass a fake ID just to satisfy the function requirements
        AddMedication(patientId = "P1001")
    }
}

