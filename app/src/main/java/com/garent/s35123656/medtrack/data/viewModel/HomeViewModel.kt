package com.garent.s35123656.medtrack.data.viewModel

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.garent.s35123656.medtrack.data.entity.Medication
import com.garent.s35123656.medtrack.data.repository.MedicationRepository
import com.garent.s35123656.medtrack.data.repository.PatientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * ViewModel for the Home and Add Medication screens.
 * Manages the UI state for the patient and their medications.
 */
class HomeViewModel(
    private val patientRepo: PatientRepository,
    private val medicationRepo: MedicationRepository
) : ViewModel() {

    // 1. UI State: Holds the patient name. Private setter to ensure state is only modified here.
    var patientName by mutableStateOf("Loading...")
        private set

    // --- Add Medication Form State (Persists across rotation) ---
    var medName by mutableStateOf("")
    var dosage by mutableStateOf("")
    var scheduledTime by mutableStateOf("")
    var notes by mutableStateOf("")
    var selectedFreq by mutableStateOf("Once daily")
    var selectedType by mutableStateOf("Tablet")
    var showErrors by mutableStateOf(false)

    /**
     * Check and reset medication if new day
     */
    fun onHomeScreenStarted(patientId: String, context: Context) {
        loadPatientName(patientId)

        viewModelScope.launch {
            val sharedPref = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
            val lastResetDate = sharedPref.getString("last_reset_date_$patientId", "")
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)

            if (lastResetDate != currentDate) {
                resetMedicationsStatus(patientId)
                sharedPref.edit().putString("last_reset_date_$patientId", currentDate).apply()
            }
        }
    }

    /**
     * Resets the add medication form fields to their default values.
     */
    fun clearAddMedicationForm() {
        medName = ""
        dosage = ""
        scheduledTime = ""
        notes = ""
        selectedFreq = "Once daily"
        selectedType = "Tablet"
        showErrors = false
    }

    /**
     * Exposes medications for a specific patient as a reactive Flow.
     */
    fun getMedications(patientId: String): Flow<List<Medication>> {
        return medicationRepo.getMedicationsForPatient(patientId)
    }

    /**
     * Fetches the patient's name asynchronously and updates the UI state.
     */
    fun loadPatientName(patientId: String) {
        viewModelScope.launch {
            patientName = patientRepo.getPatientNameById(patientId) ?: "Unknown User"
        }
    }

    /**
     * Updates the 'taken' status of a medication asynchronously.
     */
    fun toggleMedicationTaken(medId: Int, isTaken: Boolean) {
        viewModelScope.launch {
            medicationRepo.updateMedicationStatus(medId, isTaken)
        }
    }

    /**
     * Resets the 'isTaken' status for all medications of a specific patient.
     */
    fun resetMedicationsStatus(patientId: String) {
        viewModelScope.launch {
            medicationRepo.resetAllMedicationsStatus(patientId)
        }
    }


    /**
     * Factory for creating HomeViewModel instances with required repository dependencies.
     */
    class HomeViewModelFactory(
        private val patientRepo: PatientRepository,
        private val medicationRepo: MedicationRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeViewModel(patientRepo, medicationRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}