package com.adobe.marketing.nimbus.services

import com.adobe.marketing.nimbus.datamodels.PersonalizedOffer

/**
 * Seam over the AEP Optimize extension, so `DecisionScope`/`Proposition`/`Offer` SDK types never
 * leak past [AepPersonalizationService]. Enables Profile's Personalized Offers dev lab and
 * Home's Recommended for You section to fetch AJO Offer Decisioning and Adobe Target content
 * through one plain-Kotlin, decision-scope-keyed API.
 */
interface PersonalizationService {
    /** Fetches personalized offers for the given decision scopes, keyed back by scope name. */
    suspend fun fetchOffers(scopeNames: List<String>): Map<String, List<PersonalizedOffer>>
    /** Reports a personalized offer actually scrolled into view. */
    fun trackDisplayed(offerId: String)
    /** Reports a tap on a personalized offer. */
    fun trackTapped(offerId: String)
}