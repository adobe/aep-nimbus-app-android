package com.adobe.marketing.nimbus.services

import com.adobe.marketing.nimbus.datamodels.IdentityMapEntry

/**
 * Seam over the AEP Edge Identity extension, so no other layer imports `Identity` directly.
 * Backing implementation: [AepIdentityService]. Enables the Login gate, Profile's ECID and
 * Identity Map display, and login/logout via the Email identity namespace.
 */
interface IdentityService {
    /** The device's Experience Cloud ID. */
    suspend fun experienceCloudId(): String?
    /** The currently authenticated Email identity, if logged in. */
    suspend fun loggedInEmail(): String?
    /** Every namespace/ID pair Edge Identity currently knows about this device. */
    suspend fun identityMap(): List<IdentityMapEntry>
    /** Links this device to a username via an authenticated Email identity. */
    fun login(username: String)
    /** Unlinks this device from its signed-in Email identity. */
    suspend fun logout()
}