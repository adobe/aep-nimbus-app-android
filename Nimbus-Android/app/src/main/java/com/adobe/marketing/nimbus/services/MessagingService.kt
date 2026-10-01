package com.adobe.marketing.nimbus.services

import com.adobe.marketing.nimbus.datamodels.Offer
import com.adobe.marketing.nimbus.datamodels.OfferSurface

/**
 * Seam over the AJO Messaging extension's content-card APIs, so `Proposition`/`PropositionItem`
 * SDK types never leak past [AepMessagingService]. Enables Home, Inbox, Shop's CBE banner, and
 * Cart's triggered offer banner to fetch and track content cards through one plain-Kotlin API.
 */
interface MessagingService {
    /** Fetches and parses content cards for one surface. */
    suspend fun fetchOffers(surface: OfferSurface): List<Offer>?
    /** Reports a card actually scrolled into view. */
    fun trackDisplay(cardId: String)
    /** Reports a tap/interaction on a card. */
    fun trackInteract(cardId: String)
    /** Reports a card explicitly dismissed by the user. */
    fun trackDismiss(cardId: String)
}