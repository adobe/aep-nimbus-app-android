package com.adobe.marketing.nimbus.datamodels

enum class OrderStep (val label: String) {
    PLACED("Order placed"),
    PACKED("Packed"),
    SHIPPED("Shipped"),
    OUT_FOR_DELIVERY("Out for delivery"),
    DELIVERED("Delivered");

    val next: OrderStep? get() = entries.getOrNull(ordinal + 1)
    val isTerminal: Boolean get() = this == DELIVERED

    companion object {
        fun fromIndex(index: Int): OrderStep? = entries.getOrNull(index)
    }
}