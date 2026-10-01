package com.powerbank.medsure.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerbank.medsure.data.model.DischargePack
import com.powerbank.medsure.data.repository.MedSureRepository
import com.powerbank.medsure.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class DischargeUiState {
    object Loading : DischargeUiState()
    data class Success(val pack: DischargePack) : DischargeUiState()
    data class Error(val message: String) : DischargeUiState()
}

class DischargeViewModel(
    private val repository: MedSureRepository = MedSureRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<DischargeUiState>(DischargeUiState.Loading)
    val uiState: StateFlow<DischargeUiState> = _uiState.asStateFlow()

    init {
        loadDischargePack()
    }

    fun loadDischargePack(caseId: String? = null) {
        val targetCaseId = caseId ?: SessionManager.activeCaseId.value
        viewModelScope.launch {
            _uiState.value = DischargeUiState.Loading
            val result = repository.getDischargePack(targetCaseId)
            result.onSuccess { pack ->
                _uiState.value = DischargeUiState.Success(pack)
            }.onFailure { error ->
                _uiState.value = DischargeUiState.Error(error.message ?: "Failed to load discharge pack")
            }
        }
    }

    fun triggerDischargeProcessing(caseId: String? = null, documentId: String? = null) {
        val targetCaseId = caseId ?: SessionManager.activeCaseId.value
        viewModelScope.launch {
            _uiState.value = DischargeUiState.Loading
            val result = repository.processDischarge(targetCaseId, documentId)
            result.onSuccess { pack ->
                _uiState.value = DischargeUiState.Success(pack)
            }.onFailure { error ->
                _uiState.value = DischargeUiState.Error(error.message ?: "Processing failed")
            }
        }
    }
}
