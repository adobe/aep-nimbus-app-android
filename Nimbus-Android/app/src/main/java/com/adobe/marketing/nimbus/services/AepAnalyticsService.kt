package com.adobe.marketing.nimbus.services

import com.adobe.marketing.mobile.Edge
import com.adobe.marketing.mobile.ExperienceEvent
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.nimbus.datamodels.CommerceEvent
import javax.inject.Inject

/**
 * Powered by MobileCore's `trackAction`/`trackState` alongside a parallel `Edge.sendEvent` call
 * for the same interaction, dispatched together as a deliberate double-dispatch rather than a
 * redundancy: the MobileCore call is what AJO's rule engine keys its in-app-message and
 * content-card trigger conditions off of, while the Edge call is what actually lands the event
 * in the analytics dataset for reporting. This is the app capability behind every trackAction/
 * trackState call site (add-to-cart, checkout, tab switches, etc.) qualifying an AJO campaign.
 */
class AepAnalyticsService @Inject constructor(): AnalyticsService{

    /** Sends a commerce XDM event (add to cart, checkout, etc.) straight to Edge. */
    override fun track(event: CommerceEvent) {
        val experienceEvent = ExperienceEvent.Builder()
            .setXdmSchema(event.xdm)
            .build()
        Edge.sendEvent(experienceEvent, null)
    }

    /** Fires the MobileCore action call for AJO rules, plus an Edge action event for reporting. */
    override fun trackAction(action: String, data: Map<String, String>?) {
        MobileCore.trackAction(action, data)
        Edge.sendEvent(actionExperienceEvent(action, data), null)
    }

    /** Fires the MobileCore state call for AJO rules, plus an Edge screen-view event for reporting. */
    override fun trackState(state: String, data: Map<String, String>?) {
        MobileCore.trackState(state, data)
        Edge.sendEvent(stateExperienceEvent(state, data), null)
    }

    /** Builds the freeform `application.action` XDM event mirroring a trackAction call. */
    private fun actionExperienceEvent(action: String, data: Map<String, String>?): ExperienceEvent {
        val freeform = mutableMapOf<String, Any>("actionName" to action)
        data?.let {freeform.putAll(it)}
        return ExperienceEvent.Builder()
            .setXdmSchema(mapOf("eventType" to "application.action"))
            .setData(freeform)
            .build()
    }

    /** Builds the freeform `application.screenView` XDM event mirroring a trackState call. */
    private fun stateExperienceEvent(state: String, data: Map<String, String>?): ExperienceEvent {
        val freeform = mutableMapOf<String, Any>("screenName" to state)
        data?.let {freeform.putAll(it)}
        return ExperienceEvent.Builder()
            .setXdmSchema(mapOf("eventType" to "application.screenView"))
            .setData(freeform)
            .build()
    }
}