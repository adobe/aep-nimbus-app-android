package com.adobe.marketing.nimbus.services


interface NotificationService {
    fun isPushEnabled(): Boolean
    fun notificationEnableAction(): NotificationEnableAction

    /** The current FCM registration token for this device, or null if unavailable. */
    suspend fun pushToken(): String?
}

enum class NotificationEnableAction { REQUEST_PERMISSION, OPEN_SETTINGS }