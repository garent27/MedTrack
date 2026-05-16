package com.garent.s35123656.medtrack

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.garent.s35123656.medtrack.data.viewModel.ClaimAccountViewModel

@Composable
fun ClaimAccount(
    navController: NavController,
    viewModel: ClaimAccountViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    val patientId = viewModel.patientId
    val phone = viewModel.phone
    val newPassword = viewModel.newPassword
    val confirmPassword = viewModel.confirmPassword

    LaunchedEffect(Unit) {
        viewModel.claimResult.collect { result ->
            when (result) {
                is ClaimAccountViewModel.ClaimResult.Success -> {
                    Toast.makeText(context, "Account claimed successfully! Please login.", Toast.LENGTH_LONG).show()
                    navController.navigate("login") {
                        popUpTo("welcome") { inclusive = false }
                    }
                }
                is ClaimAccountViewModel.ClaimResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        item {
            Text(
                text = "Claim Your Account",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        item {
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "If you were registered via CSV, please provide your PatientID and Phone Number to set a password.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = patientId,
                onValueChange = { viewModel.patientId = it },
                label = { Text("Patient ID (e.g. P1001)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = { viewModel.phone = it },
                label = { Text("Phone Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = newPassword,
                onValueChange = { viewModel.newPassword = it },
                label = { Text("New Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { viewModel.confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { viewModel.claimAccount() },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Claim Account")
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("Back")
            }
        }
    }
}
