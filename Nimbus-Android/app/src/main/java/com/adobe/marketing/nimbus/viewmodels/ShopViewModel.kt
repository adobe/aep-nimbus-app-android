package com.adobe.marketing.nimbus.viewmodels

import androidx.lifecycle.ViewModel
import com.adobe.marketing.nimbus.data.MockCatalog
import com.adobe.marketing.nimbus.datamodels.CartLine
import com.adobe.marketing.nimbus.datamodels.CommerceEvent
import com.adobe.marketing.nimbus.datamodels.CommerceEventType
import com.adobe.marketing.nimbus.datamodels.Product
import com.adobe.marketing.nimbus.datamodels.ShopCategory
import com.adobe.marketing.nimbus.datamodels.ShopUiState
import com.adobe.marketing.nimbus.services.AnalyticsService
import com.adobe.marketing.nimbus.utils.CommerceXdmBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * Powered by Edge's XDM commerce events (via [AnalyticsService.track] and [CommerceXdmBuilder])
 * plus the AJO Messaging extension's Code-Based-Experience banners per shop category surface.
 * This is the capability behind every commerce action in the app: browsing a category, adding/
 * removing cart items, and checkout each fire a distinct `commerce.*` XDM event, while
 * category-specific CBE content renders above the product grid. Shared by Shop and Cart since
 * both are views onto the same cart state, not genuinely independent features.
 */
@HiltViewModel
class ShopViewModel @Inject constructor(
    private val analyticsService: AnalyticsService
): ViewModel() {

    private val cart = mutableMapOf<String, Int>()
    private var selectedCategory: ShopCategory? = null

    private val _uiState = MutableStateFlow(buildUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    /** Switches the active category filter and fires a browse-category trackAction. */
    fun selectCategory(category: ShopCategory?) {
        if (category == selectedCategory) return
        selectedCategory = category
        _uiState.update { buildUiState() }
        category?.let {
            analyticsService.trackAction("browse-category", mapOf("category" to it.name))
        }
    }

    /** Adds one unit of a product: fires a productListAdds XDM event plus an add-to-cart action. */
    fun increment(product: Product) {
        cart[product.id] = (cart[product.id] ?: 0) + 1
        fire(CommerceEventType.PRODUCT_LIST_ADDS, product)
        analyticsService.trackAction("add-to-cart", mapOf("sku" to product.sku, "cartSubtotal" to "%.2f".format(subtotal())))
        _uiState.update { buildUiState() }
    }

    /** Removes one unit of a product: fires a productListRemoves XDM event, no trackAction. */
    fun decrement(product: Product) {
        val qty = cart[product.id] ?: return
        if (qty == 1) cart.remove(product.id) else cart[product.id] = qty - 1
        fire(CommerceEventType.PRODUCT_LIST_REMOVES, product)
        analyticsService.trackAction("remove-from-cart", mapOf("sku" to product.sku))
        _uiState.update { buildUiState() }
    }

    /** Fires one purchases XDM event for the whole cart plus an order-complete action, then clears it. */
    fun checkout() {
        if (cart.isEmpty()) return
        val subtotal = _uiState.value.subtotal
        val xdm = CommerceXdmBuilder.purchase(cart, MockCatalog.products, subtotal)

        analyticsService.track(CommerceEvent(CommerceEventType.PURCHASES, xdm))
        analyticsService.trackAction("order-complete", mapOf("orderTotal" to "%.2f".format(subtotal)))
        cart.clear()
        _uiState.update { buildUiState() }
    }

    /** Builds and sends a single-product commerce XDM event of the given type. */
    private fun fire(type: CommerceEventType, product: Product) {
        analyticsService.track(CommerceEvent(type, CommerceXdmBuilder.productEvent(type, product)))
    }

    /** Computes the current cart subtotal from quantities and catalog prices. */
    private fun subtotal(): Double =
        MockCatalog.products.sumOf { product -> (cart[product.id] ?: 0) * product.price }

    /** Recomputes the full UI state snapshot from the current cart and category filter. */
    private fun buildUiState(): ShopUiState {
        val products = selectedCategory?.let { category ->
            MockCatalog.products.filter { it.category == category }
        } ?: MockCatalog.products
        val cartLines = mutableListOf<CartLine>()
        var subtotal = 0.0
        for (product in MockCatalog.products) {
            val qty = cart[product.id] ?: continue
            if (qty <= 0) continue
            cartLines += CartLine(product, qty)
            subtotal += product.price * qty
        }

        return ShopUiState(
            products = products,
            cart = cart.toMap(),
            cartLines = cartLines,
            selectedCategory = selectedCategory,
            cartCount = cart.values.sum(),
            subtotal = subtotal
        )
    }
}