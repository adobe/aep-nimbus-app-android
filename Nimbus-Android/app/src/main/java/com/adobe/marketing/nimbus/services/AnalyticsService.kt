package com.adobe.marketing.nimbus.services

import com.adobe.marketing.nimbus.datamodels.CommerceEvent

/**
 * Seam over MobileCore's trackAction/trackState plus Edge.sendEvent for commerce XDM events.
 * Backing implementation: [AepAnalyticsService]. Enables every screen's tracking calls (Shop
 * browsing, cart, checkout, tab switches) to feed both AJO's rule engine and the Edge dataset
 * without duplicating the double-dispatch logic at each call site.
 */
interface AnalyticsService {
    /** Sends a commerce XDM event directly to Edge. */
    fun track(event: CommerceEvent)
    /** Double-dispatches a named action to MobileCore and Edge. */
    fun trackAction(action: String, data: Map<String, String>? = null)
    /** Double-dispatches a named screen/state view to MobileCore and Edge. */
    fun trackState(state: String, data: Map<String, String>? = null)
}