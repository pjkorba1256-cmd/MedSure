package com.powerbank.medsure.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerbank.medsure.data.model.User
import com.powerbank.medsure.data.repository.MedSureRepository
import com.powerbank.medsure.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthUiState {
    object Idle : AuthUiState()
    object Loading : AuthUiState()
    data class Success(val user: User) : AuthUiState()
    data class Error(val message: String) : AuthUiState()
}

class AuthViewModel(
    private val repository: MedSureRepository = MedSureRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    val currentUser = SessionManager.currentUser

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Loading
            val result = repository.login(email.trim(), password)
            result.onSuccess { auth ->
                _uiState.value = AuthUiState.Success(auth.user)
            }.onFailure { error ->
                _uiState.value = AuthUiState.Error(error.message ?: "Authentication failed")
            }
        }
    }

    fun quickDemoLogin(role: String) {
        val email = when (role) {
            "HOSPITAL_DESK" -> "hospital@medsure.demo"
            "FAMILY" -> "family@medsure.demo"
            else -> "patient@medsure.demo"
        }
        login(email, "MedSure2026!")
    }

    fun logout() {
        SessionManager.clearSession()
        _uiState.value = AuthUiState.Idle
    }
}
