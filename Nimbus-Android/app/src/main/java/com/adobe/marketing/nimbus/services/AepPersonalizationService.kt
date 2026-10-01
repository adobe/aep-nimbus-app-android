package com.adobe.marketing.nimbus.services

import androidx.core.text.HtmlCompat
import com.adobe.marketing.mobile.AdobeCallbackWithError
import com.adobe.marketing.mobile.AdobeError
import com.adobe.marketing.mobile.optimize.DecisionScope
import com.adobe.marketing.mobile.optimize.Offer
import com.adobe.marketing.mobile.optimize.OfferType
import com.adobe.marketing.mobile.optimize.Optimize
import com.adobe.marketing.mobile.optimize.Proposition
import com.adobe.marketing.nimbus.datamodels.PersonalizedOffer
import javax.inject.Inject
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onSubscription
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Singleton

/**
 * Powered by the AEP Optimize extension (`Optimize.updatePropositions`/`getPropositions`/
 * `onPropositionsUpdate`), which covers both AJO Offer Decisioning and Adobe Target through
 * the same decision-scope-keyed API. This is the capability behind Profile's "Personalized
 * Offers" dev lab and Home's "Recommended for You" section: whichever content the scope
 * resolves to server-side (an AJO decision or a Target activity) comes back identically here.
 */
@Singleton
class AepPersonalizationService @Inject constructor(): PersonalizationService {

    private val retainedOffers = ConcurrentHashMap<String, Offer>()

    private val propositionUpdates = MutableSharedFlow<Map<DecisionScope, Proposition>>(
        extraBufferCapacity = 1
    )

    // Registers the one, process-wide callback for Edge personalization responses and republishes
    // them onto propositionUpdates so fetchOffers can await just the scopes it asked for.
    init {
        Optimize.onPropositionsUpdate(object: AdobeCallbackWithError<Map<DecisionScope, Proposition>> {
            override fun call(propositions: Map<DecisionScope, Proposition>) {
                propositionUpdates.tryEmit(propositions)
            }

            override fun fail(error: AdobeError?) {}
        })
    }

    /** Triggers an Optimize fetch for the given scopes and waits until all of them have responded. */
    override suspend fun fetchOffers(scopeNames: List<String>): Map<String, List<PersonalizedOffer>> {
        val scopes = scopeNames.map { DecisionScope(it) }
        val accumulated = mutableMapOf<DecisionScope, Proposition>()

        withTimeoutOrNull(FETCH_TIMEOUT) {
            propositionUpdates
                .onSubscription { Optimize.updatePropositions(scopes, null, null) }
                .takeWhile { update ->
                    accumulated.putAll(update.filterKeys { it in scopes })
                    accumulated.keys.toSet() != scopes.toSet()
                }
                .collect { }
        }

        return scopeNames.associateWith { name ->
            accumulated[DecisionScope(name)]?.offers?.map { offer ->
                retainedOffers[offer.id] = offer
                offer.toPersonalizedOffer()
            }.orEmpty()
        }
    }

    /** Reports a personalized offer actually scrolled into view, using the retained SDK Offer. */
    override fun trackDisplayed(offerId: String) {
        retainedOffers[offerId]?.displayed()
    }

    /** Reports a tap on a personalized offer, using the retained SDK Offer. */
    override fun trackTapped(offerId: String) {
        retainedOffers[offerId]?.tapped()
    }

    /** Converts an SDK Offer into the app's plain model, branching on its content type. */
    private fun Offer.toPersonalizedOffer(): PersonalizedOffer = when (type) {
        OfferType.IMAGE -> PersonalizedOffer(id = id, title = "", body = "", imageUrl = content)
        OfferType.JSON -> parseJsonOffer(id, content)
        OfferType.HTML -> PersonalizedOffer(
            id = id,
            title = "Just for you",
            body = HtmlCompat.fromHtml(content, HtmlCompat.FROM_HTML_MODE_LEGACY).toString().trim()
        )
        else -> PersonalizedOffer(id = id, title = "Just for you", body = content)
    }

    /** Parses a JSON-type offer's content into title/body/image, falling back on malformed JSON. */
    private fun parseJsonOffer(id: String, content: String): PersonalizedOffer = try {
        val json = JSONObject(content)
        PersonalizedOffer(
            id = id,
            title = json.optString("title").ifBlank { "Just for you" },
            body = json.optString("body"),
            imageUrl = json.optString("image").takeIf { it.isNotBlank() }
        )
    } catch (_: org.json.JSONException) {
        PersonalizedOffer(id = id, title = "Just for you", body = content)
    }

    private companion object {
        val FETCH_TIMEOUT = 10.seconds
    }
}
