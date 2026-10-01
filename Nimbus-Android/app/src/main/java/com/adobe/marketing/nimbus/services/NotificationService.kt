package com.adobe.marketing.nimbus.services


/**
 * Seam over Android's notification permission state plus the Firebase Cloud Messaging token
 * API. Backing implementation: [AndroidNotificationService]. Enables Profile's "Enable
 * notifications" flow and the copyable FCM token used for Firebase Console test sends,
 * separate from the actual AJO push handling in [NimbusFirebaseMessagingService].
 */
interface NotificationService {
    /** Whether the user currently has notifications enabled for this app. */
    fun isPushEnabled(): Boolean
    /** Which UI flow to trigger to enable push: a runtime permission or the settings page. */
    fun notificationEnableAction(): NotificationEnableAction

    /** The current FCM registration token for this device, or null if unavailable. */
    suspend fun pushToken(): String?
}

enum class NotificationEnableAction { REQUEST_PERMISSION, OPEN_SETTINGS }