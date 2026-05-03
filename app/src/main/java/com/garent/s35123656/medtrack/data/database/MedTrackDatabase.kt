package com.garent.s35123656.medtrack.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.garent.s35123656.medtrack.data.dao.MedicationDao
import com.garent.s35123656.medtrack.data.dao.PatientDao
import com.garent.s35123656.medtrack.data.dao.SymptomDao
import com.garent.s35123656.medtrack.data.entity.Medication
import com.garent.s35123656.medtrack.data.entity.Patient
import com.garent.s35123656.medtrack.data.entity.Symptom
@Database(entities = [Patient::class, Medication::class, Symptom::class], version = 1, exportSchema = false)
abstract class MedTrackDatabase : RoomDatabase() {

    abstract fun patientDao(): PatientDao
    abstract fun medicationDao(): MedicationDao
    abstract fun symptomDao(): SymptomDao

    companion object {
        @Volatile
        private var Instance: MedTrackDatabase? = null

        fun getDatabase(context: Context): MedTrackDatabase {
            return Instance ?: synchronized(this) {
                Room.databaseBuilder(context, MedTrackDatabase::class.java, "medtrack_database")
                    .build().also { Instance = it }
            }
        }
    }
}