package com.adobe.marketing.nimbus.datamodels

data class PersonalizedOffers (
    val offerDecisioning: List<PersonalizedOffer> = emptyList(),
    val target: List<PersonalizedOffer> = emptyList()
)
