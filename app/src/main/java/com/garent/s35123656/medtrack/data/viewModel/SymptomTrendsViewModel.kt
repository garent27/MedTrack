package com.garent.s35123656.medtrack.data.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.garent.s35123656.medtrack.data.repository.MedCoachRepository
import com.garent.s35123656.medtrack.data.repository.SymptomRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import java.net.SocketTimeoutException

/**
 * ViewModel for the Symptom Trends screen.
 * Handles processing trend data and managing GenAI trend analysis.
 */
class SymptomTrendsViewModel(
    private val symptomRepo: SymptomRepository,
    private val medCoachRepo: MedCoachRepository
) : ViewModel() {

    // --- GenAI Trend Question State ---
    var symptomToAnalyze by mutableStateOf("")
    var trendQuestion by mutableStateOf("")
    var trendAnswer by mutableStateOf<String?>(null)
    var isAnalyzingTrends by mutableStateOf(false)
    var trendErrorMessage by mutableStateOf<String?>(null)

    /**
     * Clears GenAI generated response and related states.
     */
    fun clearAiOutput() {
        trendAnswer = null
        trendQuestion = ""
        symptomToAnalyze = ""
        trendErrorMessage = null
    }

    /**
     * Processes symptom history into trend data (Date -> Average Severity).
     */
    fun getSymptomTrends(patientId: String): Flow<List<Pair<String, Float>>> {
        return symptomRepo.getSymptomsForPatient(patientId).map { symptoms ->
            symptoms.asReversed() 
                .groupBy { it.dateTime.split(" ").firstOrNull() ?: "" }
                .map { (date, dailySymptoms) ->
                    val avgSeverity = dailySymptoms.map { it.severity.toFloatOrNull() ?: 0f }.average().toFloat()

                    // MVVM LOGIC: Format "YYYY-MM-DD" into short "DD/MM" for the UI chart
                    val parts = date.split("-")
                    val simpleDate = if (parts.size == 3) {
                        "${parts[2]}/${parts[1]}" // Converts "2026-03-19" to "19/03"
                    } else {
                        date // Fallback just in case
                    }

                    simpleDate to avgSeverity
                }
                .takeLast(7)
        }
    }

    /**
     * Sends the trend data, full symptom history context, and user question to the GenAI model.
     * Includes user-friendly error handling for network or API issues.
     */
    fun askAiAboutTrends(patientId: String) {
        if (trendQuestion.isBlank()) return

        viewModelScope.launch {
            isAnalyzingTrends = true
            trendErrorMessage = null
            trendAnswer = null
            
            try {
                // Fetch current trends and all symptoms for full context
                val trends = getSymptomTrends(patientId).first()
                val allSymptoms = symptomRepo.getSymptomsForPatient(patientId).first()
                
                val result = medCoachRepo.askQuestionAboutTrends(trendQuestion, allSymptoms, trends)
                
                result.onSuccess { answer ->
                    trendAnswer = answer
                    isAnalyzingTrends = false
                }.onFailure { error ->
                    trendErrorMessage = when (error) {
                        is UnknownHostException -> "No internet connection. Please check your network and try again."
                        is SocketTimeoutException -> "The request timed out. Please try again later."
                        else -> "AI Expert is currently unavailable. Please try again soon."
                    }
                    isAnalyzingTrends = false
                }
            } catch (e: Exception) {
                trendErrorMessage = "Error gathering health logs. Please try again."
                isAnalyzingTrends = false
            }
        }
    }

    class SymptomTrendsViewModelFactory(
        private val symptomRepo: SymptomRepository,
        private val medCoachRepo: MedCoachRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SymptomTrendsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SymptomTrendsViewModel(symptomRepo, medCoachRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
