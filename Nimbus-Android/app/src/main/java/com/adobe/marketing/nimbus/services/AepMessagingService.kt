package com.adobe.marketing.nimbus.services

import com.adobe.marketing.mobile.AdobeCallbackWithError
import com.adobe.marketing.mobile.AdobeError
import com.adobe.marketing.mobile.Messaging
import com.adobe.marketing.mobile.MessagingEdgeEventType
import com.adobe.marketing.mobile.messaging.Proposition
import com.adobe.marketing.mobile.messaging.PropositionItem
import com.adobe.marketing.mobile.messaging.Surface
import com.adobe.marketing.nimbus.datamodels.ContentCard
import com.adobe.marketing.nimbus.datamodels.ContentSurface
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject

class AepMessagingService @Inject constructor(): MessagingService {

    private val retainedItems = ConcurrentHashMap<String, PropositionItem>()
    private val fetchMutexes = mutableMapOf<ContentSurface, Mutex>()

    override suspend fun fetchContentCards(surface: ContentSurface): List<ContentCard>? =
        fetchMutexes.getOrPut(surface) { Mutex() }.withLock {
            val sdkSurface = Surface(surface.path)
            suspendCancellableCoroutine { continuation ->
                Messaging.updatePropositionsForSurfaces(listOf(sdkSurface)) { _ ->
                    Messaging.getPropositionsForSurfaces(
                        listOf(sdkSurface),
                        object : AdobeCallbackWithError<Map<Surface, List<Proposition>>> {
                            override fun call(propositionsMap: Map<Surface, List<Proposition>>?) {
                                val propositions = propositionsMap?.get(sdkSurface).orEmpty()

                                val cards = propositions.flatMap { proposition ->
                                    proposition.items.mapNotNull { it.toContentCard() }
                                }
                                continuation.resume(cards)
                            }

                            override fun fail(error: AdobeError?) {
                                continuation.resume(null)
                            }
                        }
                    )
                }
            }
        }

    override fun trackDisplay(cardId: String) = track(cardId, MessagingEdgeEventType.DISPLAY)
    override fun trackInteract(cardId: String) = track(cardId, MessagingEdgeEventType.INTERACT)
    override fun trackDismiss(cardId: String) = track(cardId, MessagingEdgeEventType.DISMISS)

    private fun track(cardId: String, eventType: MessagingEdgeEventType) {
        retainedItems[cardId]?.track(eventType)
    }

    private fun PropositionItem.toContentCard(): ContentCard? {
        val content = itemData["content"] as? Map<*, *> ?: return null
        val title = (content["title"] as? Map<*, *>)?.get("content") as? String ?: return null
        val body = (content["body"] as? Map<*, *>)?.get("content") as? String ?: ""
        val imageUrl = (content["image"] as? Map<*, *>)?.get("url") as? String
        val actionUrl = content["actionUrl"] as? String

        retainedItems[itemId] = this
        return ContentCard(id = itemId, title = title, body = body, imageUrl = imageUrl, actionUrl = actionUrl)
    }
}