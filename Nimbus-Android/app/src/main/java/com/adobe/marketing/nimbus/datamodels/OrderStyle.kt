package com.adobe.marketing.nimbus.datamodels

enum class OrderStyle(val key: String, val label: String) {
    PROGRESS("progress", "Progress"),
    BIG_TEXT("big_text", "Big text"),
    STANDARD("standard", "Standard"),
    METRIC("metric", "Metric");

    companion object {
        fun fromKey(key: String?): OrderStyle? =
            entries.firstOrNull { it.key == key }
    }
}