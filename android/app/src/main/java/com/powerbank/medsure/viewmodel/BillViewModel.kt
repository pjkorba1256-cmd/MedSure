package com.powerbank.medsure.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerbank.medsure.data.model.BillItem
import com.powerbank.medsure.data.model.Query
import com.powerbank.medsure.data.repository.MedSureRepository
import com.powerbank.medsure.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class BillUiState {
    object Loading : BillUiState()
    data class Success(
        val items: List<BillItem>,
        val queries: List<Query>,
        val totalAmount: Double,
        val flaggedCount: Int
    ) : BillUiState()
    data class Error(val message: String) : BillUiState()
}

class BillViewModel(
    private val repository: MedSureRepository = MedSureRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<BillUiState>(BillUiState.Loading)
    val uiState: StateFlow<BillUiState> = _uiState.asStateFlow()

    private val _querySentEvent = MutableStateFlow<String?>(null)
    val querySentEvent: StateFlow<String?> = _querySentEvent.asStateFlow()

    init {
        loadBillsAndQueries()
    }

    fun loadBillsAndQueries(caseId: String? = null) {
        val targetCaseId = caseId ?: SessionManager.activeCaseId.value
        viewModelScope.launch {
            _uiState.value = BillUiState.Loading
            val billsResult = repository.getBills(targetCaseId)
            val queriesResult = repository.getQueries(targetCaseId)

            val items = billsResult.getOrDefault(emptyList())
            val queries = queriesResult.getOrDefault(emptyList())

            val total = items.sumOf { it.amount }
            val flagged = items.count { it.flagged }

            _uiState.value = BillUiState.Success(
                items = items,
                queries = queries,
                totalAmount = total,
                flaggedCount = flagged
            )
        }
    }

    fun askTheHospital(billItemId: String?, message: String) {
        val targetCaseId = SessionManager.activeCaseId.value
        viewModelScope.launch {
            val result = repository.createQuery(targetCaseId, billItemId, message)
            result.onSuccess {
                _querySentEvent.value = "Query sent to Hospital Desk successfully!"
                loadBillsAndQueries()
            }.onFailure {
                _querySentEvent.value = "Failed to send query: ${it.message}"
            }
        }
    }

    fun clearQueryEvent() {
        _querySentEvent.value = null
    }
}
