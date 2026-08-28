package com.adobe.marketing.nimbus.repositories

import com.adobe.marketing.nimbus.data.ConsentPreferences
import com.adobe.marketing.nimbus.datamodels.ConsentGateState
import com.adobe.marketing.nimbus.datamodels.ConsentState
import com.adobe.marketing.nimbus.services.ConsentService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class ConsentRepository @Inject constructor(
    private val consentPreferences: ConsentPreferences,
    private val consentService: ConsentService
){
    val consentGateState: Flow<ConsentGateState> = combine(
        consentPreferences.consentState,
        consentPreferences.hasChosenConsent
    ) { consent, hasChosen ->
        ConsentGateState(consent, hasChosen)
    }

    suspend fun chooseConsent(state: ConsentState) {
        consentService.update(state)
        consentPreferences.setConsent(state)
    }
}