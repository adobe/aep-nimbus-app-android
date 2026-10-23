package com.adobe.marketing.nimbus.services

import com.adobe.marketing.nimbus.datamodels.ConsentState

/**
 * Seam over the AEP Edge Consent extension, so no other layer imports `Consent` directly.
 * Backing implementation: [AepConsentService]. Enables the Consent gate and Profile's
 * data-collection toggle without any SDK-specific type leaking past this boundary.
 */
interface ConsentService {
    /** Pushes a new consent choice to the SDK, live. */
    fun update(state: ConsentState)
}