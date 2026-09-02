package com.adobe.marketing.nimbus.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adobe.marketing.nimbus.datamodels.OfferSurface
import com.adobe.marketing.nimbus.viewmodels.OffersViewModel
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import coil3.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.height
import com.adobe.marketing.mobile.aepcomposeui.InboxEvent
import com.adobe.marketing.mobile.aepcomposeui.UIEvent
import com.adobe.marketing.mobile.aepcomposeui.observers.AepInboxEventObserver
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.mobile.aepcomposeui.components.AepInbox
import com.adobe.marketing.mobile.aepcomposeui.state.InboxUIState
import com.adobe.marketing.mobile.messaging.MessagingInboxProvider
import com.adobe.marketing.mobile.messaging.Surface
import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.adobe.marketing.nimbus.datamodels.Offer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(viewModel: OffersViewModel = hiltViewModel()) {
    var mode by remember { mutableStateOf(RenderMode.CUSTOM) }
    val inboxProvider = remember {
        MessagingInboxProvider(Surface(OfferSurface.INBOX.path))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        RenderModeSelector(mode = mode, onModeSelected = { mode = it })

        when (mode) {
            RenderMode.CUSTOM -> {
                LaunchedEffect(Unit) {
                    viewModel.ensureLoaded(OfferSurface.INBOX)
                }
                val uiState = viewModel.uiState.collectAsStateWithLifecycle()
                val cards by remember {
                    derivedStateOf {
                        uiState.value.offersBySurface[OfferSurface.INBOX].orEmpty()
                    }
                }
                val context = LocalContext.current

                OfferFetchFailureToast(viewModel.fetchFailed)

                PullToRefreshBox(
                    isRefreshing = uiState.value.isRefreshing,
                    onRefresh = { viewModel.refresh(OfferSurface.INBOX) }) {
                    InboxContent(
                        cards = cards,
                        onCardTap = { card ->
                            viewModel.onCardInteracted(card.id)
                            card.actionUrl?.let { url ->
                                try {
                                    context.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW, url.toUri()
                                        )
                                    )
                                } catch (_: ActivityNotFoundException) {
                                    // No app can handle this URL - ignore.
                                }
                            }
                        },
                        onCardDisplayed = { viewModel.onCardDisplayed(it.id) },
                        onDismiss = { viewModel.onCardDismissed(OfferSurface.INBOX, it.id) })
                }
            }

            RenderMode.SDK_UI -> {
                val inboxUiFlow = remember(inboxProvider) { inboxProvider.getInboxUI() }
                val uiState by inboxUiFlow.collectAsStateWithLifecycle(initialValue = InboxUIState.Loading)

                AepInbox(
                    uiState = uiState, observer = object : AepInboxEventObserver {
                        override fun onInboxEvent(event: InboxEvent) {
                            if (event is InboxEvent.Display) {
                                Log.debug(
                                    "Nimbus", "InboxScreen", "SDK UI inbox displayed "
                                )
                            }
                        }

                        override fun onEvent(event: UIEvent<*, *>) {
                            when (event) {
                                is UIEvent.Display -> Log.debug(
                                    "Nimbus",
                                    "InboxScreen",
                                    "SDK UI card displayed: ${event.aepUi.getTemplate().id}"
                                )

                                is UIEvent.Interact -> Log.debug(
                                    "Nimbus",
                                    "InboxScreen",
                                    "SDK UI card interacted: ${event.aepUi.getTemplate().id}"
                                )

                                is UIEvent.Dismiss -> Log.debug(
                                    "Nimbus",
                                    "InboxScreen",
                                    "SDK UI card dismissed: ${event.aepUi.getTemplate().id}"
                                )
                            }
                        }
                    })
            }
        }
    }
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
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(cards, key = { it.id }) { card ->
            InboxCard(
                card = card,
                onTap = { onCardTap(card) },
                onDisplayed = { onCardDisplayed(card) },
                onDismiss = { onDismiss(card) })
        }
    }
}

@Composable
private fun EmptyInboxState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GateIconBadge(icon = Icons.Default.Inventory2)
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Nothing here yet", style = MaterialTheme.typography.titleMedium
        )
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
    card: Offer, onTap: () -> Unit, onDisplayed: () -> Unit, onDismiss: () -> Unit
) {
    TrackedContentCard(
        card = card, onTap = onTap, onDisplayed = onDisplayed, modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Text(
                card.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Text(card.body, style = MaterialTheme.typography.bodyMedium)
            TextButton(onClick = onDismiss) { Text("Dismiss") }
        }
    }
}