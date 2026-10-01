package com.adobe.marketing.nimbus.repositories

import com.adobe.marketing.nimbus.data.ConsentPreferences
import com.adobe.marketing.nimbus.datamodels.ConsentGateState
import com.adobe.marketing.nimbus.datamodels.ConsentState
import com.adobe.marketing.nimbus.services.ConsentService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Powered by the AEP Edge Consent extension via [ConsentService], combined with the locally
 * persisted choice in [ConsentPreferences]. This is the single source of truth behind the
 * Consent gate and Profile's toggle: `hasChosenConsent` is what lets the gate skip itself on
 * return visits, while `consentState` drives both the gate's initial choice and whatever the
 * Profile toggle currently reflects, so the two surfaces never disagree.
 */
class ConsentRepository @Inject constructor(
    private val consentPreferences: ConsentPreferences,
    private val consentService: ConsentService
){
    /** Combines the persisted choice and value into one gate-decision state. */
    val consentGateState: Flow<ConsentGateState> = combine(
        consentPreferences.consentState,
        consentPreferences.hasChosenConsent
    ) { consent, hasChosen ->
        ConsentGateState(
            consent = consent,
            hasChosenConsent = hasChosen,
            isLoading = false
        )
    }

    /** Pushes a consent choice to the SDK and persists it locally. */
    suspend fun chooseConsent(state: ConsentState) {
        consentService.update(state)
        consentPreferences.setConsent(state)
    }
}
