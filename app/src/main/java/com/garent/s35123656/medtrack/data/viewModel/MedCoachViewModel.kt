package com.garent.s35123656.medtrack.data.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.garent.s35123656.medtrack.data.remote.DrugInfo
import com.garent.s35123656.medtrack.data.repository.DrugRepository
import com.garent.s35123656.medtrack.data.repository.MedicationRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MedCoachViewModel(
    private val drugRepository: DrugRepository,
    private val medicationRepository: MedicationRepository
) : ViewModel() {

    var searchQuery by mutableStateOf("")
    var drugInfo by mutableStateOf<DrugInfo?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var patientMedications by mutableStateOf<List<String>>(emptyList())

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
            isLoading = true
            errorMessage = null
            drugInfo = null
            
            val result = drugRepository.getDrugDetails(name)
            result.onSuccess { info ->
                if (info != null) {
                    drugInfo = info
                } else {
                    errorMessage = "Drug not found in FDA database."
                }
                isLoading = false
            }.onFailure { error ->
                errorMessage = "Network error: ${error.message ?: "Unknown error"}"
                isLoading = false
            }
        }
    }

    class MedCoachViewModelFactory(
        private val drugRepository: DrugRepository,
        private val medicationRepository: MedicationRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MedCoachViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MedCoachViewModel(drugRepository, medicationRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
