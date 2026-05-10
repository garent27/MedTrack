package com.garent.s35123656.medtrack.data


/**
 * Data class for legacy medication storage compatibility.
 */
data class MedicationData(
    val medPetientID: String? = "",
    val medicationName: String? = "Unknown",
    val dosage: String? = "",
    val frequency: String? = "",
    val scheduledTime: String? = "",
    val medicationType: String? = "",
    val notes: String? = "",
    var isTaken: Boolean = false
)