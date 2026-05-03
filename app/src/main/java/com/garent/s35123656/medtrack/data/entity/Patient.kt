package com.garent.s35123656.medtrack.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey val patientId: String, // e.g., "P1001"
    val phoneNumber: String,
    val name: String,
    val password: String
)