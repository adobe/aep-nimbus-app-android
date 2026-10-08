package com.adobe.marketing.nimbus.data

import com.adobe.marketing.nimbus.datamodels.ProfileAttributes

object MockProfile {
    val attributes = ProfileAttributes (
        loyaltyTier = "Gold",
        memberSince = "2023",
        lifetimePurchases = 12
    )
}