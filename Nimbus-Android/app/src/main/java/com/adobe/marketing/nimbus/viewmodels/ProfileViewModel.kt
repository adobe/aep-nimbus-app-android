package com.adobe.marketing.nimbus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adobe.marketing.nimbus.datamodels.ConsentState
import com.adobe.marketing.nimbus.datamodels.ProfileUiState
import com.adobe.marketing.nimbus.repositories.ConsentRepository
import com.adobe.marketing.nimbus.repositories.LoginRepository
import com.adobe.marketing.nimbus.repositories.NotificationRepository
import com.adobe.marketing.nimbus.services.NotificationEnableAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val consentRepository: ConsentRepository,
    private val loginRepository: LoginRepository,
    private val notificationRepository: NotificationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val ecid = loginRepository.experienceCloudId()
            val email = loginRepository.signedInEmail()
            _uiState.value = _uiState.value.copy(ecid = ecid, signedInUser = email)
        }
        viewModelScope.launch {
            consentRepository.consentGateState.collect { gateState ->
                _uiState.value = _uiState.value.copy(consentState = gateState.consent)
            }
        }
        refreshPushState()
    }

    fun refreshPushState() {
        _uiState.value = _uiState.value.copy(pushEnabled = notificationRepository.isPushEnabled())
    }

    fun setConsent(state: ConsentState) {
        viewModelScope.launch { consentRepository.chooseConsent(state) }
    }

    fun logout() {
        viewModelScope.launch {
            loginRepository.logout()
            _uiState.value = _uiState.value.copy(signedInUser = null)
        }
    }

    fun login(username: String) {
        loginRepository.login(username)
        _uiState.value = _uiState.value.copy(signedInUser = username)
    }

    fun notificationEnableAction(): NotificationEnableAction = notificationRepository.notificationEnableAction()
}