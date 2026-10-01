package com.powerbank.medsure.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.powerbank.medsure.data.model.Alert
import com.powerbank.medsure.data.model.Query
import com.powerbank.medsure.data.repository.MedSureRepository
import com.powerbank.medsure.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

sealed class HospitalUiState {
    object Loading : HospitalUiState()
    data class Success(
        val alerts: List<Alert>,
        val queries: List<Query>,
        val redAlertCount: Int,
        val openQueryCount: Int
    ) : HospitalUiState()
    data class Error(val message: String) : HospitalUiState()
}

class HospitalViewModel(
    private val repository: MedSureRepository = MedSureRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HospitalUiState>(HospitalUiState.Loading)
    val uiState: StateFlow<HospitalUiState> = _uiState.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    init {
        loadDeskData()
    }

    fun loadDeskData() {
        viewModelScope.launch {
            _uiState.value = HospitalUiState.Loading
            val alertsRes = repository.getHospitalAlerts()
            val queriesRes = repository.getQueries(SessionManager.activeCaseId.value)

            val alerts = alertsRes.getOrDefault(emptyList())
            val queries = queriesRes.getOrDefault(emptyList())

            val redCount = alerts.count { it.severity == "RED" }
            val openQueries = queries.count { it.status == "OPEN" }

            _uiState.value = HospitalUiState.Success(
                alerts = alerts,
                queries = queries,
                redAlertCount = redCount,
                openQueryCount = openQueries
            )
        }
    }

    fun replyToQuery(queryId: String, replyText: String) {
        viewModelScope.launch {
            val result = repository.replyToQuery(queryId, replyText)
            result.onSuccess {
                _actionMessage.value = "Reply sent to family!"
                loadDeskData()
            }.onFailure {
                _actionMessage.value = "Failed to send reply: ${it.message}"
            }
        }
    }

    fun uploadDischargeFile(file: File) {
        viewModelScope.launch {
            _actionMessage.value = "Uploading and extracting discharge summary..."
            val result = repository.uploadDocument(SessionManager.activeCaseId.value, "DISCHARGE_SUMMARY", file)
            result.onSuccess {
                repository.processDischarge(SessionManager.activeCaseId.value, it.id)
                _actionMessage.value = "Discharge Summary uploaded & processed with OCR & AI!"
                loadDeskData()
            }.onFailure {
                _actionMessage.value = "Upload failed: ${it.message}"
            }
        }
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }
}
