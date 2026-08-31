package com.adobe.marketing.nimbus.services


interface NotificationService {
    fun isPushEnabled(): Boolean
    fun notificationEnableAction(): NotificationEnableAction
}

enum class NotificationEnableAction { REQUEST_PERMISSION, OPEN_SETTINGS }