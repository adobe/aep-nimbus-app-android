package com.adobe.marketing.nimbus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adobe.marketing.mobile.Assurance
import com.adobe.marketing.nimbus.datamodels.ConsentState
import com.adobe.marketing.nimbus.datamodels.ProfileUiState
import com.adobe.marketing.nimbus.repositories.AssuranceRepository
import com.adobe.marketing.nimbus.repositories.ConsentRepository
import com.adobe.marketing.nimbus.repositories.LoginRepository
import com.adobe.marketing.nimbus.repositories.NotificationRepository
import com.adobe.marketing.nimbus.repositories.PersonalizationRepository
import com.adobe.marketing.nimbus.services.NotificationEnableAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val consentRepository: ConsentRepository,
    private val loginRepository: LoginRepository,
    private val notificationRepository: NotificationRepository,
    private val assuranceRepository: AssuranceRepository,
    private val personalizationRepository: PersonalizationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private val decisionScopeInput = MutableSharedFlow<String>(extraBufferCapacity = 1)
    private val targetActivityInput = MutableSharedFlow<String>(extraBufferCapacity = 1)

    init {
        viewModelScope.launch {
            val ecid = loginRepository.experienceCloudId()
            _uiState.update { it.copy(ecid = ecid) }
        }
        viewModelScope.launch {
            loginRepository.loginGateState.collect {  gateState ->
                _uiState.update { it.copy(signedInUser = gateState.signedInUser) }
             }
        }
        viewModelScope.launch {
            consentRepository.consentGateState.collect { gateState ->
                _uiState.update { it.copy(consentState = gateState.consent) }
            }
        }
        refreshPushState()
        viewModelScope.launch {
            assuranceRepository.sessionUrl.collect { url ->
                _uiState.update { it.copy(assuranceSessionUrl = url) }
            }
        }
        viewModelScope.launch {
            personalizationRepository.offers.collect { offers ->
                _uiState.update { it.copy(personalizedOffers = offers) }
            }
        }
        viewModelScope.launch {
            personalizationRepository.scopeConfig.collect { config ->
                _uiState.update {
                    it.copy(
                        decisionScopeName = config.decisionScopeName,
                        targetActivityName = config.targetActivityName
                    )
                }
            }
        }

        viewModelScope.launch {
            decisionScopeInput.debounce(300).collect { name ->
                personalizationRepository.setDecisionScope(name)
            }
        }
        viewModelScope.launch {
            targetActivityInput.debounce(300).collect { name ->
                personalizationRepository.setTargetActivity(name)
            }
        }
    }

    fun connectAssurance(url: String) {
        if (url.isBlank()) return
        Assurance.startSession(url)
        assuranceRepository.recordSessionStarted(url)
    }

    fun refreshPushState() {
        _uiState.update { it.copy(pushEnabled = notificationRepository.isPushEnabled()) }
        viewModelScope.launch {
            val token = notificationRepository.pushToken()
            _uiState.update { it.copy(pushToken = token) }
        }
    }

    fun setConsent(state: ConsentState) {
        viewModelScope.launch { consentRepository.chooseConsent(state) }
    }

    fun logout() {
        viewModelScope.launch { loginRepository.logout() }
    }

    fun login(username: String) {
        viewModelScope.launch {  loginRepository.login(username) }
    }

    fun notificationEnableAction(): NotificationEnableAction =
        notificationRepository.notificationEnableAction()

    fun setDecisionScopeName(name: String) {
        _uiState.update { it.copy(decisionScopeName = name) }
        decisionScopeInput.tryEmit(name)
    }

    fun setTargetActivityName(name: String) {
        _uiState.update { it.copy(targetActivityName = name) }
        targetActivityInput.tryEmit(name)
    }

    fun fetchPersonalizedOffers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingOffers = true) }
            val state = _uiState.value
            personalizationRepository.refreshOffers(state.decisionScopeName, state.targetActivityName)
            _uiState.update { it.copy(isFetchingOffers = false) }
        }
    }

    fun onOfferDisplayed(offerId: String) = personalizationRepository.trackOfferDisplayed(offerId)

    fun onOfferTapped(offerId: String) = personalizationRepository.trackOfferTapped(offerId)
}