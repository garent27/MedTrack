package com.garent.s35123656.medtrack.data.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.garent.s35123656.medtrack.data.repository.MedicationRepository
import com.garent.s35123656.medtrack.data.repository.PatientRepository
import com.garent.s35123656.medtrack.data.repository.SymptomRepository
import kotlinx.coroutines.launch

class ClinicianViewModel(
    private val patientRepo: PatientRepository,
    private val medicationRepo: MedicationRepository,
    private val symptomRepo: SymptomRepository
) : ViewModel() {

    var totalPatients by mutableStateOf(0)
    var avgMedsPerPatient by mutableStateOf(0.0)
    var commonSymptom by mutableStateOf("N/A")
    var avgSymptomSeverity by mutableStateOf(0.0)
    var isLoading by mutableStateOf(false)

    fun loadStatistics() {
        viewModelScope.launch {
            isLoading = true
            totalPatients = patientRepo.getTotalPatients()
            
            val totalMeds = medicationRepo.getTotalMedicationsCount()
            avgMedsPerPatient = if (totalPatients > 0) totalMeds.toDouble() / totalPatients else 0.0
            
            commonSymptom = symptomRepo.getMostCommonCategory() ?: "N/A"
            avgSymptomSeverity = symptomRepo.getAverageSeverity() ?: 0.0
            
            isLoading = false
        }
    }

    class ClinicianViewModelFactory(
        private val patientRepo: PatientRepository,
        private val medicationRepo: MedicationRepository,
        private val symptomRepo: SymptomRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ClinicianViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ClinicianViewModel(patientRepo, medicationRepo, symptomRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
