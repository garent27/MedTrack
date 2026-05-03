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
        if (name.isBlank()) {
            drugErrorMessage = "Please enter a medication name."
            drugInfo = null
            return
        }
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
                    // Handle case where API returns 200 but results are empty
                    drugErrorMessage = "No information found for \"$name\". Please check the spelling."
                }
                isLoadingDrug = false
            }.onFailure { error ->
                // Specific error handling for network and API failures
                drugErrorMessage = when (error) {
                    is java.net.UnknownHostException ->
                        "No internet connection. Please check your network and try again."
                    is java.net.SocketTimeoutException ->
                        "The request timed out. The FDA server might be busy, please try again."
                    is retrofit2.HttpException -> {
                        if (error.code() == 404) "Medication not found. Please check the drug name spelling."
                        else "FDA Service error (Code: ${error.code()}). Please try again later."
                    }
                    else -> "An unexpected error occurred while searching for \"$name\"."
                }
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
