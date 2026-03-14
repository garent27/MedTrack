package com.garent.s35123656.medtrack.ui.theme

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.R
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garent.s35123656.medtrack.ui.theme.ui.theme.MedTrackTheme
import java.io.BufferedReader
import java.io.InputStreamReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.garent.s35123656.medtrack.AddMedication
import com.garent.s35123656.medtrack.SymptomsScreen
import kotlin.jvm.java


// for navigation to different screen
sealed class Screen(val label: String, val iconId: Int) {
    object Home : Screen("Home", android.R.drawable.ic_menu_today)
    object Symptoms : Screen("Symptoms", android.R.drawable.ic_dialog_alert)
}

class HomeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val patientId = intent.getStringExtra("PATIENT_ID") ?: ""

        setContent {
            MedTrackTheme {
                Scaffold(
                    bottomBar = { MedTrackBottomBar(currentScreen =  "Home", patientId) }
                ) { innerPadding ->
                    // Just call Home directly
                    Home(patientId = patientId, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}


@Composable
fun Home(patientId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // 1. Data State Management
    var patientName by remember { mutableStateOf("Patient") }
    var medicationList by remember { mutableStateOf(listOf<Medication>()) }

    // Load data once when patientId is available
    LaunchedEffect(patientId) {
        patientName = getPatientName(context, patientId)
        medicationList = getMedicationsForPatient(context, patientId)
    }

    // 2. Calculations for UI
    val calendar = Calendar.getInstance().time
    val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(calendar)

    val totalMeds = medicationList.size
    val takenMeds = medicationList.count { it.isTaken }

    // The Scaffold Structure
    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    // Navigates to your AddMedication Activity
                    context.startActivity(Intent(context, AddMedication::class.java))
                },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                // Using a larger font size for the "+" icon
                Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
            }
        },
        floatingActionButtonPosition = FabPosition.End // Standard bottom-right position
    ) { innerPadding ->

        // Main content layout inside the Scaffold's padding
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(24.dp) // Your custom internal spacing
        ) {
            // display user name
            Text(
                text = "Hello, $patientName",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // display current date
            Text(
                text = currentDate,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(32.dp))

            // your medication text
            Text(
                text = "Your Medications",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(12.dp))


            // checks if current patient have medication or not
            if (medicationList.isEmpty()) {
                // This shows if the CSV search returned no results for this ID
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // Take up the remaining space
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No medications scheduled.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            } else {
                // 2. Normal View if medications exist
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$takenMeds of $totalMeds medications taken today",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(medicationList) { med ->
                        MedicationCard(
                            med = med,
                            onToggleTaken = { isChecked ->
                                medicationList = medicationList.map { currentMed ->
                                    if (currentMed.name == med.name && currentMed.scheduledTime == med.scheduledTime) {
                                        currentMed.copy(isTaken = isChecked)
                                    } else {
                                        currentMed
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

fun getPatientName(context: android.content.Context, id:String): String{
    return try {
        val inputStream = context.resources.openRawResource(com.garent.s35123656.medtrack.R.raw.patients)
        val reader = BufferedReader(InputStreamReader(inputStream))
        var nameFound = "Patient"

        reader.useLines { lines ->
            lines.forEach { line ->
                val tokens = line.split(",")
                if (tokens.isNotEmpty() && tokens[0].trim() == id) {
                    nameFound = tokens[2].trim()
                    return@useLines
                }
            }
        }
        nameFound
    }   catch (e: Exception) {
        "Patient??"
    }
}

/**
 * Retrieves the medication list and filters it by PatientID
 */
fun getMedicationsForPatient(context: Context, targetId: String): List<Medication> {
    val meds = mutableListOf<Medication>()
    try {
        context.resources.openRawResource(com.garent.s35123656.medtrack.R.raw.medications).bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val tokens = line.split(",")
                // tokens[0] is PatientID, tokens[1] is MedName, etc.
                if (tokens.size >= 5 && tokens[0].trim() == targetId) {
                    meds.add(Medication(
                        name = tokens[1].trim(),
                        dosage = tokens[2].trim(),
                        frequency = tokens[3].trim(),
                        scheduledTime = tokens[4].trim()
                    ))
                }
            }
        }
    } catch (e: Exception) { e.printStackTrace() }
    return meds
}

/**
 * Reusable Card UI for each medication
 */
@Composable
fun MedicationCard(med: Medication, onToggleTaken: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = med.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "${med.dosage} - ${med.scheduledTime}", style = MaterialTheme.typography.bodySmall)
                Text(
                    text = "${med.frequency}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // The "Taken" Toggle
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Taken", style = MaterialTheme.typography.labelSmall)
                Switch(
                    checked = med.isTaken,
                    onCheckedChange = { onToggleTaken(it) }
                )
            }
        }
    }
}

data class Medication(
    val name: String,
    val dosage: String,
    val frequency: String,
    val scheduledTime: String,
    var isTaken: Boolean = false // Taken or not taken
)



@Composable
fun MedTrackBottomBar(currentScreen: String, patientId: String) {
    val context = LocalContext.current
    NavigationBar {
        // Home Tab
        NavigationBarItem(
            selected = currentScreen == "Home",
            onClick = {
                // goes to home page if it's currently not
                if (currentScreen != "Home") {
                    val intent = Intent(context, HomeScreen::class.java)
                    intent.putExtra("PATIENT_ID", patientId) // include patient id when passing
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
                // goes to symptoms page if it's currently not
                if (currentScreen != "Symptoms") {
                    val intent = Intent(context, SymptomsScreen::class.java)
                    intent.putExtra("PATIENT_ID", patientId) // include patient id when passing
                    context.startActivity(intent)
                }
            },
            label = { Text("Symptoms") },
            icon = { Icon(painterResource(android.R.drawable.ic_dialog_alert), null) }
        )
    }
}
// FOR PREVIEW ONLYYY
@Preview(showBackground = true, name = "Home Screen Preview")
@Composable
fun HomeScreenPreview() {
    MedTrackTheme {
        // We pass a fake ID just to see what the layout looks like
        Home(patientId = "P1003")
    }
}