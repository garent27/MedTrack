package com.garent.s35123656.medtrack.data.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.garent.s35123656.medtrack.data.entity.MedCoachTip
import com.garent.s35123656.medtrack.data.remote.DrugInfo
import com.garent.s35123656.medtrack.data.repository.DrugRepository
import com.garent.s35123656.medtrack.data.repository.MedCoachRepository
import com.garent.s35123656.medtrack.data.repository.MedicationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MedCoachViewModel(
    private val drugRepository: DrugRepository,
    private val medicationRepository: MedicationRepository,
    private val medCoachRepository: MedCoachRepository
) : ViewModel() {

    // --- Drug Search State ---
    var searchQuery by mutableStateOf("")
    var drugInfo by mutableStateOf<DrugInfo?>(null)
    var isLoadingDrug by mutableStateOf(false)
    var drugErrorMessage by mutableStateOf<String?>(null)
    var patientMedications by mutableStateOf<List<String>>(emptyList())

    // --- GenAI Tip State ---
    var currentTip by mutableStateOf<String?>(null)
    var isLoadingTip by mutableStateOf(false)
    var tipErrorMessage by mutableStateOf<String?>(null)
    var tipHistory by mutableStateOf<List<MedCoachTip>>(emptyList())
    var showHistoryDialog by mutableStateOf(false)

    fun loadPatientMedications(patientId: String) {
        viewModelScope.launch {
            val meds = medicationRepository.getMedicationsForPatient(patientId).first()
            patientMedications = meds.mapNotNull { it.medicationName }.distinct()
        }
    }

    fun searchDrug(name: String) {
        if (name.isBlank()) return
        searchQuery = name
        viewModelScope.launch {
            isLoadingDrug = true
            drugErrorMessage = null
            drugInfo = null
            
            val result = drugRepository.getDrugDetails(name)
            result.onSuccess { info ->
                if (info != null) {
                    drugInfo = info
                } else {
                    drugErrorMessage = "Drug not found in FDA database."
                }
                isLoadingDrug = false
            }.onFailure { error ->
                drugErrorMessage = "Network error: ${error.message ?: "Unknown error"}"
                isLoadingDrug = false
            }
        }
    }

    fun generateNewTip(patientId: String) {
        viewModelScope.launch {
            isLoadingTip = true
            tipErrorMessage = null
            
            val result = medCoachRepository.generateTip(patientId)
            result.onSuccess { tip ->
                currentTip = tip
                isLoadingTip = false
                loadTipHistory(patientId) // Refresh history
            }.onFailure { error ->
                tipErrorMessage = "Failed to generate tip: ${error.message}"
                isLoadingTip = false
            }
        }
    }

    fun loadTipHistory(patientId: String) {
        viewModelScope.launch {
            medCoachRepository.getTipHistory(patientId).collect { history ->
                tipHistory = history
            }
        }
    }

    class MedCoachViewModelFactory(
        private val drugRepository: DrugRepository,
        private val medicationRepository: MedicationRepository,
        private val medCoachRepository: MedCoachRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MedCoachViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MedCoachViewModel(drugRepository, medicationRepository, medCoachRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
