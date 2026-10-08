package com.adobe.marketing.nimbus.services

const val FLASH_SALE_TOPIC = "nimbus-flash-sale"
const val FLASH_SALE_CHANNEL_ID = "nimbus_flash_sale"

interface BroadcastTopicService {
    suspend fun subscribe(topic: String): Boolean
    suspend fun unsubscribe(topic: String): Boolean
}