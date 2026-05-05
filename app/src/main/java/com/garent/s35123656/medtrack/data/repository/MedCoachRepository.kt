package com.garent.s35123656.medtrack.data.repository

import com.garent.s35123656.medtrack.data.dao.MedCoachTipDao
import com.garent.s35123656.medtrack.data.entity.MedCoachTip
import com.garent.s35123656.medtrack.data.entity.Medication
import com.garent.s35123656.medtrack.data.entity.Symptom
import com.garent.s35123656.medtrack.data.remote.GeminiRequest
import com.garent.s35123656.medtrack.data.remote.GeminiService
import com.garent.s35123656.medtrack.data.remote.Content
import com.garent.s35123656.medtrack.data.remote.Part
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlinx.coroutines.flow.Flow
import java.util.Locale

class MedCoachRepository(private val medCoachTipDao: MedCoachTipDao) {
    private val geminiRetrofit = Retrofit.Builder()
        .baseUrl("https://generativelanguage.googleapis.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val geminiService = geminiRetrofit.create(GeminiService::class.java)
    private val apiKey = "AIzaSyBGZs9YTKXif__XrfsUgKWDa8O_OkY4X4U"

    suspend fun generateTip(
        patientId: String,
        patientName: String,
        medications: List<Medication>,
        symptoms: List<Symptom>
    ): Result<String> {

        val medContext = if (medications.isNotEmpty()) {
            "Patient is currently taking: ${medications.joinToString { "${it.medicationName} (${it.frequency})" }}."
        } else {
            "Patient has no medications currently recorded."
        }

        val symptomContext = if (symptoms.isNotEmpty()) {
            "Recently reported symptoms: ${symptoms.joinToString { "${it.category} (Severity: ${it.severity}/10)" }}."
        } else {
            "Patient hasn't reported any symptoms recently."
        }

        val prompt = """
            Generate a short, encouraging, and highly personalized health message for a patient named $patientName.
            Context:
            $medContext
            $symptomContext
            
            Instruction: Use this context to provide a supportive tip that helps them stay on top of their medication schedule. 
            If they have symptoms, acknowledge them gently without giving medical advice.
            Keep the message under 100 words.
            always add "(:" at the end of the message.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        return try {
            val response = geminiService.generateContent(apiKey, request)
            val tipText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (tipText != null) {
                // Save to database
                val tip = MedCoachTip(patientId = patientId, tipMessage = tipText)
                medCoachTipDao.insertTip(tip)
                Result.success(tipText)
            } else {
                Result.failure(Exception("No tip generated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun askQuestionAboutTrends(
        question: String,
        symptoms: List<Symptom>,
        trends: List<Pair<String, Float>>
    ): Result<String> {
        val trendContext = trends.joinToString("\n") { "- Date: ${it.first}, Avg Severity: ${it.second}/10" }
        val symptomsContext = if (symptoms.isNotEmpty()) {
            "User's recently logged symptoms:\n" + symptoms.take(10).joinToString("\n") { "- ${it.category} (Severity: ${it.severity}/10) on ${it.dateTime}" }
        } else {
            "User has no specific symptom logs yet."
        }
        
        val prompt = """
            User Question: "$question"
            
            $symptomsContext
            
            Daily Average Severity Trends (Last 7 entries):
            $trendContext
            
            Instruction: Answer the user's question based on their provided symptom logs and severity trends.
            Provide a supportive, informative response. 
            Do not give specific medical diagnoses or prescriptions. 
            Keep the response concise (under 120 words).
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        return try {
            val response = geminiService.generateContent(apiKey, request)
            val answer = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (answer != null) {
                Result.success(answer)
            } else {
                Result.failure(Exception("No answer generated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun findPatterns(
        totalPatients: Int,
        avgMeds: Double,
        commonSymptom: String,
        avgSeverity: Double
    ): Result<List<String>> {
        val prompt = """
            Analyze the following aggregated patient data from a medical tracking app and provide exactly 3 interesting patterns or observations.
            - Total patients: $totalPatients
            - Average medications per patient: ${String.format(Locale.getDefault(), "%.1f", avgMeds)}
            - Most common symptom category: $commonSymptom
            - Average symptom severity: ${String.format(Locale.getDefault(), "%.1f", avgSeverity)}/10

            Provide exactly 3 insights as a numbered list. Each insight should be a single clear sentence.
        """.trimIndent()

        val request = GeminiRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt))))
        )

        return try {
            val response = geminiService.generateContent(apiKey, request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (text != null) {
                val patterns = text.lines()
                    .map { it.trim() }
                    .filter { it.isNotEmpty() && (it.first().isDigit() || it.startsWith("*") || it.startsWith("-")) }
                    .map { it.replace(Regex("^[^a-zA-Z]+"), "").trim() }
                    .take(3)
                
                if (patterns.size >= 3) {
                    Result.success(patterns)
                } else {
                    val rawPatterns = text.split("\n").filter { it.isNotBlank() }.map { it.replace(Regex("^[^a-zA-Z]+"), "").trim() }.take(3)
                    if (rawPatterns.size >= 3) Result.success(rawPatterns)
                    else Result.failure(Exception("Could not parse patterns"))
                }
            } else {
                Result.failure(Exception("No patterns generated"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getTipHistory(patientId: String): Flow<List<MedCoachTip>> {
        return medCoachTipDao.getTipsForPatient(patientId)
    }
}
