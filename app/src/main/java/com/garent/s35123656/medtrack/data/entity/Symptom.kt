package com.garent.s35123656.medtrack.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "symptoms",
    foreignKeys = [ForeignKey(
        entity = Patient::class,
        parentColumns = ["patientId"],
        childColumns = ["patientId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Symptom(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val patientId: String,
    val category: String,
    val severity: String,
    val notes: String,
    val dateTime: String
)