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
import com.garent.s35123656.medtrack.data.repository.PatientRepository
import com.garent.s35123656.medtrack.data.repository.SymptomRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.net.SocketTimeoutException
import retrofit2.HttpException

class MedCoachViewModel(
    private val drugRepository: DrugRepository,
    private val medicationRepository: MedicationRepository,
    private val medCoachRepository: MedCoachRepository,
    private val symptomRepository: SymptomRepository,
    private val patientRepository: PatientRepository
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

    /**
     * Clears all session-specific data and AI generated outputs.
     */
    fun clearSessionData() {
        currentTip = null
        tipErrorMessage = null
        searchQuery = ""
        drugInfo = null
        drugErrorMessage = null
        patientMedications = emptyList()
        tipHistory = emptyList()
    }

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
                    drugErrorMessage = "No information found for \"$name\". Please check the spelling."
                }
                isLoadingDrug = false
            }.onFailure { error ->
                drugErrorMessage = when (error) {
                    is UnknownHostException -> "No internet connection. Please check your network."
                    is SocketTimeoutException -> "The request timed out. Please try again."
                    is HttpException -> {
                        if (error.code() == 404) "Medication not found."
                        else "FDA Service error (Code: ${error.code()})."
                    }
                    else -> "An unexpected error occurred."
                }
                isLoadingDrug = false
            }
        }
    }

    fun generateNewTip(patientId: String) {
        viewModelScope.launch {
            isLoadingTip = true
            tipErrorMessage = null
            currentTip = null
            
            try {
                val name = patientRepository.getPatientNameById(patientId) ?: "User"
                val meds = medicationRepository.getMedicationsForPatient(patientId).first()
                val symptoms = symptomRepository.getSymptomsForPatient(patientId).first()
                
                val result = medCoachRepository.generateTip(patientId, name, meds, symptoms)
                result.onSuccess { tip ->
                    currentTip = tip
                    isLoadingTip = false
                    loadTipHistory(patientId)
                }.onFailure { error ->
                    tipErrorMessage = when (error) {
                        is UnknownHostException -> "No internet connection. Please check your network and try again."
                        is SocketTimeoutException -> "The request timed out. Please try again later."
                        is HttpException -> {
                            when (error.code()) {
                                429 -> "Rate limit reached. Please wait a moment before trying again."
                                403 -> "API Access denied. Please check configuration."
                                else -> "AI Service error (Code: ${error.code()})."
                            }
                        }
                        else -> "AI Coach is currently unavailable. Please try again soon."
                    }
                    isLoadingTip = false
                }
            } catch (e: Exception) {
                tipErrorMessage = "Error preparing data for AI. Please try again."
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
        private val medCoachRepository: MedCoachRepository,
        private val symptomRepository: SymptomRepository,
        private val patientRepository: PatientRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MedCoachViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MedCoachViewModel(drugRepository, medicationRepository, medCoachRepository, symptomRepository, patientRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
