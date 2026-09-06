package com.adobe.marketing.nimbus.services

import com.adobe.marketing.nimbus.datamodels.PersonalizedOffer

interface PersonalizationService {
    suspend fun fetchOffers(scopeNames: List<String>): Map<String, List<PersonalizedOffer>>
    fun trackDisplayed(offerId: String)
    fun trackTapped(offerId: String)
}