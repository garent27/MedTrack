package com.garent.s35123656.medtrack

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.garent.s35123656.medtrack.ui.theme.MedTrackTheme
import kotlin.jvm.java

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // init variable for checking Session Persistence & Navigation Guard
        val sharedPref = getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
        val savedId = sharedPref.getString("logged_in_id", null)

        // if user log in exist
        if (savedId != null) {
            // Session exists! Skip Login and go to Home
            val intent = Intent(this, HomeScreen::class.java).apply {
                putExtra("PATIENT_ID", savedId)
            }
            startActivity(intent)
            finish() // Important: Destroy MainActivity so user can't "Go Back" to it
        } else {
            enableEdgeToEdge()
            setContent {
                MedTrackTheme {
                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        WelcomeScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}


@Composable
fun WelcomeScreen(modifier: Modifier = Modifier) {
    // Needed for the Intent is initiated from
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo above MedTrack
        Image(
            painter = painterResource(id = R.drawable.choms2), // Replace 'logo' with your actual file name
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // App Name
        Text(
            text = "MedTrack",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Health Disclaimer
        Text(
            text = "This app is for tracking purposes only and does not replace professional medical advice.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Monash Health Link
        TextButton(onClick = {
            uriHandler.openUri("https://www.monashhealth.org")
        }) {
            Text("Visit Monash Health Clinic")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button using Intent
        Button(
            onClick = {
                // Navigates to LoginActivity using the standard Intent method
                // Use the name of the ACTUAL CLASS you created
                context.startActivity(Intent(context, LoginScreen::class.java))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                context.startActivity(Intent(context, SignUpScreen::class.java))
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.weight(1f))

        // Student Info (Requirement)
        Text(
            text = "By Garent Ngor Jun Hoe (35123656)",
            style = MaterialTheme.typography.labelLarge,
            color = Color.DarkGray
        )
    }
}

@Preview(showBackground = true, name = "Home Screen Preview")
@Composable
fun MainPreview() {
    MedTrackTheme {
        // We pass a fake ID just to see what the layout looks like
        WelcomeScreen()
    }
}