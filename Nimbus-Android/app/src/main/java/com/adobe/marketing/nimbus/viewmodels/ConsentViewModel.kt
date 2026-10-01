package com.adobe.marketing.nimbus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adobe.marketing.nimbus.datamodels.ConsentGateState
import com.adobe.marketing.nimbus.datamodels.ConsentState
import com.adobe.marketing.nimbus.repositories.ConsentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Powered by the AEP Edge Consent extension via [ConsentRepository]. This ViewModel drives
 * the Consent gate screen shown once on first launch: the user's Allow/Do Not Allow choice
 * flows straight through to `Consent.update`, and `hasChosenConsent` being persisted locally
 * is what lets the gate stay skipped on every subsequent cold launch.
 */
@HiltViewModel
class ConsentViewModel @Inject constructor(
    private val consentRepository: ConsentRepository
): ViewModel() {

    /** Mirrors the repository's consent gate state as a StateFlow for the gate screen. */
    val uiState: StateFlow<ConsentGateState> = consentRepository.consentGateState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConsentGateState(consent = ConsentState.PENDING, hasChosenConsent = false, isLoading = true)
    )

    /** Records the user's Allow/Do Not Allow choice and pushes it to Edge Consent. */
    fun onConsentChosen(state: ConsentState) {
        viewModelScope.launch {
            consentRepository.chooseConsent(state)
        }
    }
}
