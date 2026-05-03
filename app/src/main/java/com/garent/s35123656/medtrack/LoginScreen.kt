package com.garent.s35123656.medtrack

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.garent.s35123656.medtrack.data.viewModel.LoginViewModel
import com.garent.s35123656.medtrack.ui.theme.MedTrackTheme

/**
 * Login screen for user authentication using PatientID and Password.
 * Follows MVVM: Delegates logic to LoginViewModel.
 */
@Composable
fun Login(
    navController: NavController,
    viewModel: LoginViewModel,
    onLoginSuccess: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val patientId = viewModel.patientId
    val password = viewModel.password
    val context = LocalContext.current

    // Observe login result from ViewModel
    LaunchedEffect(Unit) {
        viewModel.loginResult.collect { result ->
            when (result) {
                is LoginViewModel.LoginResult.Success -> {
                    Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
                    val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
                    sharedPref.edit().putString("logged_in_id", result.patientId).apply()
                    onLoginSuccess(result.patientId)
                }
                is LoginViewModel.LoginResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Image(
            painter = painterResource(id = R.drawable.medtrack),
            contentDescription = "MedTrack Logo",
            modifier = Modifier.size(180.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = patientId,
            onValueChange = { viewModel.patientId = it },
            label = { Text("Patient ID (e.g. P1001)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { viewModel.password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.login() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = { navController.navigate("claim_account") }
        ) {
            Text("Claim Account (CSV Users)", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Don't have an account?", style = MaterialTheme.typography.bodyMedium)

            TextButton(
                onClick = { navController.navigate("signup") }
            ) {
                Text("Sign Up", fontWeight = FontWeight.Bold)
            }
        }
    }
}
