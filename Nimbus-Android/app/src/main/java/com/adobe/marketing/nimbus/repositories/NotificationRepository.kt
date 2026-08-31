package com.adobe.marketing.nimbus.repositories

import com.adobe.marketing.nimbus.services.NotificationEnableAction
import com.adobe.marketing.nimbus.services.NotificationService
import javax.inject.Inject

class NotificationRepository @Inject constructor(
    private val notificationService: NotificationService
) {
    fun isPushEnabled(): Boolean = notificationService.isPushEnabled()

    fun notificationEnableAction(): NotificationEnableAction = notificationService.notificationEnableAction()
}