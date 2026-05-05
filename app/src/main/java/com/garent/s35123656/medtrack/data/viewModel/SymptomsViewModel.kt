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
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * ViewModel for the Symptoms screen.
 * Handles loading symptoms history, saving new logs, and calculating trend data.
 */
class SymptomsViewModel(private val symptomRepo: SymptomRepository) : ViewModel() {

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

    fun getSymptoms(patientId: String): Flow<List<Symptom>> {
        return symptomRepo.getSymptomsForPatient(patientId)
    }

    /**
     * Processes symptom history into trend data (Date -> Average Severity).
     * This demonstrates the "Model" processing for the Trend Chart feature.
     */
    fun getSymptomTrends(patientId: String): Flow<List<Pair<String, Float>>> {
        return symptomRepo.getSymptomsForPatient(patientId).map { symptoms ->
            symptoms.asReversed() // Older data first for the chart
                .groupBy { it.dateTime.split(" ").firstOrNull() ?: "" }
                .map { (date, dailySymptoms) ->
                    val avgSeverity = dailySymptoms.map { it.severity.toFloatOrNull() ?: 0f }.average().toFloat()
                    // Simplify date to dd/MM for chart labels
                    val simpleDate = date.split("/").take(2).joinToString("/")
                    simpleDate to avgSeverity
                }
                .takeLast(7) // Show last 7 days of entries
        }
    }

    fun addSymptom(symptom: Symptom, onComplete: () -> Unit) {
        viewModelScope.launch {
            symptomRepo.insertSymptom(symptom)
            clearForm()
            onComplete()
        }
    }

    class SymptomsViewModelFactory(private val symptomRepo: SymptomRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SymptomsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SymptomsViewModel(symptomRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
