package com.garent.s35123656.medtrack

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.garent.s35123656.medtrack.ui.theme.MedTrackTheme
import java.io.BufferedReader
import java.io.InputStreamReader


import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.garent.s35123656.medtrack.ui.theme.HomeScreen
import kotlin.jvm.java

class LoginScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedTrackTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Login(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun Login(modifier: Modifier = Modifier) {
    // 1. State management for input fields
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // 2. App Logo (Ensure 'choms2' exists in res/drawable)
        Image(
            painter = painterResource(id = R.drawable.choms2),
            contentDescription = "MedTrack Logo",
            modifier = Modifier.size(200.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 3. Phone Number Input
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone Number") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 4. Password Input (Masked)
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 5. Login Button with Logic
        Button(
            onClick = {
                if (phone.isBlank() || password.isBlank()) {
                    Toast.makeText(context, "Please enter both phone and password", Toast.LENGTH_SHORT).show()
                } else {
                    // Check credentials against patients.csv
                    val patientId = validateUserFromCsv(context, phone, password)

                    if (patientId != null) {
                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()

                        // Navigate to HomeActivity
                        context.startActivity(Intent(context, HomeScreen::class.java).apply{
                            putExtra("PATIENT_ID", patientId) // Pass patient Id
                        })
                    } else {
                        Toast.makeText(context, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }
    }
}

// function to validate user from csv

fun validateUserFromCsv(context: Context, phone: String, password: String): String? {
    return try {
        // Reads from app/src/main/res/raw/patients.csv
        val inputStream = context.resources.openRawResource(R.raw.patients)
        val reader = BufferedReader(InputStreamReader(inputStream))

        reader.useLines { lines ->
            lines.forEach { line ->
                val tokens = line.split(",")
                val csvId = tokens[0]
                val csvPhone = tokens[1]
                val csvPass = tokens[3]

                if (csvPhone == phone && csvPass == password) {
                    return csvId
                }
            }
        }
        null
    } catch (e: Exception) {
        null
    }
}