package com.powerbank.medsure.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerbank.medsure.data.model.CheckInRequest
import com.powerbank.medsure.data.model.CheckInResponse
import com.powerbank.medsure.data.repository.MedSureRepository
import com.powerbank.medsure.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class CheckInUiState {
    object Idle : CheckInUiState()
    object Submitting : CheckInUiState()
    data class Submitted(val response: CheckInResponse) : CheckInUiState()
    data class Error(val message: String) : CheckInUiState()
}

class CheckInViewModel(
    private val repository: MedSureRepository = MedSureRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckInUiState>(CheckInUiState.Idle)
    val uiState: StateFlow<CheckInUiState> = _uiState.asStateFlow()

    fun submitCheckIn(
        pain: Int,
        fever: Boolean,
        breathingDifficulty: Boolean,
        dizziness: Boolean,
        vomiting: Boolean,
        freeText: String?
    ) {
        val targetCaseId = SessionManager.activeCaseId.value
        viewModelScope.launch {
            _uiState.value = CheckInUiState.Submitting
            val request = CheckInRequest(
                pain = pain,
                fever = fever,
                breathingDifficulty = breathingDifficulty,
                dizziness = dizziness,
                vomiting = vomiting,
                freeText = freeText?.ifBlank { null }
            )
            val result = repository.submitCheckIn(targetCaseId, request)
            result.onSuccess { res ->
                _uiState.value = CheckInUiState.Submitted(res)
            }.onFailure { err ->
                _uiState.value = CheckInUiState.Error(err.message ?: "Submission failed")
            }
        }
    }

    fun resetState() {
        _uiState.value = CheckInUiState.Idle
    }
}
