package com.adobe.marketing.nimbus.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adobe.marketing.nimbus.datamodels.Product
import com.adobe.marketing.nimbus.datamodels.ShopCategory
import com.adobe.marketing.nimbus.datamodels.ShopUiState
import com.adobe.marketing.nimbus.utils.asPrice
import com.adobe.marketing.nimbus.viewmodels.ShopViewModel
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.core.net.toUri
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage
import com.adobe.marketing.nimbus.datamodels.Offer
import com.adobe.marketing.nimbus.utils.toOfferSurface
import com.adobe.marketing.nimbus.viewmodels.OffersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    onProceedToCart: () -> Unit = {},
    viewModel: ShopViewModel = hiltViewModel(),
    offersViewModel: OffersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val surface = uiState.selectedCategory.toOfferSurface()

    LaunchedEffect(surface) {
        offersViewModel.ensureLoaded(surface)
    }
    val offersState = offersViewModel.uiState.collectAsStateWithLifecycle()
    val heroCard by remember(surface) { derivedStateOf { offersState.value.offersBySurface[surface]?.firstOrNull()} }
    val context = LocalContext.current

    OfferFetchFailureToast(offersViewModel.fetchFailed)

    PullToRefreshBox(
        isRefreshing = offersState.value.isRefreshing,
        onRefresh = { offersViewModel.refresh(surface) }
    ) {
        ShopContent(
            uiState = uiState,
            heroCard = heroCard,
            onHeroCardTap = { card ->
                offersViewModel.onCardInteracted(card.id)
                card.actionUrl?.let { url ->
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
                    } catch (_: ActivityNotFoundException) {
                        // No app can handle this URL - ignore.
                    }
                }
            },
            onHeroCardDisplayed = { offersViewModel.onCardDisplayed(it.id) },
            onSelectCategory = viewModel::selectCategory,
            onIncrement = viewModel::increment,
            onDecrement = viewModel::decrement,
            onProceedToCart = onProceedToCart
        )
    }
}

@Composable
private fun ShopContent(
    uiState: ShopUiState,
    heroCard: Offer?,
    onHeroCardTap: (Offer) -> Unit,
    onHeroCardDisplayed: (Offer) -> Unit,
    onSelectCategory: (ShopCategory?) -> Unit,
    onIncrement: (Product) -> Unit,
    onDecrement: (Product) -> Unit,
    onProceedToCart: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        heroCard?.let { card ->
            ShopHeroBanner(
                card = card,
                onTap = { onHeroCardTap(card) },
                onDisplayed = { onHeroCardDisplayed(card) }
            )
        }
        CategoryFilterRow(
            selectedCategory = uiState.selectedCategory,
            onSelectCategory = onSelectCategory
        )
        ProductGrid(
            modifier = Modifier.weight(1f),
            products = uiState.products,
            cart = uiState.cart,
            onIncrement = onIncrement,
            onDecrement = onDecrement
        )
        if (uiState.cartCount > 0) {
            ShopCartBar(
                count = uiState.cartCount,
                subtotal = uiState.subtotal,
                onProceedToCart = onProceedToCart
            )
        }
    }
}

@Composable
private fun ShopHeroBanner(card: Offer, onTap: () -> Unit, onDisplayed: () ->
Unit) {
    TrackedContentCard(
        card = card,
        onTap = onTap,
        onDisplayed = onDisplayed,
        modifier = Modifier.fillMaxWidth().padding(16.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment =
            Alignment.CenterVertically) {
            AsyncImage(
                model = card.imageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(card.title, style = MaterialTheme.typography.titleMedium)
                Text(card.body, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun ProductGrid(
    modifier: Modifier = Modifier,
    products: List<Product>,
    cart: Map<String, Int>,
    onIncrement: (Product) -> Unit,
    onDecrement: (Product) -> Unit
) {
    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(products) { product ->
            ProductTile(
                product = product,
                quantity = cart[product.id] ?: 0,
                onIncrement = { onIncrement(product) },
                onDecrement = { onDecrement(product) }
            )
        }
    }
}

@Composable
private fun ShopCartBar(
    count: Int,
    subtotal: Double,
    onProceedToCart: () -> Unit
) {
    Surface(
        onClick = onProceedToCart,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal =
                16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint =
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "$count item" + if (count == 1) "" else
                        "s",
                    style = MaterialTheme.typography.titleSmall,
                    color =
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = subtotal.asPrice(),
                    style = MaterialTheme.typography.titleSmall,
                    color =
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Go to cart",
                    tint =
                        MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

private val ShopCategory.displayName: String
    get() = name.lowercase().replaceFirstChar { it.uppercase() }

val ShopCategory.icon: ImageVector
    get() = when (this) {
        ShopCategory.APPAREL -> Icons.Default.Checkroom
        ShopCategory.ACCESSORIES -> Icons.Default.ShoppingBag
        ShopCategory.GIFTS -> Icons.Default.CardGiftcard
    }

@Composable
private fun CategoryFilterRow(
    selectedCategory: ShopCategory?,
    onSelectCategory: (ShopCategory?) -> Unit
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onSelectCategory(null) },
                label = { Text("All") }
            )
        }
        items(ShopCategory.entries) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onSelectCategory(category) },
                label = { Text(category.displayName) }
            )
        }
    }
}

@Composable
private fun ProductTile(
    product: Product,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    Card(modifier = Modifier.padding(4.dp)) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(
                imageVector = product.category.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = product.name, style = MaterialTheme.typography.titleSmall)
            Text(
                text = product.price.asPrice(),
                style = MaterialTheme.typography.bodyMedium
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxSize()
            ) {
                IconButton(onClick = onDecrement, enabled = quantity > 0) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Remove one")
                }
                Text(text = quantity.toString(), style = MaterialTheme.typography.bodyLarge)
                IconButton(onClick = onIncrement) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add one")
                }
            }
        }
    }
}
