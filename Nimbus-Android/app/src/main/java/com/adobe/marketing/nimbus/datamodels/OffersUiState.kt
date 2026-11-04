package com.adobe.marketing.nimbus.datamodels

data class OffersUiState (
    val offersBySurface: Map<OfferSurface, List<Offer>> = emptyMap(),
    val isRefreshing: Boolean = false
)