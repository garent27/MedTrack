package com.garent.s35123656.medtrack.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.garent.s35123656.medtrack.data.entity.MedCoachTip
import kotlinx.coroutines.flow.Flow

@Dao
interface MedCoachTipDao {
    @Insert
    suspend fun insertTip(tip: MedCoachTip)

    @Query("SELECT * FROM med_coach_tips WHERE patientId = :patientId ORDER BY timestamp DESC")
    fun getTipsForPatient(patientId: String): Flow<List<MedCoachTip>>
}
