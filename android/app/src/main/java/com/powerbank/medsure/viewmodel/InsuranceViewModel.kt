package com.powerbank.medsure.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerbank.medsure.data.model.Claim
import com.powerbank.medsure.data.model.ClaimDraftResponse
import com.powerbank.medsure.data.repository.MedSureRepository
import com.powerbank.medsure.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class InsuranceUiState {
    object Loading : InsuranceUiState()
    data class Success(
        val claim: Claim,
        val draft: ClaimDraftResponse? = null,
        val approvalMessage: String? = null
    ) : InsuranceUiState()
    data class Error(val message: String) : InsuranceUiState()
}

class InsuranceViewModel(
    private val repository: MedSureRepository = MedSureRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<InsuranceUiState>(InsuranceUiState.Loading)
    val uiState: StateFlow<InsuranceUiState> = _uiState.asStateFlow()

    init {
        loadClaim()
    }

    fun loadClaim(caseId: String? = null) {
        val targetCaseId = caseId ?: SessionManager.activeCaseId.value
        viewModelScope.launch {
            _uiState.value = InsuranceUiState.Loading
            val result = repository.getClaim(targetCaseId)
            result.onSuccess { claim ->
                _uiState.value = InsuranceUiState.Success(claim = claim)
            }.onFailure { error ->
                _uiState.value = InsuranceUiState.Error(error.message ?: "Failed to load claim")
            }
        }
    }

    fun generateClaimDraft() {
        val targetCaseId = SessionManager.activeCaseId.value
        viewModelScope.launch {
            val currentState = _uiState.value
            val currentClaim = if (currentState is InsuranceUiState.Success) currentState.claim else null
            
            val result = repository.generateClaimDraft(targetCaseId)
            result.onSuccess { draft ->
                if (currentClaim != null) {
                    _uiState.value = InsuranceUiState.Success(claim = currentClaim, draft = draft)
                } else {
                    loadClaim()
                }
            }
        }
    }

    fun approveAndSendDraft() {
        val targetCaseId = SessionManager.activeCaseId.value
        viewModelScope.launch {
            val result = repository.approveClaim(targetCaseId)
            result.onSuccess {
                // Reload claim to reflect updated reimbursement status
                val claimResult = repository.getClaim(targetCaseId)
                if (claimResult.isSuccess) {
                    _uiState.value = InsuranceUiState.Success(
                        claim = claimResult.getOrThrow().copy(status = "REIMBURSEMENT"),
                        draft = null,
                        approvalMessage = "Claim approved & dispatched to insurer! Status updated to REIMBURSEMENT."
                    )
                }
            }
        }
    }
}
