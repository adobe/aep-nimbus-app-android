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

/**
 * Powered by five AEP capabilities at once, each behind its own repository: Edge Consent
 * (data-collection toggle), Edge Identity (ECID, login/logout, Identity Map), push/FCM
 * (notification permission + token display), Assurance (session connect), and Optimize
 * (the Personalized Offers dev lab, with its own decision-scope/Target-activity config).
 * Profile is deliberately the one screen that surfaces every extension's raw, inspectable
 * state, which is why it aggregates this many repositories instead of splitting further.
 */
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

    // Seeds every section's initial state and subscribes to each repository's reactive state,
    // plus debounces the two personalization text fields before persisting them.
    init {
        viewModelScope.launch {
            val ecid = loginRepository.experienceCloudId()
            _uiState.update { it.copy(ecid = ecid) }
        }
        viewModelScope.launch {
            loginRepository.loginGateState.collect {  gateState ->
                _uiState.update { it.copy(signedInUser = gateState.signedInUser) }
                refreshIdentityMap()
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

    /** Re-reads the full Identity Map from Edge Identity, e.g. after login/logout changes it. */
    private fun refreshIdentityMap() {
        viewModelScope.launch {
            val identities = loginRepository.identityMap()
            _uiState.update { it.copy(identityMap = identities) }
        }
    }

    /** Starts an Assurance session for the given URL and records it locally. */
    fun connectAssurance(url: String) {
        if (url.isBlank()) return
        Assurance.startSession(url)
        assuranceRepository.recordSessionStarted(url)
    }

    /** Re-checks OS notification permission state and refetches the current FCM token. */
    fun refreshPushState() {
        _uiState.update { it.copy(pushEnabled = notificationRepository.isPushEnabled()) }
        viewModelScope.launch {
            val token = notificationRepository.pushToken()
            _uiState.update { it.copy(pushToken = token) }
        }
    }

    /** Pushes a new consent choice from the Profile toggle. */
    fun setConsent(state: ConsentState) {
        viewModelScope.launch { consentRepository.chooseConsent(state) }
    }

    /** Logs the current user out, inline, without ever re-showing the login gate. */
    fun logout() {
        viewModelScope.launch { loginRepository.logout() }
    }

    /** Logs in as the given username, inline, without ever re-showing the login gate. */
    fun login(username: String) {
        viewModelScope.launch {  loginRepository.login(username) }
    }

    /** Which enable-notifications UI flow to trigger (permission dialog vs. settings page). */
    fun notificationEnableAction(): NotificationEnableAction =
        notificationRepository.notificationEnableAction()

    /** Updates the decision-scope field instantly and queues its debounced persistence. */
    fun setDecisionScopeName(name: String) {
        _uiState.update { it.copy(decisionScopeName = name) }
        decisionScopeInput.tryEmit(name)
    }

    /** Updates the Target-activity field instantly and queues its debounced persistence. */
    fun setTargetActivityName(name: String) {
        _uiState.update { it.copy(targetActivityName = name) }
        targetActivityInput.tryEmit(name)
    }

    /** Fetches personalized offers using exactly what's currently typed in the fields. */
    fun fetchPersonalizedOffers() {
        viewModelScope.launch {
            _uiState.update { it.copy(isFetchingOffers = true) }
            val state = _uiState.value
            personalizationRepository.refreshOffers(state.decisionScopeName, state.targetActivityName)
            _uiState.update { it.copy(isFetchingOffers = false) }
        }
    }

    /** Reports a personalized offer actually scrolled into view. */
    fun onOfferDisplayed(offerId: String) = personalizationRepository.trackOfferDisplayed(offerId)

    /** Reports a tap on a personalized offer. */
    fun onOfferTapped(offerId: String) = personalizationRepository.trackOfferTapped(offerId)
}