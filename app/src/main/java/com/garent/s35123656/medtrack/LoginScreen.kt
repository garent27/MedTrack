package com.garent.s35123656.medtrack

import android.content.Context
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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

/**
 *  Main login screen
 */
@Composable
fun Login(
    onLoginSuccess: (String) -> Unit, // callback to talk to MainActivity
    modifier: Modifier = Modifier
) {
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
        // 2. App Logo
        Image(
            painter = painterResource(id = R.drawable.medtrack),
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

        // 4. Password Input
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
                    // Check credentials against patients.csv and SharedPreferences
                    val patientId = validateAllUser(context, phone, password)

                    if (patientId != null) {
                        Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()

                        // Save logged-in ID to SharedPreferences
                        val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                        sharedPref.edit().putString("logged_in_id", patientId).apply()

                        // callback to let MainActivity handle the navigation
                        onLoginSuccess(patientId)

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
                    // Navigate to SignUpActivity (Leaving this as Intent for now assuming SignUp is still an Activity)
                    val intent = Intent(context, SignUpScreen::class.java)
                    context.startActivity(intent)
                }
            ) {
                Text("Sign Up", fontWeight = FontWeight.Bold)
            }
        }
    }
}


/**
 *  Function to validate user from csv
 */
fun validateUserFromCsv(context: Context, phone: String, password: String): String? {
    return try {
        val inputStream = context.assets.open("patients.csv")
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

/**
 *  Function to validate all user (CSV + SharedPreference)
 */
fun validateAllUser(context: Context, phone: String, password: String): String? {
    // 1. Check CSV first
    val csvResult = validateUserFromCsv(context, phone, password)
    if (csvResult != null) return csvResult

    // 2. If not found in CSV, check SharedPreferences
    val sharedPref = context.getSharedPreferences("users", Context.MODE_PRIVATE)
    val userJson = sharedPref.getString(phone, null) ?: return null

    return try {
        val gson = Gson()
        val user = gson.fromJson(userJson, User::class.java) // ensure User class exists

        // Check if the password matches
        if (user.Password == password) {
            user.PatientID
        } else {
            null
        }
    } catch (e: Exception) {
        Log.e("Login", "${e.message}")
        null
    }
}

// FOR PREVIEW ONLY
@Preview(showBackground = true, name = "Login Screen Preview")
@Composable
fun LoginScreenPrev() {
    MedTrackTheme {
        // Pass a dummy function {} so the preview doesn't break
        Login(onLoginSuccess = {})
    }
}