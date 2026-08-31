package com.adobe.marketing.nimbus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adobe.marketing.nimbus.datamodels.LoginGateState
import com.adobe.marketing.nimbus.repositories.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(LoginGateState())
    val uiState: StateFlow<LoginGateState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val email = loginRepository.signedInEmail()
            _uiState.value = _uiState.value.copy(
                isChecking = false,
                signedInUser = email,
                hasSeenLoginPrompt = email != null
            )
        }
    }

    fun login(username: String) {
        loginRepository.login(username)
        _uiState.value = _uiState.value.copy(
            signedInUser = username,
            hasSeenLoginPrompt = true
        )
    }

    fun continueAsGuest() {
        _uiState.value = _uiState.value.copy(hasSeenLoginPrompt = true)
    }
}