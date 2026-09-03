package com.adobe.marketing.nimbus.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adobe.marketing.nimbus.datamodels.AppTab
import com.adobe.marketing.nimbus.viewmodels.MainTabViewModel
import com.adobe.marketing.nimbus.viewmodels.ShopViewModel

private data class TabSpec(
    val tab: AppTab,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

private val tabSpecs = listOf(
    TabSpec(AppTab.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    TabSpec(AppTab.SHOP, "Shop", Icons.Filled.Storefront, Icons.Outlined.Storefront),
    TabSpec(AppTab.CART, "Cart", Icons.Filled.ShoppingCart, Icons.Outlined.ShoppingCart),
    TabSpec(AppTab.INBOX, "Inbox", Icons.Filled.Inbox, Icons.Outlined.Inbox),
    TabSpec(AppTab.PROFILE, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun NimbusMainScaffold(
    viewModel: MainTabViewModel = hiltViewModel(),
    shopViewModel: ShopViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val shopUiState by shopViewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabSpecs.forEach { spec ->
                    val selected = selectedTab == spec.tab
                    NavigationBarItem(
                        selected = selected,
                        onClick = { viewModel.selectTab(spec.tab) },
                        icon = {
                            if (spec.tab == AppTab.CART && shopUiState.cartCount > 0) {
                                BadgedBox(
                                    badge = {
                                        Badge {
                                            Text(text = shopUiState.cartCount.toString())
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = if (selected) spec.selectedIcon else spec.unselectedIcon,
                                        contentDescription = spec.label
                                    )
                                }
                            } else {
                                Icon(
                                    imageVector = if (selected) spec.selectedIcon else spec.unselectedIcon,
                                    contentDescription = spec.label
                                )
                            }
                        },
                        label = {
                            Text(
                                text = spec.label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = selectedTab,
                transitionSpec = {
                    val initialIndex = initialState.ordinal
                    val targetIndex = targetState.ordinal
                    if (targetIndex > initialIndex) {
                        (slideInHorizontally { width -> width / 4 } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> -width / 4 } + fadeOut())
                    } else {
                        (slideInHorizontally { width -> -width / 4 } + fadeIn()) togetherWith
                                (slideOutHorizontally { width -> width / 4 } + fadeOut())
                    }
                },
                label = "TabTransition"
            ) { targetTab ->
                when (targetTab) {
                    AppTab.HOME -> HomeScreen(
                        onNavigateToShop = { viewModel.selectTab(AppTab.SHOP) },
                        shopViewModel = shopViewModel
                    )
                    AppTab.SHOP -> ShopScreen(
                        onProceedToCart = { viewModel.selectTab(AppTab.CART) },
                        viewModel = shopViewModel
                    )
                    AppTab.CART -> CartScreen(
                        onShopNow = { viewModel.selectTab(AppTab.SHOP) },
                        viewModel = shopViewModel
                    )
                    AppTab.INBOX -> InboxScreen()
                    AppTab.PROFILE -> ProfileScreen()
                }
            }
        }
    }
}
