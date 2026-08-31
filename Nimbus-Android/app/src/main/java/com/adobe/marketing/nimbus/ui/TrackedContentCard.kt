package com.adobe.marketing.nimbus.ui

import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.adobe.marketing.nimbus.datamodels.ContentCard

@Composable
fun TrackedContentCard(
    card: ContentCard,
    onTap: () -> Unit,
    onDisplayed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    LaunchedEffect(card.id) {
        onDisplayed()
    }
    Card(onClick = onTap, modifier = modifier) {
        content()
    }
}
