package com.adobe.marketing.nimbus.services

import com.adobe.marketing.nimbus.datamodels.Offer
import com.adobe.marketing.nimbus.datamodels.OfferSurface

interface MessagingService {
    suspend fun fetchOffers(surface: OfferSurface): List<Offer>?
    fun trackDisplay(cardId: String)
    fun trackInteract(cardId: String)
    fun trackDismiss(cardId: String)
}