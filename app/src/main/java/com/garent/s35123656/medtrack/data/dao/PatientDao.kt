package com.garent.s35123656.medtrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.garent.s35123656.medtrack.data.entity.Patient

@Dao
interface PatientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatient(patient: Patient)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPatients(patients: List<Patient>)

    @Update
    suspend fun updatePatient(patient: Patient)

    @Query("SELECT * FROM patients WHERE phoneNumber = :phone")
    suspend fun getPatientByPhone(phone: String): Patient?

    @Query("SELECT * FROM patients WHERE patientId = :id")
    suspend fun getPatientById(id: String): Patient?

    @Query("SELECT name FROM patients WHERE patientId = :id")
    suspend fun getPatientNameById(id: String): String?

    @Query("SELECT patientId FROM patients ORDER BY patientId DESC LIMIT 1")
    suspend fun getLastPatientId(): String?

    @Query("SELECT * FROM patients WHERE patientId = :id AND phoneNumber = :phone")
    suspend fun getPatientByIdAndPhone(id: String, phone: String): Patient?
}
