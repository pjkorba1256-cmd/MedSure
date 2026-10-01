package com.powerbank.medsure.data.session

import android.content.Context
import com.powerbank.medsure.data.model.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object SessionManager {
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _token = MutableStateFlow<String?>(null)
    val token: StateFlow<String?> = _token.asStateFlow()

    private val _activeCaseId = MutableStateFlow("MS-001")
    val activeCaseId: StateFlow<String> = _activeCaseId.asStateFlow()

    var baseUrl: String = "http://10.0.2.2:8000/" // Android emulator default to host machine

    fun saveSession(user: User, authToken: String) {
        _currentUser.value = user
        _token.value = authToken
    }

    fun setActiveCaseId(caseId: String) {
        _activeCaseId.value = caseId
    }

    fun clearSession() {
        _currentUser.value = null
        _token.value = null
    }

    fun isLoggedIn(): Boolean = _token.value != null
}
