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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.garent.s35123656.medtrack.data.database.MedTrackDatabase
import com.garent.s35123656.medtrack.data.repository.MedicationRepository
import com.garent.s35123656.medtrack.data.repository.PatientRepository
import com.garent.s35123656.medtrack.data.viewModel.HomeViewModel
import com.garent.s35123656.medtrack.ui.theme.MedTrackTheme
import kotlin.jvm.java

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MedTrackTheme {

                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val context = LocalContext.current

                // 1. Session Check
                val sharedPref = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
                var loggedInId by remember { mutableStateOf(sharedPref.getString("logged_in_id", null)) }

                // 2. Track current route to show/hide bars
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "welcome"


                // 1. Initialize Database and Repositories (The "Model" and "Repo" layers)
                val db = MedTrackDatabase.getDatabase(context)
                val patientRepo = PatientRepository(db.patientDao())
                val medicationRepo = MedicationRepository(db.medicationDao())

                // 2. Create the ViewModel using the Factory
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.HomeViewModelFactory(patientRepo, medicationRepo)
                )

                // Run the seeder silently in the background
                LaunchedEffect(Unit) {
                    com.garent.s35123656.medtrack.data.seedDatabaseOnFirstLaunch(context, db)
                }

                Scaffold(
                    topBar = {
                        // Only show TopBar on Home
                        if (currentRoute == "home") {
                            MedTrackTopBar(onLogout = {
                                // Clear session and navigate back to welcome
                                sharedPref.edit().remove("logged_in_id").apply()
                                loggedInId = null
                                navController.navigate("welcome") {
                                    popUpTo(0) { inclusive = true } // Wipe history
                                }
                            })
                        }
                    },
                    bottomBar = {
                        // Show BottomBar only on Home and Symptoms
                        if (currentRoute == "home" || currentRoute == "symptoms") {
                            MedTrackBottomBar(
                                currentScreen = currentRoute,
                                patientId = loggedInId ?: "",
                                navController = navController
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    floatingActionButton = {
                        // Shows logout fab only in home
                        if (currentRoute == "home") {
                            AddMedicationFAB(onClick = {
                                navController.navigate("add_medication")
                            })
                        }
                    }
                ) { innerPadding ->

                    // Call extracted NavHost component here
                    MedTrackNavHost(
                        navController = navController,
                        innerPadding = innerPadding,
                        patientId = loggedInId ?: "",
                        onLoginSuccess = { newId ->
                            loggedInId = newId
                            navController.navigate("home") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        },
                        snackbarHostState = snackbarHostState
                    )

                }
            }
        }
    }
}


/**
 *  Main navigation for welcome, login, home and symptoms
 */
@Composable
fun MedTrackNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    patientId: String,
    onLoginSuccess: (String) -> Unit,
    snackbarHostState: SnackbarHostState
) {
    // for navigation between welcome, login, home and symptoms
    NavHost(
        navController = navController,

        // set start destination
        startDestination = if (patientId.isNotEmpty()) "home" else "welcome",
        modifier = Modifier.padding(innerPadding)
    ) {
        composable("welcome") {
            WelcomeScreen(navController = navController)
        }
        composable("login") {

            Login(navController = navController, onLoginSuccess = onLoginSuccess)
        }
        composable("signup") {
             SignUp(navController = navController, snackbarHostState = snackbarHostState)
        }
        composable("home") {
            Home(patientId = patientId, viewModel = homeViewModel)
        }
        composable("symptoms") {
            Symptoms(
                patientId = patientId,
                snackbarHostState = snackbarHostState
            )
        }
        composable("add_medication") {
            AddMedication(
                patientId = patientId,
                navController = navController,
                snackbarHostState = snackbarHostState,
                viewModel = homeViewModel
            )
        }
    }
}

/**
 *  Main welcome screen
 */
@Composable
fun WelcomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Med track Image
        Image(
            painter = painterResource(id = R.drawable.medtrack),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Medtrack title
        Text(
            text = "MedTrack",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // description
        Text(
            text = "This app is for tracking purposes only and does not replace professional medical advice.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        // goes to monash health webstie
        TextButton(onClick = { uriHandler.openUri("https://www.monashhealth.org") }) {
            Text("Visit Monash Health Clinic")
        }

        Spacer(modifier = Modifier.height(24.dp))

        // login button
        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // sign up button
        Button(
            onClick = { navController.navigate("signup") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.weight(1f))

        // my detail text
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
        // Must provide a dummy NavController for the preview to compile
        WelcomeScreen(navController = rememberNavController())
    }
}