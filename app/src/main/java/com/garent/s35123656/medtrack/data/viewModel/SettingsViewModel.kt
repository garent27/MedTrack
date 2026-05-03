package com.garent.s35123656.medtrack.data.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.garent.s35123656.medtrack.data.entity.Patient
import com.garent.s35123656.medtrack.data.repository.PatientRepository
import kotlinx.coroutines.launch

class SettingsViewModel(private val patientRepo: PatientRepository) : ViewModel() {

    var currentUser by mutableStateOf<Patient?>(null)
        private set

    fun loadUserInfo(patientId: String) {
        viewModelScope.launch {
            currentUser = patientRepo.getPatientById(patientId)
        }
    }

    class SettingsViewModelFactory(private val patientRepo: PatientRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return SettingsViewModel(patientRepo) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
