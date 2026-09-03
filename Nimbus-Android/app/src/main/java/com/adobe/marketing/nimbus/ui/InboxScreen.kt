package com.adobe.marketing.nimbus.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import com.adobe.marketing.mobile.aepcomposeui.InboxEvent
import com.adobe.marketing.mobile.aepcomposeui.UIEvent
import com.adobe.marketing.mobile.aepcomposeui.components.AepInbox
import com.adobe.marketing.mobile.aepcomposeui.observers.AepInboxEventObserver
import com.adobe.marketing.mobile.aepcomposeui.state.InboxUIState
import com.adobe.marketing.mobile.aepcomposeui.style.AepCardStyle
import com.adobe.marketing.mobile.aepcomposeui.style.AepImageStyle
import com.adobe.marketing.mobile.aepcomposeui.style.AepUIStyle
import com.adobe.marketing.mobile.aepcomposeui.style.ImageOnlyUIStyle
import com.adobe.marketing.mobile.aepcomposeui.style.LargeImageUIStyle
import com.adobe.marketing.mobile.aepcomposeui.style.SmallImageUIStyle
import com.adobe.marketing.mobile.messaging.MessagingInboxProvider
import com.adobe.marketing.mobile.messaging.Surface
import com.adobe.marketing.nimbus.datamodels.Offer
import com.adobe.marketing.nimbus.datamodels.OfferSurface
import com.adobe.marketing.nimbus.viewmodels.OffersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InboxScreen(viewModel: OffersViewModel = hiltViewModel()) {
    var mode by remember { mutableStateOf(RenderMode.CUSTOM) }
    val inboxProvider = remember {
        MessagingInboxProvider(Surface(OfferSurface.INBOX.path))
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Inbox & Offers",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
        )

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
                    onRefresh = { viewModel.refresh(OfferSurface.INBOX) },
                    modifier = Modifier.fillMaxSize()
                ) {
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
                        onDismiss = { viewModel.onCardDismissed(OfferSurface.INBOX, it.id) }
                    )
                }
            }

            RenderMode.SDK_UI -> {
                val inboxUiFlow = remember(inboxProvider) { inboxProvider.getInboxUI() }
                val uiState by inboxUiFlow.collectAsStateWithLifecycle(initialValue = InboxUIState.Loading)

                Box(modifier = Modifier.fillMaxSize()) {
                    AepInbox(
                        uiState = uiState,
                        itemsStyle = AepUIStyle(
                            smallImageUIStyle = SmallImageUIStyle.Builder()
                                .cardStyle(AepCardStyle(modifier = Modifier.fillMaxWidth()))
                                .build(),
                            largeImageUIStyle = LargeImageUIStyle.Builder()
                                .cardStyle(AepCardStyle(modifier = Modifier.fillMaxWidth()))
                                .imageStyle(AepImageStyle(modifier = Modifier.fillMaxWidth()))
                                .build(),
                            imageOnlyUIStyle = ImageOnlyUIStyle.Builder()
                                .cardStyle(AepCardStyle(modifier = Modifier.fillMaxWidth()))
                                .imageStyle(AepImageStyle(modifier = Modifier.fillMaxWidth()))
                                .build()
                        ),
                        observer = object : AepInboxEventObserver {
                            override fun onInboxEvent(event: InboxEvent) {}
                            override fun onEvent(event: UIEvent<*, *>) {}
                        }
                    )
                }
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
    val validCards = cards.filter { it.title.isNotBlank() || it.body.isNotBlank() || !it.imageUrl.isNullOrBlank() }

    if (validCards.isEmpty()) {
        EmptyInboxState()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(validCards, key = { it.id }) { card ->
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
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        GateIconBadge(icon = Icons.Default.Inventory2)
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Nothing here yet",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
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
            if (!card.imageUrl.isNullOrBlank()) {
                SubcomposeAsyncImage(
                    model = card.imageUrl,
                    contentDescription = card.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BrokenImage,
                                contentDescription = "Image failed to load",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
            if (card.title.isNotBlank()) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            if (card.body.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Dismiss",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
