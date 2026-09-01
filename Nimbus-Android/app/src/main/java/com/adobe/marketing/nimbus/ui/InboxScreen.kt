package com.adobe.marketing.nimbus.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adobe.marketing.nimbus.datamodels.Offer
import com.adobe.marketing.nimbus.datamodels.OfferSurface
import com.adobe.marketing.nimbus.viewmodels.OffersViewModel
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.core.net.toUri

@Composable
fun InboxScreen(viewModel: OffersViewModel = hiltViewModel()) {
    LaunchedEffect(Unit) {
        viewModel.ensureLoaded(OfferSurface.INBOX)
    }
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val cards by remember { derivedStateOf { uiState.value.offersBySurface[OfferSurface.INBOX].orEmpty() } }
    val context = LocalContext.current

    InboxContent(
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
        onCardDisplayed = { viewModel.onCardDisplayed(it.id)},
        onDismiss = { viewModel.onCardDismissed(OfferSurface.INBOX, it.id) }
    )
}

@Composable
private fun InboxContent(
    cards: List<Offer>,
    onCardTap: (Offer) -> Unit,
    onCardDisplayed: (Offer) -> Unit,
    onDismiss: (Offer) -> Unit
) {
    if (cards.isEmpty()) {
        EmptyInboxState()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cards, key = { it.id }) { card ->
            InboxCard(
                card = card,
                onTap = { onCardTap(card) },
                onDisplayed = { onCardDisplayed(card) },
                onDismiss = { onDismiss(card) }
            )
        }
    }
}

@Composable
private fun EmptyInboxState() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GateIconBadge(icon = Icons.Default.Inventory2)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "Nothing here yet", style =
            MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "New messages and offers will show up here.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InboxCard(
    card: Offer,
    onTap: () -> Unit,
    onDisplayed: () -> Unit,
    onDismiss: () -> Unit
) {
    TrackedContentCard(
        card = card,
        onTap = onTap,
        onDisplayed = onDisplayed,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(card.title, style = MaterialTheme.typography.titleMedium)
            Text(card.body, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}