package com.adobe.marketing.nimbus.services

interface IdentityService {
    suspend fun experienceCloudId(): String?
    suspend fun loggedInEmail(): String?
    fun login(username: String)
    fun logout()
}