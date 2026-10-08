package com.adobe.marketing.nimbus.repositories

import com.adobe.marketing.nimbus.datamodels.OrderTrackingState
import com.adobe.marketing.nimbus.services.LiveUpdateService
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

class LiveUpdateRepository @Inject constructor(
    private val liveUpdateService: LiveUpdateService
) {
    val orderState: StateFlow<OrderTrackingState?> = liveUpdateService.orderState

    fun startOrder(): Boolean = liveUpdateService.startOrder()
    fun advanceOrder(): Boolean = liveUpdateService.advanceOrder()
    fun endOrder(): Boolean = liveUpdateService.endOrder()
}