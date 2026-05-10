package com.garent.s35123656.medtrack

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.garent.s35123656.medtrack.data.viewModel.SignUpViewModel
import com.garent.s35123656.medtrack.ui.theme.MedTrackTheme
import kotlinx.coroutines.delay

/**
 * Sign Up screen for new user registration.
 * Follows MVVM: Delegates database operations and validation logic to SignUpViewModel.
 * Form state is managed by the ViewModel to survive rotation.
 */
@Composable
fun SignUp(
    navController: NavController,
    viewModel: SignUpViewModel,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    // UI State for input fields - Now managed by ViewModel
    val fullName = viewModel.fullName
    val phone = viewModel.phone
    val password = viewModel.password
    val confirmPassword = viewModel.confirmPassword
    val context = LocalContext.current


    // Observe registration results from the ViewModel
    LaunchedEffect(Unit) {
        viewModel.signUpResult.collect { result ->
            when (result) {
                is SignUpViewModel.SignUpResult.Success -> {
                    Toast.makeText(
                        context,
                        "Account created successfully!! Your user id is ${result.patientId}",
                        Toast.LENGTH_LONG
                    ).show()
                    navController.popBackStack()
                }
                is SignUpViewModel.SignUpResult.Error -> {
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        ValidatedTextField(
            value = fullName,
            onValueChange = { viewModel.fullName = it },
            label = "Full Name",
            error = null
        )

        Spacer(modifier = Modifier.height(16.dp))

        ValidatedTextField(
            value = phone,
            onValueChange = { viewModel.phone = it },
            label = "Phone Number (starts with 04)",
            error = null,
            keyboardType = KeyboardType.Phone
        )

        Spacer(modifier = Modifier.height(16.dp))

        ValidatedTextField(
            value = password,
            onValueChange = { viewModel.password = it },
            label = "Password (8+ chars, letter & number)",
            error = null,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        ValidatedTextField(
            value = confirmPassword,
            onValueChange = { viewModel.confirmPassword = it },
            label = "Confirm Password",
            error = if (confirmPassword.isNotEmpty() && confirmPassword != password) "Passwords do not match" else null,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { viewModel.signUp() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        TextButton(onClick = { navController.popBackStack() }) {
            Text("Back to Login")
        }
    }
}

/**
 * A reusable text field component with built-in error display.
 */
@Composable
fun ValidatedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    error: String?,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            isError = error != null,
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true
        )
        if (error != null) {
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Preview(showBackground = true, name = "Sign Up Screen Preview")
@Composable
fun SignUpPreview() {
    MedTrackTheme {
        // Preview logic
    }
}