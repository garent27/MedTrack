package com.garent.s35123656.medtrack.data.repository

import com.garent.s35123656.medtrack.data.dao.PatientDao
import com.garent.s35123656.medtrack.data.entity.Patient

class PatientRepository(private val patientDao: PatientDao) {
    suspend fun insertPatient(patient: Patient) = patientDao.insertPatient(patient)
    suspend fun updatePatient(patient: Patient) = patientDao.updatePatient(patient)
    suspend fun getPatientByPhone(phone: String) = patientDao.getPatientByPhone(phone)
    suspend fun getPatientById(id: String) = patientDao.getPatientById(id)
    suspend fun getPatientNameById(id: String) = patientDao.getPatientNameById(id)
    suspend fun getLastPatientId() = patientDao.getLastPatientId()
    suspend fun getPatientByIdAndPhone(id: String, phone: String) = patientDao.getPatientByIdAndPhone(id, phone)
    suspend fun getTotalPatients() = patientDao.getTotalPatients()
}
