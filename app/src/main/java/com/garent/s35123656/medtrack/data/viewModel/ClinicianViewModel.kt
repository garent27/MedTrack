package com.garent.s35123656.medtrack.data.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.garent.s35123656.medtrack.data.repository.MedCoachRepository
import com.garent.s35123656.medtrack.data.repository.MedicationRepository
import com.garent.s35123656.medtrack.data.repository.PatientRepository
import com.garent.s35123656.medtrack.data.repository.SymptomRepository
import kotlinx.coroutines.launch

class ClinicianViewModel(
    private val patientRepo: PatientRepository,
    private val medicationRepo: MedicationRepository,
    private val symptomRepo: SymptomRepository,
    private val medCoachRepo: MedCoachRepository
) : ViewModel() {

    var totalPatients by mutableStateOf(0)
    var avgMedsPerPatient by mutableStateOf(0.0)
    var commonSymptom by mutableStateOf("N/A")
    var avgSymptomSeverity by mutableStateOf(0.0)
    var isLoading by mutableStateOf(false)

    // AI Insights State
    var aiInsights by mutableStateOf<List<String>>(emptyList())
    var isFindingPatterns by mutableStateOf(false)
    var patternErrorMessage by mutableStateOf<String?>(null)

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

    fun findAiPatterns() {
        viewModelScope.launch {
            isFindingPatterns = true
            patternErrorMessage = null
            
            val result = medCoachRepo.findPatterns(
                totalPatients = totalPatients,
                avgMeds = avgMedsPerPatient,
                commonSymptom = commonSymptom,
                avgSeverity = avgSymptomSeverity
            )
            
            result.onSuccess { insights ->
                aiInsights = insights
                isFindingPatterns = false
            }.onFailure { error ->
                patternErrorMessage = "AI Analysis failed: ${error.message}"
                isFindingPatterns = false
            }
        }
    }

    class ClinicianViewModelFactory(
        private val patientRepo: PatientRepository,
        private val medicationRepo: MedicationRepository,
        private val symptomRepo: SymptomRepository,
        private val medCoachRepo: MedCoachRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ClinicianViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ClinicianViewModel(patientRepo, medicationRepo, symptomRepo, medCoachRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
