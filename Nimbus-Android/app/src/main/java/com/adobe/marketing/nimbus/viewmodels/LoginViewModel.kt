package com.adobe.marketing.nimbus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adobe.marketing.nimbus.datamodels.LoginGateState
import com.adobe.marketing.nimbus.repositories.LoginRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
): ViewModel() {

    val uiState: StateFlow<LoginGateState> = loginRepository.loginGateState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LoginGateState())

    init {
        viewModelScope.launch { loginRepository.initialize() }
    }

    fun login(username: String) {
        viewModelScope.launch { loginRepository.login(username) }
    }

    fun continueAsGuest() {
        viewModelScope.launch { loginRepository.continueAsGuest() }
    }
}