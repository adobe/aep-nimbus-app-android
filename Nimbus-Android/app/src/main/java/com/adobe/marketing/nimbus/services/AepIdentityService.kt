package com.adobe.marketing.nimbus.services

import com.adobe.marketing.mobile.edge.identity.AuthenticatedState
import com.adobe.marketing.mobile.edge.identity.Identity
import com.adobe.marketing.mobile.edge.identity.IdentityItem
import com.adobe.marketing.mobile.edge.identity.IdentityMap
import com.adobe.marketing.nimbus.datamodels.IdentityMapEntry
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject

/**
 * Powered by the AEP Edge Identity extension (`Identity.getExperienceCloudId`,
 * `Identity.getIdentities`, `Identity.updateIdentities`/`removeIdentity`). This is the
 * capability behind the Login gate's log-in/guest flow, Profile's ECID display, and the
 * Identity Map section that lists every namespace/ID pair currently known to Edge Identity
 * (ECID always present, an authenticated Email identity once the user logs in).
 */
class AepIdentityService @Inject constructor() : IdentityService {

    /** Reads the device's Experience Cloud ID, shown truncated on the Profile screen. */
    override suspend fun experienceCloudId(): String? =
        suspendCancellableCoroutine { continuation ->
            Identity.getExperienceCloudId { ecid ->
                continuation.resume(ecid)
            }
        }

    /** Returns the authenticated Email identity, if the user is currently logged in. */
    override suspend fun loggedInEmail(): String? = suspendCancellableCoroutine { continuation ->
        Identity.getIdentities { identityMap ->
            val email = identityMap?.getIdentityItemsForNamespace(NAMESPACE_EMAIL)
                ?.firstOrNull { it.authenticatedState == AuthenticatedState.AUTHENTICATED }?.id
            continuation.resume(email)
        }
    }

    /** Flattens every namespace/ID pair Edge Identity knows about, for Profile's Identity Map. */
    override suspend fun identityMap(): List<IdentityMapEntry> =
        suspendCancellableCoroutine { continuation ->
            Identity.getIdentities { identityMap ->
                val entries = identityMap?.getNamespaces()?.flatMap { namespace ->
                    identityMap.getIdentityItemsForNamespace(namespace).map { item ->
                        IdentityMapEntry(
                            namespace = namespace,
                            id = item.id,
                            authenticatedState = item.authenticatedState.name,
                            isPrimary = item.isPrimary
                        )
                    }
                }.orEmpty()
                continuation.resume(entries)
            }
        }

    /** Adds an authenticated Email identity, linking this device to the given username. */
    override fun login(username: String) {
        val identityMap = IdentityMap()
        identityMap.addItem(
            IdentityItem(username, AuthenticatedState.AUTHENTICATED, true), NAMESPACE_EMAIL
        )
        Identity.updateIdentities(identityMap)
    }

    /** Removes every Email identity item, unlinking this device from the signed-in user. */
    override suspend fun logout() = suspendCancellableCoroutine<Unit> { continuation ->
        Identity.getIdentities { identityMap ->
            identityMap?.getIdentityItemsForNamespace(NAMESPACE_EMAIL)?.forEach { item ->
                Identity.removeIdentity(item, NAMESPACE_EMAIL)
            }
            continuation.resume(Unit)
        }
    }

    private companion object {
        const val NAMESPACE_EMAIL = "Email"
    }
}