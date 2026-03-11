package com.garent.s35123656.medtrack

import android.content.Intent
import android.os.Bundle
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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.garent.s35123656.medtrack.ui.theme.Home

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
                    SymptomsScreen(
                        patientId = patientId,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
@Composable
fun SymptomsScreen(patientId: String, modifier: Modifier = Modifier) {
    // List of categories
    val categories = listOf("Pain", "Nausea", "Dizziness", "Fatigue", "Headache", "Skin Reaction", "Other")

    // State variables
    var expanded by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf(categories[0]) }

    Column(
        modifier = modifier
            .fillMaxSize()
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

        Button(
            onClick = { /* Basic button for now */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
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
        SymptomsScreen(patientId = "P1003")
    }
}