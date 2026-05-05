package com.garent.s35123656.medtrack.data.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ClinicianLoginViewModel : ViewModel() {

    var accessKey by mutableStateOf("")
    
    private val _loginResult = MutableSharedFlow<Boolean>()
    val loginResult = _loginResult.asSharedFlow()

    private val predefinedKey = "dollar-entry-apples"

    suspend fun login() {
        if (accessKey == predefinedKey) {
            _loginResult.emit(true)
        } else {
            _loginResult.emit(false)
        }
    }
}
