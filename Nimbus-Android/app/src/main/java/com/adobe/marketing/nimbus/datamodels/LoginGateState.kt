package com.adobe.marketing.nimbus.datamodels

data class LoginGateState (
    val isChecking: Boolean = true,
    val signedInUser: String? = null,
    val hasSeenLoginPrompt: Boolean = true
)