package com.garent.s35123656.medtrack.data.repository

import com.garent.s35123656.medtrack.Symptom
import com.garent.s35123656.medtrack.data.dao.SymptomDao
import kotlinx.coroutines.flow.Flow

class SymptomRepository(private val symptomDao: SymptomDao) {
    suspend fun insertSymptom(symptom: Symptom) = symptomDao.insertSymptom(symptom)
    fun getSymptomsForPatient(patientId: String): Flow<List<Symptom>> = symptomDao.getSymptomsForPatient(patientId)
}