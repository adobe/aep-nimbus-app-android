package com.adobe.marketing.nimbus.datamodels

data class Offer (
    val id: String,
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val actionUrl: String? = null,
    val dismissible: Boolean = false
)