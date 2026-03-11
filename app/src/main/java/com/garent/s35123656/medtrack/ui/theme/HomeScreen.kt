package com.garent.s35123656.medtrack.ui.theme

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.R
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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

class HomeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Get Patient id from login screen
        val patientId = intent.getStringExtra("PATIENT_ID") ?: ""
        setContent {
            MedTrackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Home(
                        patientId = patientId,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Home(patientId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // var to hold patient name
    var patientName by remember {mutableStateOf("Patient")}

    // Find patient name from ID from database
    patientName = getPatientName(context, patientId)
    val medicationList = remember(patientId) { getMedicationsForPatient(context, patientId) }


    // set a variable for current date time
    val calendar = Calendar.getInstance().time
    val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(calendar)


    // for alignment
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Hello, $patientName",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = currentDate,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Your Medications",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 3. Medication List (The Scrolling Part)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(medicationList) { med ->
                MedicationCard(med)
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
fun MedicationCard(med: Medication) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = med.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(text = "Dosage: ${med.dosage}", style = MaterialTheme.typography.bodyMedium)
            Text(text = "Frequency: ${med.frequency}", style = MaterialTheme.typography.bodyMedium)

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

            Text(
                text = "Time: ${med.scheduledTime}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}

data class Medication(
    val name: String,
    val dosage: String,
    val frequency: String,
    val scheduledTime: String
)



// FOR PREVIEW ONLYYY
@Preview(showBackground = true, name = "Home Screen Preview")
@Composable
fun HomeScreenPreview() {
    MedTrackTheme {
        // We pass a fake ID just to see what the layout looks like
        Home(patientId = "P1001")
    }
}