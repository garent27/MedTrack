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

    fun getTipHistory(patientId: String): Flow<List<MedCoachTip>> {
        return medCoachTipDao.getTipsForPatient(patientId)
    }
}
