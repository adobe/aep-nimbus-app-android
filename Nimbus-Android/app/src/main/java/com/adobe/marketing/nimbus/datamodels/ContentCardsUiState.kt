package com.adobe.marketing.nimbus.datamodels

data class ContentCardsUiState (
    val cardsBySurface: Map<ContentSurface, List<ContentCard>> = emptyMap()
)