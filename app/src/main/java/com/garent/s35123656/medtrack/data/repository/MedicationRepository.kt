package com.garent.s35123656.medtrack.data.repository

import com.garent.s35123656.medtrack.data.dao.MedicationDao
import com.garent.s35123656.medtrack.data.entity.Medication
import kotlinx.coroutines.flow.Flow

class MedicationRepository(private val medicationDao: MedicationDao) {
    suspend fun insertMedication(medication: Medication) = medicationDao.insertMedication(medication)
    fun getMedicationsForPatient(patientId: String): Flow<List<Medication>> = medicationDao.getMedicationsForPatient(patientId)
    suspend fun updateMedicationStatus(medId: Int, isTaken: Boolean) = medicationDao.updateMedicationStatus(medId, isTaken)
    suspend fun resetAllMedicationsStatus(patientId: String) = medicationDao.resetAllMedicationsStatus(patientId)
    suspend fun getTotalMedicationsCount() = medicationDao.getTotalMedicationsCount()
}
