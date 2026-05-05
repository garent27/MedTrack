package com.garent.s35123656.medtrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.garent.s35123656.medtrack.data.entity.Medication
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: Medication)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedications(medications: List<Medication>)

    @Query("SELECT * FROM medications WHERE patientId = :patientId ORDER BY id DESC")
    fun getMedicationsForPatient(patientId: String): Flow<List<Medication>>

    @Query("UPDATE medications SET isTaken = :isTaken WHERE id = :medId")
    suspend fun updateMedicationStatus(medId: Int, isTaken: Boolean)

    @Query("UPDATE medications SET isTaken = 0 WHERE patientId = :patientId")
    suspend fun resetAllMedicationsStatus(patientId: String)
}
