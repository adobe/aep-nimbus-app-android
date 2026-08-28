package com.adobe.marketing.nimbus.datamodels

data class ConsentGateState (
    val consent: ConsentState,
    val hasChosenConsent: Boolean
)