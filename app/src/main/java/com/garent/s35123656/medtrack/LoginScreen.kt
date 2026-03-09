package com.garent.s35123656.medtrack

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import java.io.BufferedReader
import java.io.InputStreamReader

import android.content.Context


@Composable
fun LoginScreen(
    onLoginSuccess: (String) -> Unit, // Passes the Patient ID on success
    modifier: Modifier = Modifier
) {
    // State for input fields
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            // 1. Logo
            androidx.compose.foundation.Image(
                painter = painterResource(id = R.drawable.choms2),
                contentDescription = "MedTrack Logo",
                modifier = Modifier.size(200.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Phone Number Field (Requirement: Phone Number)
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Password Field (Requirement: Masked Input)
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Login Button with CSV Validation Logic
            Button(
                onClick = {
                    // Step 1: Validate fields are not empty
                    if (phone.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Fields cannot be blank", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    // Step 2: Authenticate against patients.csv
                    val loggedInId = validateLogin(context, phone, password)

                    if (loggedInId != null) {
                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                        // Step 3: On success, trigger navigation
                        onLoginSuccess(loggedInId)
                    } else {
                        // Detailed error messages based on what failed
                        Toast.makeText(context, "Incorrect phone or password", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
        }
    }
}

/**
 * Helper function to read the raw CSV file and validate the user.
 * Assumes CSV format: id,name,phone,password
 */
fun validateUserFromCsv(context: android.content.Context, phone: String, password: String): String? {
    return try {
        // You must place your patients.csv in res/raw folder
        val inputStream = context.resources.openRawResource(R.raw.patients)
        val reader = BufferedReader(InputStreamReader(inputStream))

        reader.useLines { lines ->
            lines.forEach { line ->
                val tokens = line.split(",")
                if (tokens.size >= 4) {
                    val csvId = tokens[0].trim()
                    val csvPhone = tokens[2].trim()
                    val csvPassword = tokens[3].trim()

                    if (csvPhone == phone && csvPassword == password) {
                        return csvId // Found a match!
                    }
                }
            }
        }
        null // No match found
    } catch (e: Exception) {
        null
    }
}


/**
 * Validates credentials against the patients.csv schema.
 * returns PatientID if successful, null if failed.
 */
fun validateLogin(context: Context, enteredPhone: String, enteredPass: String): String? {
    try {
        // Open the file from res/raw/patients.csv
        val inputStream = context.resources.openRawResource(R.raw.patients)
        val reader = BufferedReader(InputStreamReader(inputStream))

        // Read line by line
        reader.useLines { lines ->
            lines.forEach { line ->
                // Split by comma: [0]=ID, [1]=Phone, [2]=Name, [3]=Pass
                val columns = line.split(",")
                if (columns.size >= 4) {
                    val patientId = columns[0].trim()
                    val csvPhone = columns[1].trim()
                    val csvPass = columns[3].trim()

                    // Check if phone and password match
                    if (csvPhone == enteredPhone && csvPass == enteredPass) {
                        return patientId // Match found!
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null // No match or error
}