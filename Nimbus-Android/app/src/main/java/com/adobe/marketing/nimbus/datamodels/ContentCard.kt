package com.adobe.marketing.nimbus.datamodels

data class ContentCard (
    val id: String,
    val title: String,
    val body: String,
    val imageUrl: String? = null,
    val actionUrl: String? = null
)