package com.adobe.marketing.nimbus.utils

import com.adobe.marketing.nimbus.datamodels.OrderStep

object OrderEtaFormat {
    fun text(step: OrderStep, etaDays: Int): String = when {
        step.isTerminal -> "Delivered"
        etaDays == 1 -> "Arriving in 1 day"
        else -> "Arriving in $etaDays days"
    }
}