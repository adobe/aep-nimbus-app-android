package com.adobe.marketing.nimbus.repositories

import com.adobe.marketing.nimbus.services.NotificationEnableAction
import com.adobe.marketing.nimbus.services.NotificationService
import javax.inject.Inject

/**
 * Thin pass-through to [NotificationService], which is powered by Android's notification
 * permission APIs and the Firebase Cloud Messaging token. Exists mainly for layering
 * consistency (ViewModel to Repository to Service has no exceptions in this app) rather than
 * any combining logic, since Profile's push section reads this state directly, unmodified.
 */
class NotificationRepository @Inject constructor(
    private val notificationService: NotificationService
) {
    /** Whether the user currently has notifications enabled for this app. */
    fun isPushEnabled(): Boolean = notificationService.isPushEnabled()

    /** Which UI flow to trigger to enable push. */
    fun notificationEnableAction(): NotificationEnableAction = notificationService.notificationEnableAction()

    /** The current FCM registration token for this device. */
    suspend fun pushToken(): String? = notificationService.pushToken()
}