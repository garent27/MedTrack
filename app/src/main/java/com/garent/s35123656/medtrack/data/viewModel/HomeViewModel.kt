package com.garent.s35123656.medtrack.data.viewModel

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
     * Adds a new medication to the database asynchronously.
     * Executes the onComplete callback upon success.
     */
    fun addMedication(medication: Medication, onComplete: () -> Unit) {
        viewModelScope.launch {
            medicationRepo.insertMedication(medication)
            clearAddMedicationForm()
            onComplete()
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