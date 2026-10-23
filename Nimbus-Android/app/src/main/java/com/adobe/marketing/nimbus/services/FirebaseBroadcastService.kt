package com.adobe.marketing.nimbus.services

import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

class FirebaseBroadcastService @Inject constructor(): BroadcastTopicService {

    override suspend fun subscribe(topic: String): Boolean =
        suspendCancellableCoroutine{ continuation ->
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnCompleteListener { continuation.resume(it.isSuccessful) }
        }

    override suspend fun unsubscribe(topic: String): Boolean =
        suspendCancellableCoroutine{ continuation ->
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnCompleteListener { continuation.resume(it.isSuccessful) }
        }
}