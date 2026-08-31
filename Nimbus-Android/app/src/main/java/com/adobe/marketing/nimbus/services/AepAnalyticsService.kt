package com.adobe.marketing.nimbus.services

import com.adobe.marketing.mobile.Edge
import com.adobe.marketing.mobile.ExperienceEvent
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.nimbus.datamodels.CommerceEvent
import javax.inject.Inject

class AepAnalyticsService @Inject constructor(): AnalyticsService{

    override fun track(event: CommerceEvent) {
        val experienceEvent = ExperienceEvent.Builder()
            .setXdmSchema(event.xdm)
            .build()
        Edge.sendEvent(experienceEvent, null)
    }

    override fun trackAction(action: String, data: Map<String, String>?) {
        MobileCore.trackAction(action, data)
        Edge.sendEvent(actionExperienceEvent(action, data), null)
    }

    override fun trackState(state: String, data: Map<String, String>?) {
        MobileCore.trackState(state, data)
        Edge.sendEvent(stateExperienceEvent(state, data), null)
    }

    private fun actionExperienceEvent(action: String, data: Map<String, String>?): ExperienceEvent {
        val freeform = mutableMapOf<String, Any>("actionName" to action)
        data?.let {freeform.putAll(it)}
        return ExperienceEvent.Builder()
            .setXdmSchema(mapOf("eventType" to "application.action"))
            .setData(freeform)
            .build()
    }

    private fun stateExperienceEvent(state: String, data: Map<String, String>?): ExperienceEvent {
        val freeform = mutableMapOf<String, Any>("screenName" to state)
        data?.let {freeform.putAll(it)}
        return ExperienceEvent.Builder()
            .setXdmSchema(mapOf("eventType" to "application.screenView"))
            .setData(freeform)
            .build()
    }
}