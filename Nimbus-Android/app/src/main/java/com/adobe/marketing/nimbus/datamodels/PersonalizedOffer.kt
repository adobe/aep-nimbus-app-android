package com.adobe.marketing.nimbus.datamodels

data class PersonalizedOffer (
    val id: String,
    val title: String,
    val body: String,
    val imageUrl: String? = null
)