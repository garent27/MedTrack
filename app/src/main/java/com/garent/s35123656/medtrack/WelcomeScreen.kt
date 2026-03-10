package com.garent.s35123656.medtrack


import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


@Composable
fun WelcomeScreen(onNavigateToLogin: () -> Unit) {
//    val uriHandler = LocalUriHandler.current
//
//    Column(
//        modifier = Modifier
//            .fillMaxSize()
//            .padding(24.dp),
//        horizontalAlignment = Alignment.CenterHorizontally,
//        verticalArrangement = Arrangement.Center
//    ) {
//
//        // App Name at the top
//        Text(
//            text = "MedTrack",
//            fontSize = 42.sp,
//            fontWeight = FontWeight.Bold,
//            color = MaterialTheme.colorScheme.primary
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//
//
//        // Health Disclaimer
//        Text(
//            text = "This app is for tracking purposes only and does not replace professional medical advice.",
//            style = MaterialTheme.typography.bodySmall,
//            textAlign = TextAlign.Center,
//            color = Color.Gray
//        )
//
//        Spacer(modifier = Modifier.height(48.dp))
//
//        // Monash Health Link
//        TextButton(onClick = {
//            uriHandler.openUri("https://www.monashhealth.org")
//        }) {
//            Text("Visit Monash Health Clinic")
//        }
//
//        Spacer(modifier = Modifier.height(24.dp))
//
//        // Login Button
//        Button(
//            context.startActivity(Intent(context, LoginScreen::class.java))
//        ) {
//            Text("Login")
//        }
//
//        Spacer(modifier = Modifier.weight(1f))
//
//        // Student Info (Requirement)
//        Text(
//            text = "By Garent Ngor Jun Hoe (35123656)",
//            style = MaterialTheme.typography.labelLarge,
//            color = Color.DarkGray
//        )
//    }
}