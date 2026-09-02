package com.adobe.marketing.nimbus.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.adobe.marketing.mobile.aepcomposeui.ImageOnlyUI
import com.adobe.marketing.mobile.aepcomposeui.LargeImageUI
import com.adobe.marketing.mobile.aepcomposeui.SmallImageUI
import com.adobe.marketing.mobile.aepcomposeui.UIEvent
import com.adobe.marketing.mobile.aepcomposeui.components.ImageOnlyCard
import com.adobe.marketing.mobile.aepcomposeui.components.LargeImageCard
import com.adobe.marketing.mobile.aepcomposeui.components.SmallImageCard
import com.adobe.marketing.mobile.aepcomposeui.observers.AepUIEventObserver
import com.adobe.marketing.mobile.aepcomposeui.style.ImageOnlyUIStyle
import com.adobe.marketing.mobile.aepcomposeui.style.LargeImageUIStyle
import com.adobe.marketing.mobile.aepcomposeui.style.SmallImageUIStyle
import com.adobe.marketing.mobile.messaging.ContentCardUIProvider
import com.adobe.marketing.mobile.messaging.Surface
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.nimbus.datamodels.Offer
import com.adobe.marketing.nimbus.datamodels.OfferSurface
import com.adobe.marketing.nimbus.datamodels.ShopCategory
import com.adobe.marketing.nimbus.viewmodels.OffersViewModel
import com.adobe.marketing.nimbus.viewmodels.ShopViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: OffersViewModel = hiltViewModel(),
    shopViewModel: ShopViewModel = hiltViewModel(),
    onNavigateToShop: () -> Unit = {}
) {
    var mode by remember { mutableStateOf(RenderMode.SDK_UI) }
    val ccUiProvider = remember { ContentCardUIProvider(Surface(OfferSurface.HOME.path)) }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OfferFetchFailureToast(viewModel.fetchFailed)

    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = { viewModel.refresh(OfferSurface.HOME) },
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            HomeHeader()
            QuickShopCategoriesRow(onCategoryTap = { category ->
                shopViewModel.selectCategory(category)
                onNavigateToShop()
            })

            Text(
                text = "Offers for you",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 4.dp)
            )
            RenderModeSelector(mode = mode, onModeSelected = { mode = it })

            Text(
                text = if (mode == RenderMode.SDK_UI) "Default (ContentCardUIProvider)" else "Custom example",
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            when (mode) {
                RenderMode.SDK_UI -> {
                    val ccUiFlow = remember(ccUiProvider) { ccUiProvider.getContentCardUIFlow() }
                    val uiResult by ccUiFlow.collectAsStateWithLifecycle(
                        initialValue = Result.success(emptyList())
                    )

                    val observer = remember {
                        object : AepUIEventObserver {
                            override fun onEvent(event: UIEvent<*, *>) {
                                when (event) {
                                    is UIEvent.Display -> Log.debug(
                                        "Nimbus",
                                        "HomeScreen",
                                        "SDK UI card displayed: ${event.aepUi.getTemplate().id}"
                                    )

                                    is UIEvent.Interact -> Log.debug(
                                        "Nimbus",
                                        "HomeScreen",
                                        "SDK UI card interacted: ${event.aepUi.getTemplate().id}"
                                    )

                                    is UIEvent.Dismiss -> Log.debug(
                                        "Nimbus",
                                        "HomeScreen",
                                        "SDK UI card dismissed: ${event.aepUi.getTemplate().id}"
                                    )
                                }
                            }
                        }
                    }

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiResult.getOrNull().orEmpty(), key = { it.getTemplate().id }) { aepUI ->
                            Box(modifier = Modifier.width(220.dp)) {
                                when (aepUI) {
                                    is SmallImageUI -> SmallImageCard(
                                        aepUI, SmallImageUIStyle.Builder().build(), observer
                                    )

                                    is LargeImageUI -> LargeImageCard(
                                        aepUI, LargeImageUIStyle.Builder().build(), observer
                                    )

                                    is ImageOnlyUI -> ImageOnlyCard(
                                        aepUI, ImageOnlyUIStyle.Builder().build(), observer
                                    )
                                }
                            }
                        }
                    }
                }

                RenderMode.CUSTOM -> {
                    LaunchedEffect(Unit) {
                        viewModel.ensureLoaded(OfferSurface.HOME)
                    }
                    val cards by remember {
                        derivedStateOf {
                            uiState.offersBySurface[OfferSurface.HOME].orEmpty()
                        }
                    }
                    val context = LocalContext.current

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
        }
    }
}

@Composable
private fun HomeContent(
    cards: List<Offer>, onCardTap: (Offer) -> Unit, onCardDisplayed: (Offer) -> Unit
) {
    Column(
        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
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
                        onDisplayed = { onCardDisplayed(card) })
                }
            }
        }
    }
}

@Composable
private fun ContentCardTile(
    card: Offer, onTap: () -> Unit, onDisplayed: () -> Unit
) {
    TrackedContentCard(
        card = card, onTap = onTap, onDisplayed = onDisplayed, modifier = Modifier.width(220.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentScale = ContentScale.Crop
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

@Composable
private fun HomeHeader() {
    Text(
        text = "Welcome to Nimbus",
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun QuickShopCategoriesRow(onCategoryTap: (ShopCategory) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(ShopCategory.entries) { category ->
            Card(onClick = { onCategoryTap(category) }) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Text(
                        text = category.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}
