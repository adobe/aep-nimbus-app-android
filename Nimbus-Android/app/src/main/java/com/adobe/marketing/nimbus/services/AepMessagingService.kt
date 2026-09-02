package com.adobe.marketing.nimbus.services

import com.adobe.marketing.mobile.AdobeCallbackWithError
import com.adobe.marketing.mobile.AdobeError
import com.adobe.marketing.mobile.Messaging
import com.adobe.marketing.mobile.MessagingEdgeEventType
import com.adobe.marketing.mobile.messaging.Proposition
import com.adobe.marketing.mobile.messaging.PropositionItem
import com.adobe.marketing.mobile.messaging.SchemaType
import com.adobe.marketing.mobile.messaging.Surface
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.nimbus.datamodels.Offer
import com.adobe.marketing.nimbus.datamodels.OfferSurface
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class AepMessagingService @Inject constructor() : MessagingService {

    private val retainedItems = ConcurrentHashMap<String, PropositionItem>()
    private val fetchMutexes = mutableMapOf<OfferSurface, Mutex>()

    override suspend fun fetchOffers(surface: OfferSurface): List<Offer>? =
        fetchMutexes.getOrPut(surface) { Mutex() }.withLock {
            val sdkSurface = Surface(surface.path)
            suspendCancellableCoroutine { continuation ->
                Messaging.updatePropositionsForSurfaces(listOf(sdkSurface)) { success ->
                    Log.debug(
                        "Nimbus",
                        "AepMessagingService",
                        "updatePropositionsForSurfaces surface=${surface.path} success=$success"
                    )
                    if (success != true) {
                        continuation.resume(null)
                        return@updatePropositionsForSurfaces
                    }
                    Messaging.getPropositionsForSurfaces(
                        listOf(sdkSurface),
                        object : AdobeCallbackWithError<Map<Surface, List<Proposition>>> {
                            override fun call(propositionsMap: Map<Surface, List<Proposition>>?) {
                                val propositions = propositionsMap?.get(sdkSurface).orEmpty()

                                val cards = propositions.flatMap { proposition ->
                                    proposition.items.mapNotNull { it.toOffer() }
                                }
                                continuation.resume(cards)
                            }

                            override fun fail(error: AdobeError?) {
                                Log.debug(
                                    "Nimbus",
                                    "AepMessagingService",
                                    "getPropositionsForSurfaces surface=${surface.path} failed: $error"
                                )
                                continuation.resume(null)
                            }
                        })
                }
            }
        }

    override fun trackDisplay(cardId: String) = track(cardId, MessagingEdgeEventType.DISPLAY)
    override fun trackInteract(cardId: String) = track(cardId, MessagingEdgeEventType.INTERACT)
    override fun trackDismiss(cardId: String) = track(cardId, MessagingEdgeEventType.DISMISS)

    private fun track(cardId: String, eventType: MessagingEdgeEventType) {
        retainedItems[cardId]?.track(eventType)
    }

    private fun PropositionItem.toOffer(): Offer? {
        val content = when (schema) {
            SchemaType.CONTENT_CARD -> itemData["content"] as? Map<*, *>
            SchemaType.JSON_CONTENT -> (itemData["content"] as? Map<*, *>)?.get("content")
                    as? Map<*, *>
            else -> null
        } ?: return null

        val title = (content["title"] as? Map<*, *>)?.get("content") as? String ?: ""
        val body = (content["body"] as? Map<*, *>)?.get("content") as? String ?: ""
        val imageUrl = (content["image"] as? Map<*, *>)?.get("url") as? String
        val actionUrl = (content["actionUrl"] as? String)?.takeIf { it.isNotBlank() }
        val dismissStyle = (content["dismissBtn"] as? Map<*, *>)?.get("style") as? String
        val dismissible = dismissStyle != null && dismissStyle != "none"

        retainedItems[itemId] = this
        return Offer(id = itemId, title = title, body = body, imageUrl = imageUrl, actionUrl
        = actionUrl, dismissible = dismissible)
    }
}