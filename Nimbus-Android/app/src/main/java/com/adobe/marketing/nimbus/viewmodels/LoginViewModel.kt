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

/**
 * Powered by the AEP Edge Identity extension via [LoginRepository]. This ViewModel drives the
 * Login gate shown once on first launch (after Consent): logging in sets an authenticated
 * Email identity, choosing "Continue as Guest" instead just marks the gate passed. Either
 * choice durably skips this gate on every future launch, including after a later logout.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginRepository: LoginRepository
): ViewModel() {

    /** Mirrors the repository's login gate state as a StateFlow for the gate screen. */
    val uiState: StateFlow<LoginGateState> = loginRepository.loginGateState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LoginGateState())

    // Reads any already-logged-in identity once, so the gate reflects it immediately.
    init {
        viewModelScope.launch { loginRepository.initialize() }
    }

    /** Links this device to a username and durably marks the gate as passed. */
    fun login(username: String) {
        viewModelScope.launch { loginRepository.login(username) }
    }

    /** Marks the gate as passed without logging in, for anonymous browsing. */
    fun continueAsGuest() {
        viewModelScope.launch { loginRepository.continueAsGuest() }
    }
}