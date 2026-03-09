package com.garent.s35123656.medtrack
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.compose.ui.Modifier

@Composable
fun MedTrackNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    // NavHost is the "Stage" where your screens are swapped out
    NavHost(
        navController = navController,
        startDestination = "welcome", // This screen shows up first
        modifier = modifier
    ) {
        // 1. Welcome Screen
        composable("welcome") {
            WelcomeScreen(
                onNavigateToLogin = { navController.navigate("login") }
            )
        }

        // 2. Login Screen
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    // After login, we clear the backstack so the user
                    // can't "back button" back into the login screen.
                    navController.navigate("home") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }

//        // 3. Home Screen
//        composable("home") {
//            HomeScreen()
//        }
    }
}