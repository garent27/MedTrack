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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    // --- Event Flow for UI Navigation & Toasts ---
    private val _addSymptomResult = MutableSharedFlow<SymptomResult>()
    val addSymptomResult = _addSymptomResult.asSharedFlow()

    sealed class SymptomResult {
        object Success : SymptomResult()
        data class Error(val message: String) : SymptomResult()
    }

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
     * Validates input and adds a new symptom to the database.
     */
    fun saveSymptom(patientId: String) {
        viewModelScope.launch {
            if (selectedCategory == "Select Category") {
                _addSymptomResult.emit(SymptomResult.Error("Please select a category!"))
                return@launch
            }
            if (dateTime == "Select Date & Time") {
                _addSymptomResult.emit(SymptomResult.Error("When did this symptom occur?"))
                return@launch
            }

            val newSymptom = Symptom(
                patientId = patientId,
                category = selectedCategory,
                severity = severity.toInt().toString(),
                notes = notes,
                dateTime = dateTime
            )

            symptomRepo.insertSymptom(newSymptom)
            clearForm()
            _addSymptomResult.emit(SymptomResult.Success)
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
