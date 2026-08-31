package com.adobe.marketing.nimbus.services

import com.adobe.marketing.nimbus.datamodels.CommerceEvent

interface AnalyticsService {
    fun track(event: CommerceEvent)
    fun trackAction(action: String, data: Map<String, String>? = null)
    fun trackState(state: String, data: Map<String, String>? = null)
}