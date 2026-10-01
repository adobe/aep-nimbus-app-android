package com.adobe.marketing.nimbus.services

import android.content.Context
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject

/**
 * Powered by `NotificationManagerCompat` (permission state) and Firebase Cloud Messaging's
 * `FirebaseMessaging.getInstance().token` API. This is the capability behind Profile's
 * "Enable notifications" button (routing to the Android 13+ runtime permission dialog or
 * the app's notification settings page below it) and the copyable FCM token shown on the
 * same screen, used to send test pushes directly from the Firebase Console.
 */
class AndroidNotificationService @Inject constructor(
    @param:ApplicationContext private val context: Context
): NotificationService {
    /** Checks the OS-level notification permission state for this app. */
    override fun isPushEnabled(): Boolean =
        NotificationManagerCompat.from(context).areNotificationsEnabled()

    /** Picks the runtime-permission dialog on API 33+, or the settings page below that. */
    override fun notificationEnableAction(): NotificationEnableAction =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            NotificationEnableAction.REQUEST_PERMISSION
        } else {
            NotificationEnableAction.OPEN_SETTINGS
        }

    /** Fetches the current FCM registration token from Firebase. */
    override suspend fun pushToken(): String? =
        suspendCancellableCoroutine { continuation ->
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                continuation.resume(if (task.isSuccessful) task.result else null)
            }
        }
}