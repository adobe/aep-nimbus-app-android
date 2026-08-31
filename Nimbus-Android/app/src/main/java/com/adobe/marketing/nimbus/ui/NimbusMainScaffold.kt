package com.adobe.marketing.nimbus.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adobe.marketing.nimbus.datamodels.AppTab
import com.adobe.marketing.nimbus.viewmodels.MainTabViewModel

private data class TabSpec(val tab: AppTab, val label: String, val icon: ImageVector)

private val tabSpecs = listOf(
    TabSpec(AppTab.HOME, "Home", Icons.Default.Home),
    TabSpec(AppTab.SHOP, "Shop", Icons.Default.Storefront),
    TabSpec(AppTab.CART, "Cart", Icons.Default.ShoppingCart),
    TabSpec(AppTab.INBOX, "Inbox", Icons.Default.Inbox),
    TabSpec(AppTab.PROFILE, "Profile", Icons.Default.PersonOutline)
)

@Composable
fun NimbusMainScaffold(
    viewModel: MainTabViewModel = hiltViewModel()
) {
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()

    Scaffold(
        bottomBar = {
            NavigationBar {
                tabSpecs.forEach { spec ->
                    NavigationBarItem(
                        selected = selectedTab == spec.tab,
                        onClick = { viewModel.selectTab(spec.tab) },
                        icon = { Icon(imageVector = spec.icon, contentDescription = spec.label) },
                        label = { Text(spec.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            when (selectedTab) {
                AppTab.HOME -> HomeScreen()
                AppTab.SHOP -> ShopScreen(onProceedToCart = {
                    viewModel.selectTab(AppTab.CART)
                })
                AppTab.CART -> CartScreen(onShopNow = { viewModel.selectTab(AppTab.SHOP) })
                AppTab.INBOX -> InboxScreen()
                AppTab.PROFILE -> ProfileScreen()
            }
        }
    }
}