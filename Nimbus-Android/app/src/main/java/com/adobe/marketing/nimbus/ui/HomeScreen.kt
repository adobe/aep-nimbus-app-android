package com.adobe.marketing.nimbus.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adobe.marketing.nimbus.datamodels.Offer
import com.adobe.marketing.nimbus.datamodels.OfferSurface
import com.adobe.marketing.nimbus.viewmodels.OffersViewModel

@Composable
fun HomeScreen(viewModel: OffersViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        viewModel.ensureLoaded(OfferSurface.HOME)
    }
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val cards by remember { derivedStateOf {
        uiState.value.offersBySurface[OfferSurface.HOME].orEmpty() } }
    val context = LocalContext.current

    PullToRefreshBox(
        isRefreshing = uiState.value.isRefreshing,
        onRefresh = { viewModel.refresh(OfferSurface.HOME) }
    ) {
        HomeContent(
            cards = cards,
            onCardTap = { card ->
                viewModel.onCardInteracted(card.id)
                card.actionUrl?.let { url ->
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    } catch (_: ActivityNotFoundException) {
                        // No app can handle this URL - ignore.
                    }
                }
            },
            onCardDisplayed = { viewModel.onCardDisplayed(it.id) }
        )
    }
}

@Composable
private fun HomeContent(
    cards: List<Offer>,
    onCardTap: (Offer) -> Unit,
    onCardDisplayed: (Offer) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        if (cards.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(cards, key = { it.id }) { card ->
                    ContentCardTile(
                        card = card,
                        onTap = { onCardTap(card) },
                        onDisplayed = { onCardDisplayed(card) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ContentCardTile(card: Offer, onTap: () -> Unit, onDisplayed: () -> Unit) {
    TrackedContentCard(
        card = card,
        onTap = onTap,
        onDisplayed = onDisplayed,
        modifier = Modifier.width(220.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = card.title,
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(
                text = card.body,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
