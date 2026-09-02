package com.adobe.marketing.nimbus.repositories

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AssuranceRepository @Inject constructor(){
    private val _sessionUrl = MutableStateFlow<String?>(null)
    val sessionUrl: StateFlow<String?> = _sessionUrl.asStateFlow()

    fun recordSessionStarted(url: String) {
        _sessionUrl.value = url
    }
}