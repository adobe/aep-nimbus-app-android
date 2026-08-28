package com.adobe.marketing.nimbus.services

import com.adobe.marketing.mobile.edge.consent.Consent
import com.adobe.marketing.nimbus.datamodels.ConsentState
import javax.inject.Inject

class AepConsentService @Inject constructor():
    ConsentService {
        override fun update(state: ConsentState) {
            val value = when (state) {
                ConsentState.YES -> "y"
                ConsentState.NO -> "n"
                ConsentState.PENDING -> "p"
            }

            val consents: Map<String, Any> = mapOf(
                "consents" to mapOf(
                    "collect" to mapOf("val" to value)
                )
            )
            Consent.update(consents)
    }
}