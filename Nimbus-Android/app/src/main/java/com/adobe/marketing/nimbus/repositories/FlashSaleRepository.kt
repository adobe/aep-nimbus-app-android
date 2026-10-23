package com.adobe.marketing.nimbus.repositories

import com.adobe.marketing.nimbus.data.LiveUpdatePreferences
import com.adobe.marketing.nimbus.services.BroadcastTopicService
import com.adobe.marketing.nimbus.services.FLASH_SALE_TOPIC
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FlashSaleRepository @Inject constructor(
    private val topicService: BroadcastTopicService,
    private val preferences: LiveUpdatePreferences
) {
    val optedIn: Flow<Boolean> = preferences.flashSaleOptIn

    suspend fun setOptIn(enabled: Boolean): Boolean {
        val changed = if (enabled) {
            topicService.subscribe(FLASH_SALE_TOPIC)
        } else {
            topicService.unsubscribe(FLASH_SALE_TOPIC)
        }
        if (changed) preferences.setFlashSaleOptIn(enabled)
        return changed
    }
}