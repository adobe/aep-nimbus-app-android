package com.adobe.marketing.nimbus.services

import com.adobe.marketing.mobile.edge.consent.Consent
import com.adobe.marketing.nimbus.datamodels.ConsentState
import javax.inject.Inject

/**
 * Powered by the AEP Edge Consent extension's `Consent.update` API. This is the capability
 * behind the Consent gate on first launch and the Profile "Allow Data Collection" toggle:
 * flipping the `collect` consent value live suppresses or resumes outbound Edge Network
 * calls at the SDK level, without the app needing to gate its own trackAction/trackState/
 * Edge.sendEvent call sites individually.
 */
class AepConsentService @Inject constructor():
    ConsentService {
        /** Maps the app's ConsentState to the SDK's y/n/p `collect` value and pushes it live. */
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