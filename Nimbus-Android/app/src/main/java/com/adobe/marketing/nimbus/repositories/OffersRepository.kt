package com.adobe.marketing.nimbus.repositories

import com.adobe.marketing.nimbus.datamodels.Offer
import com.adobe.marketing.nimbus.datamodels.OfferSurface
import com.adobe.marketing.nimbus.services.MessagingService
import javax.inject.Inject

class OffersRepository @Inject constructor(
    private val messagingService: MessagingService
) {
    suspend fun fetchOffers(surface: OfferSurface): List<Offer>? =
        messagingService.fetchOffers(surface)

    fun trackDisplay(cardId: String) = messagingService.trackDisplay(cardId)
    fun trackInteract(cardId: String) = messagingService.trackInteract(cardId)
    fun trackDismiss(cardId: String) = messagingService.trackDismiss(cardId)
}