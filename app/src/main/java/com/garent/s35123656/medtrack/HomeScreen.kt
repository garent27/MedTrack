package com.garent.s35123656.medtrack

import android.content.Context
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.navigation.NavController
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.runtime.collectAsState
import com.garent.s35123656.medtrack.data.entity.Medication
import com.garent.s35123656.medtrack.data.viewModel.HomeViewModel
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.HealthAndSafety
import com.garent.s35123656.medtrack.data.MedicationData

/**
 * Main home screen
 */
@Composable
fun Home(patientId: String, viewModel: HomeViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    // Observe data reactively from the ViewModel
    val medicationList by viewModel.getMedications(patientId).collectAsState(initial = emptyList())
    val patientName = viewModel.patientName

    // Load the patient's name and check for daily reset
    LaunchedEffect(patientId) {
        if (patientId.isNotEmpty()) {
            viewModel.onHomeScreenStarted(patientId, context)
        }
    }

    // Calculations for UI
    val calendar = Calendar.getInstance().time
    val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
    val currentDateText = dateFormat.format(calendar)

    val totalMeds = medicationList.size
    val takenMeds = medicationList.count { it.isTaken }

    // Updated to a single LazyColumn to support horizontal/landscape scrolling
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
    ) {
        // Welcome section with Patient ID for reminder
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Hello, $patientName",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "ID: $patientId",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Current date display
        item {
            Text(
                text = currentDateText,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
        }

        item {
            Text(
                text = "Your Medications",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Conditional display based on medication list
        if (medicationList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 64.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No medications scheduled.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // Summary card
            item {
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
            }

            // List of medication cards
            items(medicationList) { med ->
                MedicationCard(
                    med = med,
                    onToggleTaken = { isChecked ->
                        viewModel.toggleMedicationTaken(med.id, isChecked)
                    }
                )
            }
        }
    }
}


/**
 * Top bar with logout action.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedTrackTopBar(onLogout: () -> Unit) {
    TopAppBar(
        title = { Text("MedTrack", fontWeight = FontWeight.Bold) },
        actions = {
            IconButton(onClick = onLogout) {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}

/**
 * Floating Action Button to navigate to the Add Medication screen.
 */
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

/**
 * Card UI component for displaying medication details and status.
 */
@Composable
fun MedicationCard(med: Medication, onToggleTaken: (Boolean) -> Unit) {
    val cardAlpha = if (med.isTaken) 0.6f else 1f
    val textDecoration = if (med.isTaken) TextDecoration.LineThrough else TextDecoration.None
    val cardElevation = if (med.isTaken) 0.dp else 2.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .alpha(cardAlpha),
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
                        textDecoration = textDecoration
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


/**
 * Bottom navigation bar for switching between core screens.
 */
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
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
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
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            label = { Text("Symptoms") },
            icon = { Icon(painterResource(android.R.drawable.ic_dialog_alert), null) }
        )

        // MedCoach Tab
        NavigationBarItem(
            selected = currentScreen == "med_coach",
            onClick = {
                if (currentScreen != "med_coach") {
                    navController.navigate("med_coach") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            label = { Text("Coach") },
            icon = { Icon(Icons.Filled.HealthAndSafety, contentDescription = "MedCoach") }
        )

        // Settings Tab
        NavigationBarItem(
            selected = currentScreen == "settings",
            onClick = {
                if (currentScreen != "settings") {
                    navController.navigate("settings") {
                        popUpTo("home") { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            },
            label = { Text("Settings") },
            icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") }
        )
    }
}

@Preview(showBackground = true, name = "Home Screen Preview")
@Composable
fun HomeScreenPreview() {
    MedTrackTheme {
        // Home(patientId = "P1001", viewModel = ...)
    }
}
