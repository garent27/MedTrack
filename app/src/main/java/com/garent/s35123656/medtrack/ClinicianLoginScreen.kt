package com.garent.s35123656.medtrack

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.garent.s35123656.medtrack.data.viewModel.ClinicianLoginViewModel
import kotlinx.coroutines.launch

@Composable
fun ClinicianLogin(
    navController: NavController,
    viewModel: ClinicianLoginViewModel
) {
    val accessKey = viewModel.accessKey
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Observe login result from ViewModel
    LaunchedEffect(Unit) {
        viewModel.loginResult.collect { success ->
            if (success) {
                navController.navigate("clinician_dashboard") {
                    popUpTo("clinician_login") { inclusive = true }
                }
            } else {
                Toast.makeText(context, "Invalid Access Key", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Clinician Access",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = accessKey,
            onValueChange = { viewModel.accessKey = it },
            label = { Text("Access Key") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.login() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Back")
        }
    }
}
