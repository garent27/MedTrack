package com.garent.s35123656.medtrack

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.garent.s35123656.medtrack.ui.theme.MedTrackTheme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 *  Main Symptoms screen
 */
@Composable
fun Symptoms(
    patientId: String,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {

    val context = LocalContext.current

    // for pop up message
    val scope = rememberCoroutineScope()

    // List of all the details in a symptom card
    val categories = listOf("Pain", "Nausea", "Dizziness", "Fatigue", "Headache", "Skin Reaction", "Other")

    // Flag for user opened list of symptom
    var expanded by remember { mutableStateOf(false) }

    // State variables selected by user
    var selectedCategory by remember { mutableStateOf("Select Category") }
    var severity by remember { mutableFloatStateOf(5f)}
    var notes by remember { mutableStateOf("")}


    val dateTime = remember { mutableStateOf("Select Date & Time") }


    // State for the list
    var symptomList by remember { mutableStateOf(listOf<Symptom>()) }

    // Load list on first entry
    LaunchedEffect(Unit) {
        symptomList = getSymptomsForPatient(context, patientId)
    }

    // The Scaffold for the layout
    Box(modifier = modifier.fillMaxSize()) {
    // Use LazyColumn for the WHOLE screen instead of Column
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


            // 1. The Dropdown Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                // This OutlinedCard acts as the "Button" to open the menu
                OutlinedCard(
                    onClick = { expanded = true },
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

                // The actual Menu that pops up
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.fillMaxWidth(0.8f) // Optional: adjust width
                ) {
                    categories.forEach { category ->
                        DropdownMenuItem(
                            text = { Text(category) },
                            onClick = {
                                selectedCategory = category
                                expanded = false
                            }
                        )
                    }
                }
            }


            Spacer(modifier = Modifier.height(24.dp))



            // 2. The slider bar

            //slider colors
            val sliderColor = when {
                severity < 4f -> Color(0xFF4CAF50) // Green (Mild)
                severity < 7f -> Color(0xFFFFC107) // Amber (Moderate)
                else -> Color(0xFFF44336)          // Red (Severe)
            }


            Text(
                "Severity: ${severity.toInt()}/10",
                style = MaterialTheme.typography.labelLarge,
                color = sliderColor, // Optional: make the text color match!
                fontWeight = FontWeight.Bold
            )

            // Slider to set serverity
            Slider(
                value = severity,
                onValueChange = { severity = it },
                valueRange = 1f..10f,
                steps = 8,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = sliderColor,          // The circle handle
                    activeTrackColor = sliderColor,    // The line to the left of the thumb
                    activeTickColor = Color.Transparent, // hide tick marks for cleaner look
                    inactiveTrackColor = sliderColor.copy(alpha = 0.24f) // Faded version of the color
                )
            )


            Spacer(modifier = Modifier.height(24.dp))


            // 3. Additional text notes field
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Additional notes (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(modifier = Modifier.height(24.dp))


            // 4. Date and time picker
            Text(text = "Date & Time", style = MaterialTheme.typography.labelLarge)

            OutlinedButton(
                onClick = {
                    // Trigger the chained dialogs
                    showDateTimePicker(context) { pickedValue ->
                        dateTime.value = pickedValue
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                if (dateTime.value == "Select Date & Time") {
                    Text(text = "Select Date & Time")
                } else {
                    Text(text = "SCHEDULE: ${dateTime.value}")
                }
            }


            Spacer(modifier = Modifier.height(32.dp))


            // SAVE BUTTON
            Button(
                onClick = {
                    // if cateogry not selected
                    Log.d(
                        "Symp Debug",
                        "Save button clicked and selected category = $selectedCategory"
                    )

                    val currentDateTime = dateTime.value
                    if (selectedCategory == "Select Category") {
                        scope.launch { snackbarHostState.showSnackbar("Please select a category!") }
                    } else if (dateTime.value == "Select Date & Time") {
                        scope.launch { snackbarHostState.showSnackbar("when did this symptom occur?") }
                    }
                    // Note: severity is 10f from the slider, so it's always in range by design.
                    else {
                        // SUCCESS MESSAGE
                        scope.launch {
                            snackbarHostState.showSnackbar("Success: symptom saved ^-^")
                        }

                        // Clear form
                        selectedCategory = "Select Category"
                        severity = 5f
                        notes = ""
                        dateTime.value = "Select Date & Time"

                        Log.d("Symp Debug", "Form Cleared")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Save")
            }
        }

        // ============= BOTTOM SECTION: Symptom History =================
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
        }
    }
}


/**
 *  Function for Date time picker
 */
fun showDateTimePicker(context: Context, onDateTimeSelected: (String) -> Unit) {
    val calendar = Calendar.getInstance()

    // 1. Create the Date Picker
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val datePart = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)

            // 2. IMMEDIATELY create and show the Time Picker after date is picked
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    val timePart = String.format("%02d:%02d", hour, minute)
                    // 3. Send the final combined string back to the UI
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
    )

    datePickerDialog.show()
}


/**
 *  Symptom data class
 */
//data class Symptom(
//    val category: String,
//    val severity: String,
//    val notes: String,
//    val dateTime: String
//)


/**
 *  Getting symptoms from csv file
 */
fun getSymptomsForPatient(context: android.content.Context, targetId: String): List<Symptom> {
    val list = mutableListOf<Symptom>()
    try {
        //read file
        val file = context.assets.open("symptoms.csv")


        file.bufferedReader().useLines { lines ->

            lines.drop(1).forEach { line ->
                // split by comma
                val tokens = line.split(",")
                if (tokens.size >= 5 && tokens[0].trim() == targetId) {
                    list.add(Symptom(
                        category = tokens[1].trim(),
                        severity = tokens[2].trim(),
                        notes = tokens[3].trim(),
                        dateTime = tokens[4].trim()
                    ))
                }
            }
        }
    } catch (e: Exception) { e.printStackTrace() }

    // Sort by newest added
//    return list.reversed()

    // Sort by date
    return list.sortedByDescending { it.dateTime }
}



// The Card to hold symtomp detailo
@Composable
fun SymptomCard(symptom: Symptom) {
    // Convert string severity to Int (default to 0 if parsing fails)
    val sevValue = symptom.severity.toIntOrNull() ?: 0

    // Determine color and label based on the rules provided
    val (label, statusColor) = when (sevValue) {
        in 1..3 -> "Mild" to Color(0xFF4CAF50)      // Green
        in 4..6 -> "Moderate" to Color(0xFFFFC107)  // Amber/Yellow
        in 7..10 -> "Severe" to Color(0xFFF44336)   // Red
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

                // Color-coded Severity Label
                Surface(
                    color = statusColor.copy(alpha = 0.1f), // Light background
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(1.dp, statusColor) // Solid border
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

            // show date
            Text(
                text = symptom.dateTime,
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray
            )

            // show no notes if don't have
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


// FOR PREVIEW ONLYYY
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SymptomsPreview() {
    MedTrackTheme {
        val temp = remember { SnackbarHostState() }
        // We pass a fake ID just to satisfy the function requirements
        Symptoms(patientId = "P1001", snackbarHostState = temp)
    }
}