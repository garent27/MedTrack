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

class HomeViewModel(
    private val patientRepo: PatientRepository,
    private val medicationRepo: MedicationRepository
) : ViewModel() {

    // 1. UI State: Holds the patient name. We prevent external modification using 'private set'[cite: 24].
    var patientName by mutableStateOf("Loading...")
        private set

    // 2. Fetch data from Repo: Get medications as a reactive Flow[cite: 23]
    fun getMedications(patientId: String): Flow<List<Medication>> {
        return medicationRepo.getMedicationsForPatient(patientId)
    }

    // 3. Database Operations: Run asynchronously using coroutines[cite: 23, 24]
    fun loadPatientName(patientId: String) {
        viewModelScope.launch {
            patientName = patientRepo.getPatientNameById(patientId) ?: "Unknown User"
        }
    }

    fun toggleMedicationTaken(medId: Int, isTaken: Boolean) {
        viewModelScope.launch {
            medicationRepo.updateMedicationStatus(medId, isTaken)
        }
    }

    fun addMedication(medication: Medication) {
        viewModelScope.launch {
            medicationRepo.insertMedication(medication)
        }
    }

    // 4. The Factory to build this ViewModel[cite: 23]
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