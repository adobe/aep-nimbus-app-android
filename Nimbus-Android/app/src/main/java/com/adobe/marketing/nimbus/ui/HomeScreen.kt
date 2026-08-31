package com.adobe.marketing.nimbus.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adobe.marketing.nimbus.datamodels.ContentCard
import com.adobe.marketing.nimbus.datamodels.ContentSurface
import com.adobe.marketing.nimbus.viewmodels.ContentCardsViewModel

@Composable
fun HomeScreen(viewModel: ContentCardsViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        viewModel.ensureLoaded(ContentSurface.HOME)
    }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cards = uiState.cardsBySurface[ContentSurface.HOME].orEmpty()
    val context = LocalContext.current

    HomeContent(
        cards = cards,
        onCardTap = { card ->
            viewModel.onCardInteracted(card.id)
            card.actionUrl?.let { url ->
                try {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (e: ActivityNotFoundException) {
                    // No app can handle this URL - ignore.
                }
            }
        },
        onCardDisplayed = { viewModel.onCardDisplayed(it.id) }
    )
}

@Composable
private fun HomeContent(
    cards: List<ContentCard>,
    onCardTap: (ContentCard) -> Unit,
    onCardDisplayed: (ContentCard) -> Unit
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
private fun ContentCardTile(card: ContentCard, onTap: () -> Unit, onDisplayed: () -> Unit) {
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
