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

class LoginViewModel(private val patientRepo: PatientRepository) : ViewModel() {

    // --- Login Form State ---
    var patientId by mutableStateOf("")
    var password by mutableStateOf("")

    private val _loginResult = MutableSharedFlow<LoginResult>()
    val loginResult = _loginResult.asSharedFlow()

    /**
     * Authenticates the user against the Room database using PatientID and Password.
     */
    fun login() {
        viewModelScope.launch {
            if (patientId.isBlank() || password.isBlank()) {
                _loginResult.emit(LoginResult.Error("Please enter both PatientID and password"))
                return@launch
            }

            val patient = patientRepo.getPatientById(patientId)
            if (patient == null) {
                _loginResult.emit(LoginResult.Error("Account not found. If you are from the CSV, please claim your account first."))
            } else if (patient.password.isEmpty()) {
                _loginResult.emit(LoginResult.Error("This account has not been claimed yet. Please use the Claim Account option."))
            } else if (patient.password != password) {
                _loginResult.emit(LoginResult.Error("Incorrect password"))
            } else {
                _loginResult.emit(LoginResult.Success(patient.patientId))
            }
        }
    }

    sealed class LoginResult {
        data class Success(val patientId: String) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }

    class LoginViewModelFactory(private val patientRepo: PatientRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return LoginViewModel(patientRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
