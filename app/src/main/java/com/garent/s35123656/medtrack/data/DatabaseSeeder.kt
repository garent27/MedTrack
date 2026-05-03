package com.garent.s35123656.medtrack.data

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// --- NEW IMPORTS FOR YOUR FOLDER STRUCTURE ---
import com.garent.s35123656.medtrack.data.database.MedTrackDatabase
import com.garent.s35123656.medtrack.data.entity.Medication
import com.garent.s35123656.medtrack.data.entity.Patient
import com.garent.s35123656.medtrack.data.entity.Symptom

suspend fun seedDatabaseOnFirstLaunch(context: Context, database: MedTrackDatabase) {
    val prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    val isSeeded = prefs.getBoolean("is_db_seeded", false)

    if (isSeeded) return

    withContext(Dispatchers.IO) {
        try {
            val gson = Gson()

            // 1. MIGRATE PATIENTS
            val patientsToInsert = mutableListOf<Patient>()
            context.assets.open("patients.csv").bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val tokens = line.split(",")
                    if (tokens.size >= 4) {
                        // The requirement says NOT to import password from CSV for account claiming
                        // We set password to an empty string so they must claim it
                        patientsToInsert.add(
                            Patient(tokens[0].trim(), tokens[1].trim(), tokens[2].trim(), "")
                        )
                    }
                }
            }
            // Also migrate legacy SharedPreferences users if any (optional based on your A1 implementation)
            val usersPref = context.getSharedPreferences("users", Context.MODE_PRIVATE)
            usersPref.all.values.forEach { json ->
                try {
                    val userType = object : TypeToken<Map<String, String>>() {}.type
                    val userMap: Map<String, String> = gson.fromJson(json.toString(), userType)
                    userMap["PatientID"]?.let { id ->
                        patientsToInsert.add(
                            Patient(id, userMap["PhoneNumber"] ?: "", userMap["Name"] ?: "", userMap["Password"] ?: "")
                        )
                    }
                } catch (e: Exception) { Log.e("Seed", "User Parse Error") }
            }
            database.patientDao().insertPatients(patientsToInsert)

            // 2. MIGRATE MEDICATIONS
            val medsToInsert = mutableListOf<Medication>()
            context.assets.open("medications.csv").bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val tokens = line.split(",")
                    if (tokens.size >= 5) {
                        medsToInsert.add(
                            Medication(
                                patientId = tokens[0].trim(), medicationName = tokens[1].trim(),
                                dosage = tokens[2].trim(), frequency = tokens[3].trim(),
                                scheduledTime = tokens[4].trim(), medicationType = tokens.getOrNull(5)?.trim() ?: "",
                                notes = tokens.getOrNull(6)?.trim() ?: ""
                            )
                        )
                    }
                }
            }
            val medsPref = context.getSharedPreferences("medications", Context.MODE_PRIVATE)
            medsPref.all.values.forEach { json ->
                try {
                    val listType = object : TypeToken<List<Map<String, Any>>>() {}.type
                    val medsList: List<Map<String, Any>> = gson.fromJson(json.toString(), listType)
                    medsList.forEach { medMap ->
                        medsToInsert.add(
                            Medication(
                                patientId = medMap["medPetientID"].toString(), medicationName = medMap["medicationName"].toString(),
                                dosage = medMap["dosage"].toString(), frequency = medMap["frequency"].toString(),
                                scheduledTime = medMap["scheduledTime"].toString(), medicationType = medMap["medicationType"].toString(),
                                notes = medMap["notes"].toString(), isTaken = medMap["isTaken"] as? Boolean ?: false
                            )
                        )
                    }
                } catch (e: Exception) { Log.e("Seed", "Med Parse Error") }
            }
            database.medicationDao().insertMedications(medsToInsert)

            // 3. MIGRATE SYMPTOMS
            val symptomsToInsert = mutableListOf<Symptom>()
            context.assets.open("symptoms.csv").bufferedReader().useLines { lines ->
                lines.drop(1).forEach { line ->
                    val tokens = line.split(",")
                    if (tokens.size >= 5) {
                        symptomsToInsert.add(
                            Symptom(
                                patientId = tokens[0].trim(), category = tokens[1].trim(),
                                severity = tokens[2].trim(), notes = tokens[3].trim(), dateTime = tokens[4].trim()
                            )
                        )
                    }
                }
            }
            database.symptomDao().insertSymptoms(symptomsToInsert)

            // 4. Mark as seeded
            prefs.edit().putBoolean("is_db_seeded", true).apply()
            Log.d("Database", "Seed completed successfully")

        } catch (e: Exception) {
            Log.e("Database", "Error seeding database: ${e.message}")
        }
    }
}
