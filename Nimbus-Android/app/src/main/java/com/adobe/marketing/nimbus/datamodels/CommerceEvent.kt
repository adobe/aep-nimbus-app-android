package com.adobe.marketing.nimbus.datamodels

enum class CommerceEventType {
    PRODUCT_LIST_ADDS,
    PRODUCT_LIST_REMOVES,
    PURCHASES
}

data class CommerceEvent (
    val type: CommerceEventType,
    val xdm: Map<String, Any>
)