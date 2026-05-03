package com.garent.s35123656.medtrack.data.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.garent.s35123656.medtrack.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class ClaimAccountViewModel(private val patientRepo: PatientRepository) : ViewModel() {

    var patientId by mutableStateOf("")
    var phone by mutableStateOf("")
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    private val _claimResult = MutableSharedFlow<ClaimResult>()
    val claimResult = _claimResult.asSharedFlow()

    fun claimAccount() {
        viewModelScope.launch {
            if (patientId.isBlank() || phone.isBlank() || newPassword.isBlank()) {
                _claimResult.emit(ClaimResult.Error("Please fill in all fields"))
                return@launch
            }

            if (newPassword != confirmPassword) {
                _claimResult.emit(ClaimResult.Error("Passwords do not match"))
                return@launch
            }

            val patient = patientRepo.getPatientByIdAndPhone(patientId, phone)
            if (patient == null) {
                _claimResult.emit(ClaimResult.Error("Invalid PatientID or Phone Number. Please check your details."))
            } else if (patient.password.isNotEmpty()) {
                _claimResult.emit(ClaimResult.Error("This account has already been claimed. Please login instead."))
            } else {
                // Update password
                val updatedPatient = patient.copy(password = newPassword)
                patientRepo.updatePatient(updatedPatient)
                clearFields()
                _claimResult.emit(ClaimResult.Success)
            }
        }
    }

    fun clearFields() {
        patientId = ""
        phone = ""
        newPassword = ""
        confirmPassword = ""
    }

    sealed class ClaimResult {
        object Success : ClaimResult()
        data class Error(val message: String) : ClaimResult()
    }

    class ClaimAccountViewModelFactory(private val patientRepo: PatientRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ClaimAccountViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ClaimAccountViewModel(patientRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
