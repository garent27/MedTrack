package com.garent.s35123656.medtrack

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.garent.s35123656.medtrack.ui.theme.HomeScreen
import com.garent.s35123656.medtrack.ui.theme.MedTrackTheme

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.util.Calendar

class SymptomsScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val patientId = intent.getStringExtra("PATIENT_ID") ?: ""

        setContent {
            MedTrackTheme {
                Scaffold(
                    bottomBar = { MedTrackBottomBar(currentScreen = "Symptoms", patientId) }
                ) { innerPadding ->
                    // Pass the padding from Scaffold to your screen
                    Symptoms(
                        patientId = patientId,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
@Composable
fun Symptoms(patientId: String, modifier: Modifier = Modifier) {

    val context = LocalContext.current

    // for pop up message
    val snackbarHostState = remember { SnackbarHostState() }
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
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        // Use the modifier passed from the activity here
        modifier = modifier.fillMaxSize()
    ) { innerPadding -> // This 'innerPadding' contains the space occupied by the bars

// Use LazyColumn for the WHOLE screen instead of Column
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                // Note: We use padding(horizontal) here so the scrollbar
                // is at the edge of the screen, not 24dp away.
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
            Text(
                "Severity: ${severity.toInt()}/10",
                style = MaterialTheme.typography.labelLarge
            )
            Slider(
                value = severity,
                onValueChange = { severity = it },
                valueRange = 1f..10f,
                steps = 8, // This creates 9 intervals between 1 and 10 (total 10 positions)
                modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
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
                        scope.launch { snackbarHostState.showSnackbar("Please pick a date and time!") }
                    }
                    // Note: severity is 1f..10f from the slider, so it's always in range by design.
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
        item {
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTTOM SECTION: Symptom History ---
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

@Composable
fun TimePickerFun(mTime: MutableState<String>): TimePickerDialog {
    // Get the current context
    val mContext = LocalContext.current
    // Get a calendar instance
    val mCalendar = Calendar.getInstance()

    // Get the current hour and minute
    val mHour = mCalendar.get(Calendar.HOUR_OF_DAY)
    val mMinute = mCalendar.get(Calendar.MINUTE)

    // Set the calendar's time to the current time
    mCalendar.time = Calendar.getInstance().time

    // Return a TimePickerDialog
    return TimePickerDialog(
        mContext,
        { _, hour: Int, minute: Int ->
            // Update the state value directly
            // Format ensures 14:05 instead of 14:5
            mTime.value = String.format("%02d:%02d", hour, minute)
        },
        mHour,
        mMinute,
        false
    )
}

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

//fun saveSymptomToCSV(context: android.content.Context, id: String, category: String): Boolean {
//    return try {
//        // get file
//        val file = File(context.filesDir, "symptoms.csv")
//
//        // Open file in append mode
//        FileOutputStream(file, true).bufferedWriter().use { writer ->
//            writer.write("$id,$category,N/A,N/A,N/A")
//            writer.newLine()
//        }
//
//        // Write the data row with given data
//        Log.d("CSV_DEBUG", "Successfully wrote to: ${file.absolutePath}")
//        true
//
//    } catch (e: Exception) {
//        e.printStackTrace()
//        false
//    }
//}


data class Symptom(
    val category: String,
    val severity: String,
    val notes: String,
    val dateTime: String
)


// Getting symptoms from csv file
fun getSymptomsForPatient(context: android.content.Context, targetId: String): List<Symptom> {
    val list = mutableListOf<Symptom>()
    try {
        //read file
        val file = context.resources.openRawResource(R.raw.symptoms)


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

    // Sort by date: reverse the list so the newest symptom is at the top
    return list.reversed()
}



// The Card to hold symtomp detailo
@Composable
fun SymptomCard(symptom: Symptom) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = symptom.category, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Sev: ${symptom.severity}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Text(text = symptom.dateTime, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            if (symptom.notes != "N/A" && symptom.notes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = symptom.notes, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun MedTrackBottomBar(currentScreen: String, patientId: String) {
    val context = LocalContext.current
    NavigationBar {
        // Home Tab
        NavigationBarItem(
            selected = currentScreen == "Home",
            onClick = {
                if (currentScreen != "Home") {
                    val intent = Intent(context, HomeScreen::class.java)
                    intent.putExtra("PATIENT_ID", patientId) // <--- CRITICAL
                    context.startActivity(intent)
                }
            },
            label = { Text("Home") },
            icon = { Icon(painterResource(android.R.drawable.ic_menu_today), null) }
        )
        // Symptoms Tab
        NavigationBarItem(
            selected = currentScreen == "Symptoms",
            onClick = {
                if (currentScreen != "Symptoms") {
                    val intent = Intent(context, SymptomsScreen::class.java)
                    intent.putExtra("PATIENT_ID", patientId) // <--- CRITICAL
                    context.startActivity(intent)
                }
            },
            label = { Text("Symptoms") },
            icon = { Icon(painterResource(android.R.drawable.ic_dialog_alert), null) }
        )
    }
}

// FOR PREVIEW ONLYYY
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SymptomsPreview() {
    MedTrackTheme {
        // We pass a fake ID just to satisfy the function requirements
        Symptoms(patientId = "P1001")
    }
}