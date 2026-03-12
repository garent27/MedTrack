package com.garent.s35123656.medtrack

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

    // List of categories
    val categories = listOf("Pain", "Nausea", "Dizziness", "Fatigue", "Headache", "Skin Reaction", "Other")

    // State variables
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    // State for the list
    var symptomList by remember { mutableStateOf(listOf<Symptom>()) }

    // Load list on first entry
    LaunchedEffect(Unit) {
        symptomList = getSymptomsForPatient(context, patientId)
    }

    // 2. The Scaffold goes here
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        // Use the modifier passed from the activity here
        modifier = modifier.fillMaxSize()
    ) { innerPadding -> // This 'innerPadding' contains the space occupied by the bars
        Column(
        modifier = modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(24.dp)
    ) {
        Text(
            text = "Log Symptom",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Category", style = MaterialTheme.typography.labelLarge)

        // The Dropdown Container
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

        Spacer(modifier = Modifier.height(32.dp))


        // SAVE BUTTON
        Button(
            onClick = {
                // if cateogry not selected
                Log.d("Symp Debug", "Save button clicked and selected category = $selectedCategory")

                if (selectedCategory == "Select Category") {
                    scope.launch { snackbarHostState.showSnackbar("Please select a category first!") }
                } else {
                    Log.d("Symp Debug", "Trying to save it now ")
                    // save the symptom with pop up message
                    val success = saveSymptomToCSV(context, patientId, selectedCategory)
                    Log.d("Symp Debug", "Success value is = $success ")
                    if (success) {
                        scope.launch { snackbarHostState.showSnackbar("Saved: $selectedCategory") }
                    } else {
                        scope.launch { snackbarHostState.showSnackbar("Error saving to CSV") }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            // --- BOTTOM SECTION: Symptom History ---
            Text("Symptom History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)

            Spacer(modifier = Modifier.height(8.dp))

            if (symptomList.isEmpty()) {
                Text("No history found.", color = Color.Gray)
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(symptomList) { symptom ->
                        SymptomCard(symptom)
                    }
                }
            }
        }
    }
}

fun saveSymptomToCSV(context: android.content.Context, id: String, category: String): Boolean {
    return try {
        // get file
        val file = File(context.filesDir, "symptoms.csv")

        // Open file in append mode
        FileOutputStream(file, true).bufferedWriter().use { writer ->
            writer.write("$id,$category,N/A,N/A,N/A")
            writer.newLine()
        }

        // Write the data row with given data
        Log.d("CSV_DEBUG", "Successfully wrote to: ${file.absolutePath}")
        true

    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}


data class Symptom(
    val category: String,
    val severity: String,
    val notes: String,
    val dateTime: String
)


// Getting symptoms from csv file
fun getSymptomsForPatient(context: android.content.Context, targetId: String): List<Symptom> {
    val list = mutableListOf<Symptom>()
    val file = File(context.filesDir, "symptoms.csv")

    if (!file.exists()) return list

    try {
        file.bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val tokens = line.split(",")
                // tokens: 0=ID, 1=Category, 2=Severity, 3=Notes, 4=DateTime
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