package com.adobe.marketing.nimbus.repositories

import com.adobe.marketing.nimbus.data.LoginPreferences
import com.adobe.marketing.nimbus.datamodels.IdentityMapEntry
import com.adobe.marketing.nimbus.datamodels.LoginGateState
import com.adobe.marketing.nimbus.services.IdentityService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Powered by the AEP Edge Identity extension via [IdentityService], combined with the durable
 * `hasPassedLoginGate` flag in [LoginPreferences]. This is the single source of truth behind
 * the Login gate and Profile's login/logout: the gate-passed flag is set once by either login
 * or guest choice and never cleared afterward, which is what lets Profile's login/logout
 * actions stay inline on that screen instead of ever re-triggering the first-launch gate.
 */
@Singleton
class LoginRepository @Inject constructor(
    private val identityService: IdentityService,
    private val loginPreferences: LoginPreferences
) {
    private val signedInUser = MutableStateFlow<String?>(null)
    private val isChecking = MutableStateFlow(true)

    /** Combines the in-memory signed-in user with the durable gate-passed flag. */
    val loginGateState: Flow<LoginGateState> = combine(
        signedInUser, loginPreferences.hasPassedLoginGate, isChecking
    ) { user, hasPassedLoginGate, checking ->
        LoginGateState(
            isChecking = checking,
            signedInUser = user,
            hasSeenLoginPrompt = user != null || hasPassedLoginGate
        )
    }

    /** Reads any already-authenticated identity once at app start. */
    suspend fun initialize() {
        signedInUser.update { identityService.loggedInEmail() }
        isChecking.update { false }
    }

    /** The device's Experience Cloud ID. */
    suspend fun experienceCloudId(): String? = identityService.experienceCloudId()

    /** Links this device to a username and durably marks the gate as passed. */
    suspend fun login(username: String) {
        identityService.login(username)
        signedInUser.update { username }
        loginPreferences.markLoginGatePassed()
    }

    /** Every namespace/ID pair Edge Identity currently knows about this device. */
    suspend fun identityMap(): List<IdentityMapEntry> = identityService.identityMap()

    /** Durably marks the gate as passed without logging in. */
    suspend fun continueAsGuest() {
        loginPreferences.markLoginGatePassed()
    }

    /** Unlinks this device from its signed-in identity; never clears the gate-passed flag. */
    suspend fun logout() {
        identityService.logout()
        signedInUser.update { null }
    }
}