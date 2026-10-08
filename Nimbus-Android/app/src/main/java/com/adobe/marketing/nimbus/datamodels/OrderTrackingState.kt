package com.adobe.marketing.nimbus.datamodels

data class OrderTrackingState (
    val orderNumber: String,
    val step: OrderStep,
    val etaDays: Int
)