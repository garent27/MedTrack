package com.garent.s35123656.medtrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
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
import com.garent.s35123656.medtrack.ui.theme.MedTrackTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import kotlin.jvm.java
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.Icon


class HomeScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val patientId = intent.getStringExtra("PATIENT_ID") ?: ""

        setContent {
            MedTrackTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "home"

                Scaffold(
                    topBar = {
                        if (currentRoute == "home") {
                            MedTrackTopBar(onLogout = { performLogout(this) })
                        }
                    },
                    floatingActionButton = {
                        if (currentRoute == "home") {
                            AddMedicationFAB(onClick = {
                                val intent = Intent(this, AddMedication::class.java)
                                intent.putExtra("PATIENT_ID", patientId)
                                startActivity(intent)
                            })
                        }
                    },
                    bottomBar = {
                        MedTrackBottomBar(
                            currentScreen = currentRoute,
                            patientId = patientId,
                            navController = navController
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    // Use your new simplified method here
                    MedTrackNavHost(
                        navController = navController,
                        innerPadding = innerPadding,
                        patientId = patientId,
                        snackbarHostState = snackbarHostState
                    )

                }
            }
        }
    }
}


//@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Home(patientId: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // 1. Data State Management
    var patientName by remember { mutableStateOf("Unknown User") }
    var medicationList by remember { mutableStateOf(listOf<MedicationData>()) }


    // --- Lifecycle Observer to handle reloading ---
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            // This event triggers every time you return from AddMedication
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                patientName = GetPatientName(context, patientId)
                medicationList = getMedicationsForPatient(context, patientId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Load data once when patientId is available
    LaunchedEffect(patientId) {
        patientName = GetPatientName(context, patientId)
        medicationList = getMedicationsForPatient(context, patientId)
    }

    // 2. Calculations for UI
    val calendar = Calendar.getInstance().time
    val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    val currentDate = dateFormat.format(calendar)

    val totalMeds = medicationList.size
    val takenMeds = medicationList.count { it.isTaken }

    // Main column for all the content
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        // display username
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
            // Normal View if medications exist
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
                                if (currentMed.medicationName == med.medicationName && currentMed.scheduledTime == med.scheduledTime) {
                                    currentMed.copy(isTaken = isChecked)
                                } else {
                                    currentMed
                                }
                            }
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}


// Top bar button for logging out
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedTrackTopBar(onLogout: () -> Unit) {
    TopAppBar(
        title = { Text("MedTrack", fontWeight = FontWeight.Bold) },
        actions = {
            IconButton(onClick = onLogout) {
                // Replaced Text with Icon
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = "Logout", // Good for accessibility/screen readers
                    tint = MaterialTheme.colorScheme.error // This keeps your red color!
                )
            }
        }
    )
}

private fun performLogout(context: Context) {
    val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    sharedPref.edit().remove("logged_in_id").apply()
    val intent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    }
    context.startActivity(intent)
}


@Composable
fun AddMedicationFAB(onClick: () -> Unit) {
    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}


//retrieve user name from csv
private fun getNameFromCsv(context: Context, id: String): String? {
    return try {
        context.assets.open("patients.csv").bufferedReader().useLines { lines ->
            lines.forEach { line ->
                val tokens = line.split(",")
                if (tokens.isNotEmpty() && tokens[0].trim() == id) {
                    return@useLines tokens[2].trim()
                }
            }
            null
        }
    } catch (e: Exception) {
        null
    }
}

// retrieve user name from csv + gson
fun GetPatientName(context: Context, id: String): String {

    // 1. First, try to find the name in the CSV
    val csvName = getNameFromCsv(context, id)
    if (csvName != null) return csvName

    // 2. If not in CSV, look through SharedPreferences (JSON)
    val sharedPref = context.getSharedPreferences("users", Context.MODE_PRIVATE)
    val gson = Gson()

    // We must loop through all saved users because the key is Phone, not ID
    val allEntries = sharedPref.all
    for (entry in allEntries.values) {
        try {
            val userJson = entry.toString()
            val user = gson.fromJson(userJson, User::class.java)
            if (user.PatientID == id) {
                return user.Name
            }
        } catch (e: Exception) {
            continue // Skip if a specific entry is corrupted
        }
    }

    return "Unknown Patient"
}

/**
 * Retrieves the medication list and filters it by PatientID
 */
fun getMedicationsForPatient(context: Context, targetId: String): List<MedicationData> {

    // 1. Load from SharedPreferences (Gson)
    val sharedPref = context.getSharedPreferences("medications", Context.MODE_PRIVATE)
    val gson = Gson()
    val json = sharedPref.getString(targetId, null)

    val sharedPrefMeds = if (json != null) {
        val type = object : TypeToken<List<MedicationData>>() {}.type
        gson.fromJson<List<MedicationData>>(json, type) ?: emptyList()
    } else {
        emptyList()
    }


    // 2. Load from CSV (Raw Resource)
    val csvMeds = mutableListOf<MedicationData>()
    try {
        context.assets.open("medications.csv").bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val tokens = line.split(",")
                // tokens[0] is PatientID, tokens[1] is MedName, etc.
                if (tokens.size >= 5 && tokens[0].trim() == targetId) {
                    csvMeds.add(MedicationData(
                        medPetientID = tokens[0].trim(),
                        medicationName = tokens[1].trim(),
                        dosage = tokens[2].trim(),
                        frequency = tokens[3].trim(),
                        scheduledTime = tokens[4].trim(),
                        medicationType = tokens.getOrNull(5)?.trim() ?: "Unknown",
                        notes = tokens.getOrNull(6)?.trim() ?: ""
                    ))
                }
            }
        }
    } catch (e: Exception) { e.printStackTrace() }

    // return the combine reversed so newest is first
    return sharedPrefMeds.reversed() + csvMeds.reversed()
}

/**
 * Reusable Card UI for each medication
 */
@Composable
fun MedicationCard(med: MedicationData, onToggleTaken: (Boolean) -> Unit) {
    // 1. Define visual states based on med.isTaken
    val cardAlpha = if (med.isTaken) 0.6f else 1f
    val textDecoration = if (med.isTaken) TextDecoration.LineThrough else TextDecoration.None
    val cardElevation = if (med.isTaken) 0.dp else 2.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(cardAlpha), // 2. Greys out the entire card
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = med.medicationName ?: "Unknown Medication",
                    style = MaterialTheme.typography.titleMedium.copy(
                        textDecoration = textDecoration // 3. Applies Strikethrough
                    ),
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${med.dosage} - ${med.scheduledTime}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (med.isTaken) Color.Gray else Color.Unspecified
                )
                Text(
                    text = "${med.frequency}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (med.isTaken) Color.Gray else MaterialTheme.colorScheme.secondary
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
data class MedicationData(
    val medPetientID: String? = "",
    val medicationName: String? = "Unknown",
    val dosage: String? = "",
    val frequency: String? = "",
    val scheduledTime: String? = "",
    val medicationType: String? = "",
    val notes: String? = "",
    var isTaken: Boolean = false
)



@Composable
fun MedTrackBottomBar(
    currentScreen: String,
    patientId: String,
    navController: NavController
) {
    NavigationBar {
        // Home Tab
        NavigationBarItem(
            selected = currentScreen == "home",
            onClick = {
                if (currentScreen != "home") {
                    navController.navigate("home") {
                        // 1. Pop back to the root ("home"), but SAVE the state
                        // of the screen we are leaving (like Symptoms)
                        popUpTo("home") {
                            saveState = true
                        }
                        // 2. Prevent creating duplicate Home screens
                        launchSingleTop = true
                        // 3. RESTORE the Home screen state if we were here before
                        restoreState = true
                    }
                }
            },
            label = { Text("Home") },
            icon = { Icon(painterResource(android.R.drawable.ic_menu_today), null) }
        )

        // Symptoms Tab
        NavigationBarItem(
            selected = currentScreen == "symptoms",
            onClick = {
                if (currentScreen != "symptoms") {
                    navController.navigate("symptoms") {
                        // 1. Pop back to the root ("home") to avoid a massive backstack,
                        // but SAVE the state of the screen we are leaving (Home)
                        popUpTo("home") {
                            saveState = true
                        }
                        // 2. Prevent creating duplicate Symptoms screens
                        launchSingleTop = true
                        // 3. RESTORE the Symptoms screen state (like typed notes)
                        restoreState = true
                    }
                }
            },
            label = { Text("Symptoms") },
            icon = { Icon(painterResource(android.R.drawable.ic_dialog_alert), null) }
        )
    }
}

@Composable
fun MedTrackNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    patientId: String,
    snackbarHostState: SnackbarHostState
) {
    NavHost(
        navController = navController,
        startDestination = "home",
        modifier = Modifier.padding(innerPadding)
    ) {
        composable("home") {
            Home(patientId = patientId)
        }
        composable("symptoms") {
            Symptoms(
                patientId = patientId,
                snackbarHostState = snackbarHostState
            )
        }
    }
}


// FOR PREVIEW ONLY
@Preview(showBackground = true, name = "Home Screen Preview")
@Composable
fun HomeScreenPreview() {
    MedTrackTheme {
        // We pass a fake ID just to see what the layout looks like
        Home(patientId = "P1001")
    }
}