package com.adobe.marketing.nimbus.repositories

import com.adobe.marketing.nimbus.datamodels.ContentCard
import com.adobe.marketing.nimbus.datamodels.ContentSurface
import com.adobe.marketing.nimbus.services.MessagingService
import javax.inject.Inject

class ContentCardRepository @Inject constructor(
    private val messagingService: MessagingService
) {
    suspend fun fetchContentCards(surface: ContentSurface): List<ContentCard>? =
        messagingService.fetchContentCards(surface)

    fun trackDisplay(cardId: String) = messagingService.trackDisplay(cardId)
    fun trackInteract(cardId: String) = messagingService.trackInteract(cardId)
    fun trackDismiss(cardId: String) = messagingService.trackDismiss(cardId)
}