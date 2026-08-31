package com.adobe.marketing.nimbus.datamodels

data class ProfileUiState (
    val ecid: String? = null,
    val signedInUser: String? = null,
    val consentState: ConsentState = ConsentState.PENDING,
    val pushEnabled: Boolean = false
)