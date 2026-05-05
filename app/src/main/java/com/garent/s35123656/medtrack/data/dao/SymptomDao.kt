package com.garent.s35123656.medtrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.garent.s35123656.medtrack.data.entity.Symptom
import kotlinx.coroutines.flow.Flow

@Dao
interface SymptomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptom(symptom: Symptom)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSymptoms(symptoms: List<Symptom>)

    @Query("SELECT * FROM symptoms WHERE patientId = :patientId ORDER BY dateTime DESC")
    fun getSymptomsForPatient(patientId: String): Flow<List<Symptom>>

    @Query("SELECT category FROM symptoms GROUP BY category ORDER BY COUNT(*) DESC LIMIT 1")
    suspend fun getMostCommonCategory(): String?

    @Query("SELECT AVG(CAST(severity AS FLOAT)) FROM symptoms")
    suspend fun getAverageSeverity(): Double?
}
