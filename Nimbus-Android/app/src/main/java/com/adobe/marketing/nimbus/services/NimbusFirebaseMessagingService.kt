package com.adobe.marketing.nimbus.services

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.nimbus.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.adobe.marketing.mobile.messaging.MessagingService as AepMessagingService

/**
 * Custom Firebase Cloud Messaging entry point for Nimbus.
 *
 * Registered in the manifest as the app's FCM service, replacing the AEP SDK's own
 * [AepMessagingService]. Every push is first handed to the AEP Messaging extension via
 * [AepMessagingService.handleRemoteMessage], which runs the SDK's internal
 * `isAJONotification` check:
 *
 *  - If the payload originated from Adobe Journey Optimizer, the SDK builds, displays,
 *    and tracks the notification and returns `true`.
 *  - If it returns `false` — a plain FCM message sent outside AJO, e.g. a Firebase
 *    console test — Nimbus renders a basic notification itself so the message is still
 *    received and visible.
 *
 * This mirrors the pattern in the AEP Messaging test app's `NotificationService`, but
 * delegates the AJO detection/display to the SDK rather than re-implementing it.
 */
class NimbusFirebaseMessagingService : FirebaseMessagingService() {

    /** Forward the refreshed FCM token to the SDK so AJO can target this device. */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // === AJO interaction point ===
        // Hand the FCM registration token to the AEP SDK. The Messaging extension
        // syncs it to the Edge Network so Adobe Journey Optimizer can target push
        // to this device. Without this call, AJO campaigns cannot reach the app.
        Log.d(TAG, "onNewToken: forwarding FCM token to AEP SDK (MobileCore.setPushIdentifier). token=$token")
        MobileCore.setPushIdentifier(token)
    }

    /** Routes an incoming push to the AEP SDK first, falling back to a basic notification. */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(
            TAG,
            "onMessageReceived: messageId=${message.messageId}, from=${message.from}, " +
                "dataKeys=${message.data.keys}, hasNotificationBlock=${message.notification != null}"
        )

        // === AJO interaction point ===
        // Delegate to the AEP Messaging extension first. handleRemoteMessage runs the
        // SDK's internal isAJONotification check: if the payload originated from Adobe
        // Journey Optimizer, the SDK builds, displays, and tracks the notification and
        // returns true. This is the path exercised by real AJO campaigns.
        val handledByAjo = AepMessagingService.handleRemoteMessage(this, message)
        if (handledByAjo) {
            Log.d(TAG, "onMessageReceived: message identified as AJO — displayed & tracked by the AEP SDK.")
            return
        }

        // === Application custom-handling point ===
        // Not an AJO notification (isAJONotification returned false) — e.g. a plain FCM
        // message sent from the Firebase console or a non-Adobe backend. The SDK ignores
        // these, so the app renders its own notification here. Replace this with whatever
        // custom handling your app needs (routing, data sync, silent handling, etc.).
        Log.d(TAG, "onMessageReceived: non-AJO message — handling with app's custom notification.")
        showBasicNotification(message)
    }

    /** Builds and posts a plain notification for a non-AJO push, tapping into MainActivity. */
    private fun showBasicNotification(message: RemoteMessage) {
        // POST_NOTIFICATIONS is a runtime permission on Android 13+. If it has not been
        // granted, notify() would be a silent no-op, so skip the work.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            Log.w(TAG, "showBasicNotification: POST_NOTIFICATIONS not granted — skipping display.")
            return
        }

        ensureChannelExists()

        val title = message.notification?.title ?: message.data["title"] ?: "Nimbus"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: message.data["message"]
            ?: ""

        val contentIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentIntent)
            .build()

        val notificationId = message.messageId?.hashCode() ?: DEFAULT_NOTIFICATION_ID
        NotificationManagerCompat.from(this).notify(notificationId, notification)
        Log.d(TAG, "showBasicNotification: posted app notification id=$notificationId, title=\"$title\".")
    }

    /** Creates the app's own (non-AJO) notification channel on API 26+, once. */
    private fun ensureChannelExists() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply { description = "General (non-AJO) push notifications" }
            )
        }
    }

    private companion object {
        const val TAG = "NimbusFCM"
        const val CHANNEL_ID = "nimbus_push"
        const val CHANNEL_NAME = "Nimbus Push"
        const val DEFAULT_NOTIFICATION_ID = 0x4E49 // "NI"
    }
}
