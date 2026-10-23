package com.adobe.marketing.nimbus.services

import com.adobe.marketing.mobile.messaging.liveupdate.LiveUpdatePayload
import com.adobe.marketing.nimbus.datamodels.OrderStep
import com.adobe.marketing.nimbus.datamodels.OrderStyle
import com.adobe.marketing.nimbus.datamodels.OrderTrackingState
import org.json.JSONObject

internal object OrderPayloadParser {
    private const val KEY_STEP = "custom_key_step"
    private const val KEY_ETA_DAYS = "custom_key_eta_days"
    private const val KEY_STYLE = "custom_key_style"

    fun parse(payload: LiveUpdatePayload, currentStyle: OrderStyle?): OrderTrackingState? {
        val state = payload.contentState ?: return null
        val step = OrderStep.fromIndex(state.optInt(KEY_STEP, -1)) ?: return null
        return OrderTrackingState(
            orderNumber = payload.notificationId,
            step = step,
            etaDays = state.optInt(KEY_ETA_DAYS, 0),
            style = OrderStyle.fromKey(state.optString(KEY_STYLE)) ?: currentStyle ?: OrderStyle.PROGRESS
        )
    }

    fun contentState(step: OrderStep, etaDays: Int, style: OrderStyle): JSONObject =
        JSONObject()
            .put(KEY_STEP, step.ordinal)
            .put(KEY_ETA_DAYS, etaDays)
            .put(KEY_STYLE, style.key)
}