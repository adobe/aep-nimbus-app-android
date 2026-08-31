package com.adobe.marketing.nimbus.services

import com.adobe.marketing.nimbus.datamodels.ContentCard
import com.adobe.marketing.nimbus.datamodels.ContentSurface

interface MessagingService {
    suspend fun fetchContentCards(surface: ContentSurface): List<ContentCard>?
    fun trackDisplay(cardId: String)
    fun trackInteract(cardId: String)
    fun trackDismiss(cardId: String)
}