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
import androidx.navigation.NavController
import com.google.gson.Gson
import kotlinx.coroutines.launch
import kotlin.jvm.java

/**
 *  Main login screen
 */
@Composable
fun Login(
    navController: NavController,
    onLoginSuccess: (String) -> Unit, // callback to talk to MainActivity
    modifier: Modifier = Modifier
) {
    // 1. State management for input fields
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val context = LocalContext.current

    val db = com.garent.s35123656.medtrack.data.database.MedTrackDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()

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
                scope.launch {
                    if (phone.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please enter both phone and password", Toast.LENGTH_SHORT).show()
                    } else {
                        val patient = db.patientDao().getPatientByPhone(phone)

                        if (patient == null) {
                            Toast.makeText(context, "No account found with this phone number", Toast.LENGTH_SHORT).show()
                        } else if (patient.password != password) {
                            Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                            val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                            sharedPref.edit().putString("logged_in_id", patient.patientId).apply()
                            onLoginSuccess(patient.patientId)
                        }
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
                    navController.navigate("signup")

                }
            ) {
                Text("Sign Up", fontWeight = FontWeight.Bold)
            }
        }
    }
}


//fun validateAllUser(context: Context, phone: String, password: String): Pair<String?, String?> {
//    // 1. Check CSV
//    try {
//        val inputStream = context.assets.open("patients.csv")
//        val reader = BufferedReader(InputStreamReader(inputStream))
//        var phoneFoundInCsv = false
//
//        reader.useLines { lines ->
//            lines.drop(1).forEach { line ->
//                val tokens = line.split(",")
//                if (tokens.size >= 4) {
//                    val csvId = tokens[0].trim()
//                    val csvPhone = tokens[1].trim()
//                    val csvPass = tokens[3].trim()
//
//                    if (csvPhone == phone) {
//                        phoneFoundInCsv = true
//                        if (csvPass == password) {
//                            return Pair(csvId, null)
//                        }
//                    }
//                }
//            }
//        }
//        if (phoneFoundInCsv) {
//            return Pair(null, "Incorrect password")
//        }
//    } catch (e: Exception) {
//        Log.e("Login", "CSV Error: ${e.message}")
//    }
//
//    // 2. Check SharedPreferences
//    val sharedPref = context.getSharedPreferences("users", Context.MODE_PRIVATE)
//    val userJson = sharedPref.getString(phone, null)
//        ?: return Pair(null, "No account found with this phone number")
//
//    return try {
//        val gson = Gson()
//        val user = gson.fromJson(userJson, User::class.java)
//        if (user.Password == password) {
//            Pair(user.PatientID, null)
//        } else {
//            Pair(null, "Incorrect password")
//        }
//    } catch (e: Exception) {
//        Pair(null, "Error loading account data")
//    }
//
//}

// FOR PREVIEW ONLY
@Preview(showBackground = true, name = "Login Screen Preview")
@Composable
fun LoginScreenPrev() {
    MedTrackTheme {
        // Pass a dummy function {} so the preview doesn't break
//        Login(onLoginSuccess = {})
    }
}