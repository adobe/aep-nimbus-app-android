package com.adobe.marketing.nimbus.services

import com.adobe.marketing.mobile.AdobeCallbackWithError
import com.adobe.marketing.mobile.AdobeError
import com.adobe.marketing.mobile.Messaging
import com.adobe.marketing.mobile.MessagingEdgeEventType
import com.adobe.marketing.mobile.messaging.Proposition
import com.adobe.marketing.mobile.messaging.PropositionItem
import com.adobe.marketing.mobile.messaging.Surface
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
        val rawContent = itemData["content"]
        val content: Map<*, *> = when (rawContent) {
            is Map<*, *> -> (rawContent["content"] as? Map<*, *>)
                ?: (rawContent["data"] as? Map<*, *>)
                ?: rawContent
            else -> itemData
        }

        val title = extractText(content["title"])
            .ifBlank { extractText(content["headline"]) }
            .ifBlank { extractText(content["header"]) }
            .ifBlank { extractText(itemData["title"]) }

        val body = extractText(content["body"])
            .ifBlank { extractText(content["description"]) }
            .ifBlank { extractText(content["text"]) }
            .ifBlank { extractText(itemData["body"]) }

        val imageUrl = extractUrl(content["image"])
            ?: extractUrl(content["imageUrl"])
            ?: extractUrl(content["img"])
            ?: extractUrl(content["media"])
            ?: extractUrl(content["asset"])
            ?: extractUrl(content["banner"])
            ?: extractUrl(content["src"])
            ?: extractUrl(content["url"])
            ?: extractUrl(itemData["image"])
            ?: extractUrl(itemData["imageUrl"])
            ?: extractUrl(itemData["media"])

        val actionUrl = extractUrl(content["actionUrl"])
            ?: extractUrl(content["action"])
            ?: extractUrl(content["cta"])
            ?: extractUrl(content["link"])
            ?: extractUrl(itemData["actionUrl"])

        val dismissStyle = (content["dismissBtn"] as? Map<*, *>)?.get("style") as? String
        val dismissible = dismissStyle != null && dismissStyle != "none"

        if (title.isBlank() && body.isBlank() && imageUrl.isNullOrBlank()) {
            return null
        }

        retainedItems[itemId] = this
        return Offer(
            id = itemId,
            title = title,
            body = body,
            imageUrl = imageUrl,
            actionUrl = actionUrl,
            dismissible = dismissible
        )
    }

    private fun extractText(obj: Any?): String {
        return when (obj) {
            is String -> obj
            is Map<*, *> -> (obj["content"] ?: obj["text"] ?: obj["value"]) as? String ?: ""
            else -> ""
        }
    }

    private fun extractUrl(obj: Any?): String? {
        return when (obj) {
            is String -> obj.takeIf { it.isNotBlank() && (it.startsWith("http://") || it.startsWith("https://") || it.startsWith("data:")) }
            is Map<*, *> -> {
                val candidate = obj["url"] ?: obj["src"] ?: obj["uri"] ?: obj["content"] ?: obj["href"] ?: obj["path"]
                when (candidate) {
                    is String -> candidate.takeIf { it.isNotBlank() && (it.startsWith("http://") || it.startsWith("https://") || it.startsWith("data:")) }
                    is Map<*, *> -> extractUrl(candidate)
                    else -> null
                }
            }
            else -> null
        }
    }
}
