package com.adobe.marketing.nimbus.datamodels

import com.adobe.marketing.nimbus.data.MockProfile

data class ProfileUiState (
    val ecid: String? = null,
    val signedInUser: String? = null,
    val consentState: ConsentState = ConsentState.PENDING,
    val pushEnabled: Boolean = false,
    val pushToken: String? = null,
    val assuranceSessionUrl: String? = null,

    val profileAttributes: ProfileAttributes = MockProfile.attributes,
    val decisionScopeName: String = "",
    val targetActivityName: String = "",
    val personalizedOffers: PersonalizedOffers = PersonalizedOffers(),
    val isFetchingOffers: Boolean = false
)