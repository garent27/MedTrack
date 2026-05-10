package com.garent.s35123656.medtrack.data.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.garent.s35123656.medtrack.data.entity.Medication
import com.garent.s35123656.medtrack.data.repository.MedicationRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class AddMedicationViewModel(
    private val medicationRepo: MedicationRepository
) : ViewModel() {

    // --- Form State ---
    var medName by mutableStateOf("")
    var dosage by mutableStateOf("")
    var scheduledTime by mutableStateOf("")
    var notes by mutableStateOf("")
    var selectedFreq by mutableStateOf("Once daily")
    var selectedType by mutableStateOf("Tablet")
    var showErrors by mutableStateOf(false)

    // Event Flow for navigation and Toasts
    private val _addMedResult = MutableSharedFlow<Boolean>()
    val addMedResult = _addMedResult.asSharedFlow()

    fun saveMedication(patientId: String) {
        viewModelScope.launch {
            val dosageRegex = Regex("""^\d+(\.\d+)?(mg|ml|g)$""")
            val isNameValid = medName.isNotBlank()
            val isDosageValid = dosageRegex.matches(dosage.trim())
            val isTimeValid = scheduledTime.isNotBlank()

            if (isNameValid && isDosageValid && isTimeValid) {
                val newMedication = Medication(
                    patientId = patientId,
                    medicationName = medName,
                    dosage = dosage,
                    frequency = selectedFreq,
                    scheduledTime = scheduledTime,
                    medicationType = selectedType,
                    notes = notes,
                    isTaken = false
                )
                medicationRepo.insertMedication(newMedication)
                clearForm()
                _addMedResult.emit(true) // Tell the UI it was successful!
            } else {
                showErrors = true
                _addMedResult.emit(false) // Tell the UI validation failed
            }
        }
    }

    fun clearForm() {
        medName = ""
        dosage = ""
        scheduledTime = ""
        notes = ""
        selectedFreq = "Once daily"
        selectedType = "Tablet"
        showErrors = false
    }

    class AddMedicationViewModelFactory(
        private val medicationRepo: MedicationRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(AddMedicationViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return AddMedicationViewModel(medicationRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}