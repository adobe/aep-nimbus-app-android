package com.adobe.marketing.nimbus.datamodels

data class ProfileUiState (
    val ecid: String? = null,
    val identityMap: List<IdentityMapEntry> = emptyList(),
    val signedInUser: String? = null,
    val consentState: ConsentState = ConsentState.PENDING,
    val pushEnabled: Boolean = false,
    val pushToken: String? = null,
    val assuranceSessionUrl: String? = null
)