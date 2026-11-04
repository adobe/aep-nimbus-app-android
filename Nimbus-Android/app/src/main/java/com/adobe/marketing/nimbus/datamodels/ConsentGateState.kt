package com.adobe.marketing.nimbus.datamodels

data class ConsentGateState (
    val consent: ConsentState = ConsentState.PENDING,
    val hasChosenConsent: Boolean = false,
    val isLoading: Boolean = true
)
