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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.adobe.marketing.nimbus.datamodels.Offer
import com.adobe.marketing.nimbus.datamodels.Product
import com.adobe.marketing.nimbus.datamodels.ShopCategory
import com.adobe.marketing.nimbus.datamodels.ShopUiState
import com.adobe.marketing.nimbus.utils.asPrice
import com.adobe.marketing.nimbus.utils.toOfferSurface
import com.adobe.marketing.nimbus.viewmodels.OffersViewModel
import com.adobe.marketing.nimbus.viewmodels.ShopViewModel

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
    val heroCard by remember(surface) {
        derivedStateOf { offersState.value.offersBySurface[surface]?.firstOrNull() }
    }
    val context = LocalContext.current

    OfferFetchFailureToast(offersViewModel.fetchFailed)

    PullToRefreshBox(
        isRefreshing = offersState.value.isRefreshing,
        onRefresh = { offersViewModel.refresh(surface) },
        modifier = Modifier.fillMaxSize()
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
private fun ShopHeroBanner(card: Offer, onTap: () -> Unit, onDisplayed: () -> Unit) {
    TrackedContentCard(
        card = card,
        onTap = onTap,
        onDisplayed = onDisplayed,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!card.imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = card.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = card.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

val ShopCategory.displayName: String
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
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { onSelectCategory(null) },
                label = { Text("All Products") }
            )
        }
        items(ShopCategory.entries) { category ->
            FilterChip(
                selected = selectedCategory == category,
                onClick = { onSelectCategory(category) },
                label = { Text(category.displayName) },
                leadingIcon = {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            )
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
private fun ProductTile(
    product: Product,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = product.imageUrl,
                contentDescription = product.name,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.align(Alignment.Start)
            )
            Text(
                text = product.price.asPrice(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalIconButton(
                    onClick = onDecrement,
                    enabled = quantity > 0,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = "Remove one",
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = quantity.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                FilledTonalIconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add one",
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
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
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "$count item" + if (count == 1) "" else "s",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = subtotal.asPrice(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Go to cart",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}
