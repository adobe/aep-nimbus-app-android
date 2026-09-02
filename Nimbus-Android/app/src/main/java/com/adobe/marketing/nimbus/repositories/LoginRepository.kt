package com.adobe.marketing.nimbus.repositories

import com.adobe.marketing.nimbus.data.LoginPreferences
import com.adobe.marketing.nimbus.datamodels.LoginGateState
import com.adobe.marketing.nimbus.services.IdentityService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val identityService: IdentityService, private val loginPreferences: LoginPreferences
) {
    private val signedInUser = MutableStateFlow<String?>(null)
    private val isChecking = MutableStateFlow(true)

    val loginGateState: Flow<LoginGateState> = combine(
        signedInUser, loginPreferences.hasChosenGuest, isChecking
    ) { user, hasChosenGuest, checking ->
        LoginGateState(
            isChecking = checking,
            signedInUser = user,
            hasSeenLoginPrompt = user != null || hasChosenGuest
        )
    }

    suspend fun initialize() {
        signedInUser.value = identityService.loggedInEmail()
        isChecking.value = false
    }

    suspend fun experienceCloudId(): String? = identityService.experienceCloudId()

    suspend fun login(username: String) {

        identityService.login(username)
        loginPreferences.clearChoseGuest()
        signedInUser.value = username
    }

    suspend fun continueAsGuest() {
        loginPreferences.setChoseGuest()
    }

    suspend fun logout() {
        identityService.logout()
        loginPreferences.clearChoseGuest()
        signedInUser.value = null
    }
}