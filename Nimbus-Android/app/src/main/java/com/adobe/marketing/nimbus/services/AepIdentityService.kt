package com.adobe.marketing.nimbus.services

import com.adobe.marketing.mobile.edge.identity.AuthenticatedState
import com.adobe.marketing.mobile.edge.identity.Identity
import com.adobe.marketing.mobile.edge.identity.IdentityItem
import com.adobe.marketing.mobile.edge.identity.IdentityMap
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject

class AepIdentityService @Inject constructor() : IdentityService {

    override suspend fun experienceCloudId(): String? =
        suspendCancellableCoroutine { continuation ->
            Identity.getExperienceCloudId { ecid ->
                continuation.resume(ecid)
            }
        }

    override suspend fun loggedInEmail(): String? =
        suspendCancellableCoroutine { continuation ->
            Identity.getIdentities { identityMap ->
                val email = identityMap
                    ?.getIdentityItemsForNamespace(NAMESPACE_EMAIL)
                    ?.firstOrNull { it.authenticatedState == AuthenticatedState.AUTHENTICATED }
                    ?.id
                continuation.resume(email)
            }
        }

    override fun login(username: String) {
        val identityMap = IdentityMap()
        identityMap.addItem(
            IdentityItem(username, AuthenticatedState.AUTHENTICATED, true),
            NAMESPACE_EMAIL
        )
        Identity.updateIdentities(identityMap)
    }

    override suspend fun logout() = suspendCancellableCoroutine<Unit> {  continuation ->
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