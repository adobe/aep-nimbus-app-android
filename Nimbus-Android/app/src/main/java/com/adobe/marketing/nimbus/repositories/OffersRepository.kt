package com.adobe.marketing.nimbus.repositories

import com.adobe.marketing.nimbus.datamodels.Offer
import com.adobe.marketing.nimbus.datamodels.OfferSurface
import com.adobe.marketing.nimbus.services.MessagingService
import javax.inject.Inject

/**
 * Thin pass-through to [MessagingService], powered by the AJO Messaging extension. Backs
 * [OffersViewModel]'s shared, per-surface offer state for Home, Inbox, Shop's CBE banner, and
 * Cart's triggered banner, plus their display/interact/dismiss engagement tracking.
 */
class OffersRepository @Inject constructor(
    private val messagingService: MessagingService
) {
    /** Fetches and parses content cards for one surface. */
    suspend fun fetchOffers(surface: OfferSurface): List<Offer>? =
        messagingService.fetchOffers(surface)

    /** Reports a card actually scrolled into view. */
    fun trackDisplay(cardId: String) = messagingService.trackDisplay(cardId)
    /** Reports a tap/interaction on a card. */
    fun trackInteract(cardId: String) = messagingService.trackInteract(cardId)
    /** Reports a card explicitly dismissed by the user. */
    fun trackDismiss(cardId: String) = messagingService.trackDismiss(cardId)
}