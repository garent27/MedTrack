package com.garent.s35123656.medtrack

import android.annotation.SuppressLint
import android.app.Activity
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import com.garent.s35123656.medtrack.ui.theme.MedTrackTheme
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.core.content.edit
import kotlinx.coroutines.delay

class AddMedication : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val patientId = intent.getStringExtra("PATIENT_ID") ?: ""

        setContent {
            MedTrackTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = {SnackbarHost(snackbarHostState)}
                ) { innerPadding ->
                    AddMedication(
                        patientId = patientId,
                        modifier = Modifier.padding(innerPadding),
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}


/**
 *  Main Add Medication screen
 */
@Composable
fun AddMedication(patientId: String, modifier: Modifier = Modifier, snackbarHostState: SnackbarHostState) {
    val context = LocalContext.current

    // recorded variables
    var medName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    val timeState = remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    // Dropdowns option for frequency
    val frequencies = listOf("Once daily", "Twice daily", "Three times daily", "As needed")
    var freqExpanded by remember { mutableStateOf(false) }
    var selectedFreq by remember { mutableStateOf(frequencies[0]) }

    // Dropdown option for type
    val medTypes = listOf("Tablet", "Capsule", "Liquid", "Injection", "Topical", "Other")
    var typeExpanded by remember { mutableStateOf(false) }
    var selectedType by remember { mutableStateOf(medTypes[0]) }

    // Create the scope needed to launch the snackbar (since it's an async "wait" task)
    val scope = rememberCoroutineScope()

    // Track if the user has clicked "Save" to trigger error visibility
    var showErrors by remember { mutableStateOf(false) }

    // Validation Logic
    val dosageRegex = Regex("""^\d+(\.\d+)?(mg|ml|g)$""")
    val isNameValid = medName.isNotBlank()
    val isDosageValid = dosageRegex.matches(dosage.trim())
    val isTimeValid = timeState.value.isNotBlank()


    // Shared preference gson
    val gson = Gson()


    // for maybe future horizontal screen integration purpose
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
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
                modifier = Modifier.fillMaxWidth(),

                // inline validation handling
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
                onValueChange = { dosage = it },
                label = { Text("Dosage (e.g. 250mg) *") },
                modifier = Modifier.fillMaxWidth(),

                isError = showErrors && !isDosageValid,
                supportingText = {
                    if (showErrors && !isDosageValid) {
                        // Logic to switch the error message
                        val errorMessage = if (dosage.isBlank()) {
                            "Dosage is required"
                        } else {
                            "Format error: Use number + unit (e.g., 500mg, 10ml, 2g)"
                        }

                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
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
            Text(text = "Schedule Time", style = MaterialTheme.typography.labelLarge)

            OutlinedButton(
                onClick = {
                    // Call your function directly
                    showTimePicker(context, timeState)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),

                // Visual feedback: Red border if error
                border = if (showErrors && !isTimeValid)
                    BorderStroke(1.dp, MaterialTheme.colorScheme.error)
                else {
                    ButtonDefaults.outlinedButtonBorder(enabled = true)
                }
            ) {
                // Access the .value property of the MutableState
                Text(
                    text = if (timeState.value.isEmpty()) "Select Time"
                    else "SCHEDULE: ${timeState.value}"
                )
            }
            if (showErrors && !isTimeValid) {
                Text(
                    text = "Please select a time",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
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

        // helper to clear fields
        val clearFields = {
            medName = ""
            dosage = ""
            timeState.value = "" // Resetting the state object
            notes = ""
            selectedFreq = frequencies[0]
            selectedType = medTypes[0]

            showErrors = false
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
                        if (isNameValid && isDosageValid && isTimeValid) {

                            // 1. Initialize SharedPreferences
                            val sharedPref = context.getSharedPreferences("medications", Context.MODE_PRIVATE)

                            // 2. Create the Medication object using your specific fields
                            val newMedication = MedicationData(
                                medPetientID = patientId,
                                medicationName = medName,
                                dosage = dosage,
                                frequency = selectedFreq,
                                scheduledTime = timeState.value,
                                medicationType = selectedType,
                                notes = notes
                            )

                            // 3. Retrieve existing list for this patient
                            val existingJson = sharedPref.getString(patientId, null)
                            val listType = object : TypeToken<MutableList<MedicationData>>() {}.type

                            val medicationList: MutableList<MedicationData> = if (existingJson == null) {
                                mutableListOf()
                            } else {
                                gson.fromJson(existingJson, listType)
                            }

                            // 4. Add the new entry and save back to SP
                            medicationList.add(newMedication)
                            val updatedJson = gson.toJson(medicationList)

                            sharedPref.edit {
                                putString(patientId, updatedJson)
                            }


                            scope.launch {
                            snackbarHostState.showSnackbar("Success: Medication Added")
                            clearFields()
                            }

                            // wait then kill screen
                            scope.launch {
                                delay(900)
                                (context as? Activity)?.finish()
                            }

                        } else {
                            showErrors = true // Show all red messages
                            scope.launch { snackbarHostState.showSnackbar("Please fix errors above") }
                        }
                    },
                    modifier = Modifier.weight(1f) // Takes up 50% width
                ) {
                    Text("Save")
                }

                // CLEAR BUTTON
                OutlinedButton(
                    onClick = { clearFields() },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
            }
        }

        // back button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = {
                        // 1. (Optional) Log your data check
                        val checkData = context.getSharedPreferences("medications", Context.MODE_PRIVATE)
                            .getString(patientId, "Nothing found")
                        android.util.Log.d("SAVED_DATA", "Stored JSON: $checkData")

                        // 2. THE FIX: Close this activity
                        // This returns the user to the previous activity in the stack
                        (context as? Activity)?.finish()
                    }
                ) {
                    Text("Back to Home")
                }
            }

            // Remember your spacer so it's not touching the very edge!
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

/**
 * function for user selecting time
 */
@SuppressLint("DefaultLocale")
fun showTimePicker(mContext: Context, mTime: MutableState<String>) {
    val mCalendar = Calendar.getInstance()
    val mHour = mCalendar.get(Calendar.HOUR_OF_DAY)
    val mMinute = mCalendar.get(Calendar.MINUTE)

    val mTimePickerDialog = TimePickerDialog(
        mContext,
        { _, hour: Int, minute: Int ->
            // Formatting to ensure 08:05 instead of 8:5
            mTime.value = String.format("%02d:%02d", hour, minute)
        },
        mHour,
        mMinute,
        false
    )

    mTimePickerDialog.show()
}



// FOR PREVIEW ONLYYY
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun AddMedicationPreview() {
    MedTrackTheme {
        val dummySnackbarHostState = remember { SnackbarHostState() }
        // We pass a fake ID just to satisfy the function requirements
        AddMedication(patientId = "P1001",snackbarHostState = dummySnackbarHostState)
    }
}

