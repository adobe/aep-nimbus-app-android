package com.adobe.marketing.nimbus.services

import com.adobe.marketing.mobile.messaging.liveupdate.LiveUpdatePayload
import com.adobe.marketing.nimbus.datamodels.FlashSaleState
import org.json.JSONObject

internal object SalePayloadParser {
    private const val KEY_DISCOUNT_PERCENT = "custom_key_sale_discount"

    fun isSale(payload: LiveUpdatePayload): Boolean =
        payload.contentState?.has(KEY_DISCOUNT_PERCENT) == true

    fun parse(payload: LiveUpdatePayload): FlashSaleState? {
        val state = payload.contentState ?: return null
        if (!state.has(KEY_DISCOUNT_PERCENT)) return null
        return FlashSaleState(discountPercent = state.optInt(KEY_DISCOUNT_PERCENT, 0))
    }

    fun contentState(discountPercent: Int): JSONObject =
        JSONObject().put(KEY_DISCOUNT_PERCENT, discountPercent)
}