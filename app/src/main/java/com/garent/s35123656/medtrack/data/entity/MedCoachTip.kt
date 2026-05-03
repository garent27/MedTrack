package com.garent.s35123656.medtrack.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "med_coach_tips")
data class MedCoachTip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: String,
    val tipMessage: String,
    val timestamp: Long = System.currentTimeMillis()
)
