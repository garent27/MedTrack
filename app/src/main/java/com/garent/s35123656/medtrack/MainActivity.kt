package com.garent.s35123656.medtrack

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.garent.s35123656.medtrack.ui.theme.MedTrackTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MedTrackTheme {
                // Create and remember controller
                val navController = rememberNavController()
                // layout structure
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MedTrackNavigation(
                        navController = navController,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}


//
//@Composable
//fun MedTrackNavigation(modifier: Modifier = Modifier){
//    val navController = rememberNavController()
//
//    // The startDestination ensures the Welcome screen shows up first
//    NavHost(
//        navController = navController,
//
//        // ensure welcome screen shows up first
//        startDestination = "welcome",
//        modifier = modifier
//    ) {
//        composable("welcome") {
//            WelcomeScreen(
//                onNavigateToLogin = { navController.navigate("login") }
//            )
//        }
//        composable("login") {
//            LoginScreen(
//                onLoginSuccess = { patientId ->
//                    navController.navigate("home")
//                }
//            )
//        }
//    }
//}
