package com.garent.s35123656.medtrack

import android.app.Activity
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
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
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
                    Toast.makeText(
                        context,
                        "Please enter both phone and password",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // Check credentials against patients.csv
                    val patientId = validateAllUser(context, phone, password)

                    if (patientId != null) {
                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()

                        // save id log in in shared preference validate successful
                        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        sharedPref.edit().putString("logged_in_id", patientId).apply()

                        context.startActivity(Intent(context, HomeScreen::class.java).apply {
                            putExtra("PATIENT_ID", patientId)
                        })
                        // kill current screen
                        (context as Activity).finish()

                        // Navigate to HomeActivity
                        context.startActivity(Intent(context, HomeScreen::class.java).apply {
                            putExtra("PATIENT_ID", patientId) // Pass patient ID
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



        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Don't have an account?", style = MaterialTheme.typography.bodyMedium)

            TextButton(
                onClick = {
                    // Navigate to SignUpActivity
                    val intent = Intent(context, SignUpScreen::class.java)
                    context.startActivity(intent)
                }
            ) {
                Text("Sign Up", fontWeight = FontWeight.Bold)
            }
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
            lines.drop(1).forEach { line ->
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
        Log.e("Login", "${e.message}")
        null
    }
}

fun validateAllUser(context: Context, phone: String, password: String): String? {
    // 1. Check CSV first
    val csvResult = validateUserFromCsv(context, phone, password)
    if (csvResult != null) return csvResult

    // 2. If not found in CSV, check SharedPreferences
    val sharedPref = context.getSharedPreferences("users", Context.MODE_PRIVATE)
    val userJson = sharedPref.getString(phone, null) ?: return null // User doesn't exist

    return try {
        val gson = Gson()
        val user = gson.fromJson(userJson, User::class.java)

        // Check if the password matches
        if (user.Password == password) {
            user.PatientID // Return the generated ID (e.g., P1004)
        } else {
            null // Wrong password
        }
    } catch (e: Exception) {
        Log.e("Login", "${e.message}")
        null
    }
}

// FOR PREVIEW ONLY
@Preview(showBackground = true, name = "Home Screen Preview")
@Composable
fun LoginScreenPrev() {
    MedTrackTheme {
        // We pass a fake ID just to see what the layout looks like
        Login()
    }
}