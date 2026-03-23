package com.garent.s35123656.medtrack

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.garent.s35123656.medtrack.ui.theme.MedTrackTheme
import com.google.gson.Gson
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SignUpScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedTrackTheme {
                // 1. Move the SnackbarState here
                val snackbarHostState = remember { SnackbarHostState() }

                Scaffold(
                    // 2. Attach the host here
                    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    // 3. Pass the state down so the function can still trigger messages
                    SignUp(
                        snackbarHostState = snackbarHostState,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
@Composable
fun SignUp(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // user variables
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    // Error States
    var nameError by remember { mutableStateOf<String?>(null) }
    var phoneError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmError by remember { mutableStateOf<String?>(null) }

    // snackbar messages
    val scope = rememberCoroutineScope() // Required for launching the snackbar

    Column(
        modifier = modifier // Use the modifier from the Activity
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

        // Full Name Field
        ValidatedTextField(
            value = fullName,
            onValueChange = { fullName = it; nameError = null },
            label = "Full Name",
            error = nameError
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Phone Number Field
        ValidatedTextField(
            value = phone,
            onValueChange = { phone = it; phoneError = null },
            label = "Phone Number (starts with 04)",
            error = phoneError,
            keyboardType = KeyboardType.Phone
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        ValidatedTextField(
            value = password,
            onValueChange = { password = it; passwordError = null },
            label = "Password (8+ chars, letter & number)",
            error = passwordError,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        ValidatedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmError = null },
            label = "Confirm Password",
            error = confirmError,
            isPassword = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Sign Up Button
        Button(
            onClick = {
                // Reset errors
                nameError = null; phoneError = null; passwordError = null; confirmError = null

                val isPhoneValid = phone.startsWith("04") && phone.length == 10
                val isPasswordValid = password.length >= 8 &&
                        password.any { it.isLetter() } &&
                        password.any { it.isDigit() }

                // 1. Basic Required Check
                if (fullName.isBlank()) nameError = "Name is required"

                // 2. Phone Logic
                if (phone.isBlank()) phoneError = "Phone is required"
                else if (!isPhoneValid) phoneError = "Must start with 04 and be 10 digits"
                else if (!checkUniquePhone(context, phone)) phoneError =
                    "This phone number is already registered"

                // 3. Password Logic
                if (password.isBlank()) passwordError = "Password is required"
                else if (!isPasswordValid) passwordError =
                    "Must be 8+ chars with a letter and a number"

                // 4. Confirm Logic
                if (confirmPassword != password) confirmError = "Passwords do not match"

                // Final Validation Check
                if (nameError == null && phoneError == null && passwordError == null && confirmError == null) {
                    // save the user
                    saveNewUser(context, fullName, phone, password)

                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Account created successfully! Redirecting back to login"
                        )

                        // kill current screen and go back login
                        (context as? Activity)?.finish()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        TextButton(onClick = { (context as? Activity)?.finish() }) {
            Text("Back to Login")

        }
    }
}
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

fun checkUniquePhone(context: Context, phone: String): Boolean {
    // 1. Check CSV
    try {
        context.resources.openRawResource(R.raw.patients).bufferedReader().useLines { lines ->
            if (lines.any { it.split(",").getOrNull(1)?.trim() == phone }) return false
        }
    } catch (e: Exception) { e.printStackTrace() }

    // 2. Check SharedPreferences
    val sharedPref = context.getSharedPreferences("users", Context.MODE_PRIVATE)
    // We store users with phone as the key
    return !sharedPref.contains(phone)
}

fun saveNewUser(context: Context, name: String, phone: String, pass: String) {
    val sharedPref = context.getSharedPreferences("users", Context.MODE_PRIVATE)
    val gson = Gson()

    val nextId = generateNextPatientId(context)
    val newUser = User(
        PatientID = nextId,
        PhoneNumber = phone,
        Name = name,
        Password = pass
    )

    val userJson = gson.toJson(newUser)

    sharedPref.edit().putString(phone, userJson).apply()
}

fun generateNextPatientId(context: Context): String {
    val ids = mutableListOf<Int>()

    // 1. Check CSV for IDs
    try {
        context.resources.openRawResource(R.raw.patients).bufferedReader().useLines { lines ->
            lines.drop(1).forEach { line ->
                val idPart = line.split(",").getOrNull(0)?.removePrefix("P")?.toIntOrNull()
                if (idPart != null) ids.add(idPart)
            }
        }
    } catch (e: Exception) { e.printStackTrace() }

    // 2. Check SharedPreferences for IDs
    val sharedPref = context.getSharedPreferences("users", Context.MODE_PRIVATE)
    val allEntries = sharedPref.all
    val gson = Gson()

    allEntries.values.forEach { json ->
        val user = gson.fromJson(json.toString(), User::class.java)
        val idPart = user.PatientID.removePrefix("P").toIntOrNull()
        if (idPart != null) ids.add(idPart)
    }

    // 3. Find max and increment
    val nextId = (ids.maxOrNull() ?: 1000) + 1
    return "P$nextId"
}

data class User(
    val PatientID: String,
    val PhoneNumber: String,
    val Name: String,
    val Password: String
)


@Preview(showBackground = true, name = "Home Screen Preview")
@Composable
fun LoginScreenPrev3() {
    // 1. Create a dummy state just for the preview
    val dummySnackbarHostState = remember { SnackbarHostState() }

    MedTrackTheme {
        // 2. Pass the dummy state into your function
        SignUp(snackbarHostState = dummySnackbarHostState)
    }
}