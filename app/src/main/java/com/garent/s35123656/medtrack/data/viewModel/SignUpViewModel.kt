package com.garent.s35123656.medtrack.data.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.garent.s35123656.medtrack.data.entity.Patient
import com.garent.s35123656.medtrack.data.repository.PatientRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class SignUpViewModel(private val patientRepo: PatientRepository) : ViewModel() {

    // --- Sign Up Form State (Persists across rotation) ---
    var fullName by mutableStateOf("")
    var phone by mutableStateOf("")
    var password by mutableStateOf("")
    var confirmPassword by mutableStateOf("")

    private val _signUpResult = MutableSharedFlow<SignUpResult>()
    val signUpResult = _signUpResult.asSharedFlow()

    fun signUp() {
        viewModelScope.launch {
            // Check if phone exists
            val existingUser = patientRepo.getPatientByPhone(phone)
            if (existingUser != null) {
                _signUpResult.emit(SignUpResult.Error("This phone number is already registered"))
                return@launch
            }

            // Generate new ID
            val lastId = patientRepo.getLastPatientId()
            val nextNum = (lastId?.removePrefix("P")?.toIntOrNull() ?: 1000) + 1
            val newPatientId = "P$nextNum"

            // Insert into DB
            val newPatient = Patient(
                patientId = newPatientId,
                phoneNumber = phone,
                name = fullName,
                password = password
            )
            patientRepo.insertPatient(newPatient)
            _signUpResult.emit(SignUpResult.Success)
        }
    }

    sealed class SignUpResult {
        object Success : SignUpResult()
        data class Error(val message: String) : SignUpResult()
    }

    class SignUpViewModelFactory(private val patientRepo: PatientRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SignUpViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SignUpViewModel(patientRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}