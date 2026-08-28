package com.adobe.marketing.nimbus.repositories

import com.adobe.marketing.nimbus.services.IdentityService
import javax.inject.Inject

class LoginRepository @Inject constructor(
    private val identityService: IdentityService
) {
    suspend fun signedInEmail(): String? = identityService.loggedInEmail()

    fun login(username: String) = identityService.login(username)

    fun logout() = identityService.logout()
}