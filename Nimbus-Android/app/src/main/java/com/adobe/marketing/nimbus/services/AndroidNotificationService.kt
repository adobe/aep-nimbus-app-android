package com.adobe.marketing.nimbus.services

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AndroidNotificationService @Inject constructor(
    @param:ApplicationContext private val context: Context
): NotificationService {
    override fun isPushEnabled(): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    override fun notificationEnableAction(): NotificationEnableAction =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationEnableAction.REQUEST_PERMISSION
        } else {
            NotificationEnableAction.OPEN_SETTINGS
        }
}