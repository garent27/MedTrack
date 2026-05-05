package com.garent.s35123656.medtrack.data.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.garent.s35123656.medtrack.data.entity.Symptom
import com.garent.s35123656.medtrack.data.repository.SymptomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

/**
 * ViewModel for the Symptoms screen.
 * handles loading symptom history and logging new symptoms.
 */
class SymptomsViewModel(
    private val symptomRepo: SymptomRepository
) : ViewModel() {

    // --- Symptoms Form State ---
    var selectedCategory by mutableStateOf("Select Category")
    var severity by mutableFloatStateOf(5f)
    var notes by mutableStateOf("")
    var dateTime by mutableStateOf("Select Date & Time")
    var isExpanded by mutableStateOf(false)

    /**
     * Resets the symptoms form fields.
     */
    fun clearForm() {
        selectedCategory = "Select Category"
        severity = 5f
        notes = ""
        dateTime = "Select Date & Time"
        isExpanded = false
    }

    /**
     * Exposes symptom history for a specific patient.
     */
    fun getSymptoms(patientId: String): Flow<List<Symptom>> {
        return symptomRepo.getSymptomsForPatient(patientId)
    }

    /**
     * Adds a new symptom to the database.
     */
    fun addSymptom(symptom: Symptom, onComplete: () -> Unit) {
        viewModelScope.launch {
            symptomRepo.insertSymptom(symptom)
            clearForm()
            onComplete()
        }
    }

    class SymptomsViewModelFactory(
        private val symptomRepo: SymptomRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SymptomsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SymptomsViewModel(symptomRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
