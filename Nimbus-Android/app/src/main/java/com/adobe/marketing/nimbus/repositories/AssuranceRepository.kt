package com.adobe.marketing.nimbus.repositories

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Powered by the AEP Assurance extension's `Assurance.startSession` call, made from Profile
 * or `MainActivity`'s deep-link handler. This repository only records "we told the SDK to
 * start a session with this URL" — Assurance has no API to query live connection status, so
 * the app-side "connected" state here is an honest approximation, not a real status signal.
 */
@Singleton
class AssuranceRepository @Inject constructor(){
    private val _sessionUrl = MutableStateFlow<String?>(null)
    val sessionUrl: StateFlow<String?> = _sessionUrl.asStateFlow()

    /** Records the URL a session was started with, so Profile can show it as "connected". */
    fun recordSessionStarted(url: String) {
        _sessionUrl.update { url }
    }
}