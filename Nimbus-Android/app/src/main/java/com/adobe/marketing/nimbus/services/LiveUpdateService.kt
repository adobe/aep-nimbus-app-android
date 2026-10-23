package com.adobe.marketing.nimbus.services

import com.adobe.marketing.nimbus.datamodels.OrderStyle
import com.adobe.marketing.nimbus.datamodels.OrderTrackingState
import kotlinx.coroutines.flow.StateFlow

interface LiveUpdateService {
    val orderState: StateFlow<OrderTrackingState?>
    fun startOrder(): Boolean
    fun advanceOrder(): Boolean
    fun endOrder(): Boolean
    fun setOrderStyle(style: OrderStyle): Boolean
}