package com.garent.s35123656.medtrack

import android.content.Context
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
import com.garent.s35123656.medtrack.data.repository.DrugRepository
import com.garent.s35123656.medtrack.data.repository.MedicationRepository
import com.garent.s35123656.medtrack.data.repository.PatientRepository
import com.garent.s35123656.medtrack.data.repository.SymptomRepository
import com.garent.s35123656.medtrack.data.viewModel.HomeViewModel
import com.garent.s35123656.medtrack.data.viewModel.LoginViewModel
import com.garent.s35123656.medtrack.data.viewModel.SignUpViewModel
import com.garent.s35123656.medtrack.data.viewModel.SymptomsViewModel
import com.garent.s35123656.medtrack.data.viewModel.SettingsViewModel
import com.garent.s35123656.medtrack.data.viewModel.ClaimAccountViewModel
import com.garent.s35123656.medtrack.data.viewModel.MedCoachViewModel
import com.garent.s35123656.medtrack.ui.theme.MedTrackTheme

/**
 * MainActivity serves as the entry point of the application.
 * It initializes the database, repositories, and root ViewModels.
 * It also manages the overall layout (Scaffold) and navigation (NavHost).
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MedTrackTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                val context = LocalContext.current

                // 1. Session Check (Simple session management using SharedPreferences)
                val sharedPref = remember { context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE) }
                var loggedInId by remember { mutableStateOf(sharedPref.getString("logged_in_id", null)) }

                // 2. Track current route to show/hide UI bars (TopBar, BottomBar, FAB)
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route ?: "welcome"

                // 3. Initialize Database and Repositories (The "Model" layer)
                val db = MedTrackDatabase.getDatabase(context)
                val patientRepo = PatientRepository(db.patientDao())
                val medicationRepo = MedicationRepository(db.medicationDao())
                val symptomRepo = SymptomRepository(db.symptomDao())
                val drugRepo = DrugRepository()

                // 4. Initialize ViewModels (The "ViewModel" layer)
                val homeViewModel: HomeViewModel = viewModel(
                    factory = HomeViewModel.HomeViewModelFactory(patientRepo, medicationRepo)
                )
                val loginViewModel: LoginViewModel = viewModel(
                    factory = LoginViewModel.LoginViewModelFactory(patientRepo)
                )
                val signUpViewModel: SignUpViewModel = viewModel(
                    factory = SignUpViewModel.SignUpViewModelFactory(patientRepo)
                )
                val symptomsViewModel: SymptomsViewModel = viewModel(
                    factory = SymptomsViewModel.SymptomsViewModelFactory(symptomRepo)
                )
                val settingsViewModel: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.SettingsViewModelFactory(patientRepo)
                )
                val claimAccountViewModel: ClaimAccountViewModel = viewModel(
                    factory = ClaimAccountViewModel.ClaimAccountViewModelFactory(patientRepo)
                )
                val medCoachViewModel: MedCoachViewModel = viewModel(
                    factory = MedCoachViewModel.MedCoachViewModelFactory(drugRepo, medicationRepo)
                )

                // Run the database seeder on first launch
                LaunchedEffect(Unit) {
                    com.garent.s35123656.medtrack.data.seedDatabaseOnFirstLaunch(context, db)
                }

                val onLogout = {
                    sharedPref.edit().remove("logged_in_id").apply()
                    loggedInId = null
                    navController.navigate("welcome") {
                        popUpTo(0) { inclusive = true }
                    }
                }

                Scaffold(
                    topBar = {
                        // TopBar is only visible on the Home screen
                        if (currentRoute == "home") {
                            MedTrackTopBar(onLogout = onLogout)
                        }
                    },
                    bottomBar = {
                        // BottomBar is visible on core screens
                        val coreScreens = listOf("home", "symptoms", "med_coach", "settings")
                        if (currentRoute in coreScreens) {
                            MedTrackBottomBar(
                                currentScreen = currentRoute,
                                patientId = loggedInId ?: "",
                                navController = navController
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    floatingActionButton = {
                        // FAB for adding medication, visible only on the Home screen
                        if (currentRoute == "home") {
                            AddMedicationFAB(onClick = {
                                navController.navigate("add_medication")
                            })
                        }
                    }
                ) { innerPadding ->
                    // Root Navigation Host
                    MedTrackNavHost(
                        navController = navController,
                        innerPadding = innerPadding,
                        patientId = loggedInId ?: "",
                        homeViewModel = homeViewModel,
                        loginViewModel = loginViewModel,
                        signUpViewModel = signUpViewModel,
                        symptomsViewModel = symptomsViewModel,
                        settingsViewModel = settingsViewModel,
                        claimAccountViewModel = claimAccountViewModel,
                        medCoachViewModel = medCoachViewModel,
                        onLoginSuccess = { newId ->
                            loggedInId = newId
                            navController.navigate("home") {
                                popUpTo("welcome") { inclusive = true }
                            }
                        },
                        onLogout = onLogout,
                        snackbarHostState = snackbarHostState
                    )
                }
            }
        }
    }
}

/**
 * Main navigation host defining the screens and passing necessary ViewModels.
 */
@Composable
fun MedTrackNavHost(
    navController: NavHostController,
    innerPadding: PaddingValues,
    patientId: String,
    homeViewModel: HomeViewModel,
    loginViewModel: LoginViewModel,
    signUpViewModel: SignUpViewModel,
    symptomsViewModel: SymptomsViewModel,
    settingsViewModel: SettingsViewModel,
    claimAccountViewModel: ClaimAccountViewModel,
    medCoachViewModel: MedCoachViewModel,
    onLoginSuccess: (String) -> Unit,
    onLogout: () -> Unit,
    snackbarHostState: SnackbarHostState
) {
    NavHost(
        navController = navController,
        startDestination = if (patientId.isNotEmpty()) "home" else "welcome",
        modifier = Modifier.padding(innerPadding)
    ) {
        composable("welcome") {
            WelcomeScreen(navController = navController)
        }
        composable("login") {
            Login(
                navController = navController,
                viewModel = loginViewModel,
                onLoginSuccess = onLoginSuccess
            )
        }
        composable("signup") {
             SignUp(
                 navController = navController,
                 viewModel = signUpViewModel,
                 snackbarHostState = snackbarHostState
             )
        }
        composable("claim_account") {
            ClaimAccount(
                navController = navController,
                viewModel = claimAccountViewModel
            )
        }
        composable("home") {
            Home(patientId = patientId, viewModel = homeViewModel)
        }
        composable("symptoms") {
            Symptoms(
                patientId = patientId,
                viewModel = symptomsViewModel,
                snackbarHostState = snackbarHostState
            )
        }
        composable("med_coach") {
            MedCoach(
                patientId = patientId,
                viewModel = medCoachViewModel
            )
        }
        composable("settings") {
            SettingsScreen(
                patientId = patientId,
                viewModel = settingsViewModel,
                onLogout = onLogout,
                onClinicianLogin = { /* TODO: Navigate to Clinician Login */ }
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
 * Main welcome screen of the application.
 */
@Composable
fun WelcomeScreen(navController: NavController, modifier: Modifier = Modifier) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.medtrack),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "MedTrack",
            fontSize = 42.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "This app is for tracking purposes only and does not replace professional medical advice.",
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        TextButton(onClick = { uriHandler.openUri("https://www.monashhealth.org") }) {
            Text("Visit Monash Health Clinic")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { navController.navigate("signup") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Sign Up")
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "By Garent Ngor Jun Hoe (35123656)",
            style = MaterialTheme.typography.labelLarge,
            color = Color.DarkGray
        )
    }
}
