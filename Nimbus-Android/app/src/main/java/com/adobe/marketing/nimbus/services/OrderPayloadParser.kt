package com.adobe.marketing.nimbus.services

import com.adobe.marketing.mobile.messaging.liveupdate.LiveUpdatePayload
import com.adobe.marketing.nimbus.datamodels.OrderStep
import com.adobe.marketing.nimbus.datamodels.OrderTrackingState
import org.json.JSONObject

internal object OrderPayloadParser {
    private const val KEY_STEP = "custom_key_step"
    private const val KEY_ETA_DAYS = "custom_key_eta_days"

    fun parse(payload: LiveUpdatePayload): OrderTrackingState? {
        val state = payload.contentState ?: return null
        val step = OrderStep.fromIndex(state.optInt(KEY_STEP, -1)) ?: return null
        return OrderTrackingState(
            orderNumber = payload.notificationId,
            step = step,
            etaDays = state.optInt(KEY_ETA_DAYS, 0)
        )
    }

    fun contentState(step: OrderStep, etaDays: Int): JSONObject =
        JSONObject().put(KEY_STEP, step.ordinal).put(KEY_ETA_DAYS, etaDays)
}