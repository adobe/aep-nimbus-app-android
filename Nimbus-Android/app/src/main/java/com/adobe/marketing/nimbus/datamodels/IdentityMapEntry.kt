package com.adobe.marketing.nimbus.datamodels

data class IdentityMapEntry (
    val namespace: String,
    val id: String,
    val authenticatedState: String,
    val isPrimary: Boolean
)