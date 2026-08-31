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
import javax.inject.Inject

@HiltViewModel
class ShopViewModel @Inject constructor(
    private val analyticsService: AnalyticsService
): ViewModel() {

    private val cart = mutableMapOf<String, Int>()
    private var selectedCategory: ShopCategory? = null

    private val _uiState = MutableStateFlow(buildUiState())
    val uiState: StateFlow<ShopUiState> = _uiState.asStateFlow()

    fun selectCategory(category: ShopCategory?) {
        if (category == selectedCategory) return
        selectedCategory = category
        _uiState.value = buildUiState()
        category?.let {
            analyticsService.trackAction("browse-category", mapOf("category" to it.name))
        }
    }

    fun increment(product: Product) {
        cart[product.id] = (cart[product.id] ?: 0) + 1
        fire(CommerceEventType.PRODUCT_LIST_ADDS, product)
        analyticsService.trackAction("add-to-cart", mapOf("sku" to product.sku))
        _uiState.value = buildUiState()
    }

    fun decrement(product: Product) {
        val qty = cart[product.id] ?: return
        if (qty == 1) cart.remove(product.id) else cart[product.id] = qty - 1
        fire(CommerceEventType.PRODUCT_LIST_REMOVES, product)
        analyticsService.trackAction("remove-from-cart", mapOf("sku" to product.sku))
        _uiState.value = buildUiState()
    }

    fun checkout() {
        if (cart.isEmpty()) return
        val subtotal = _uiState.value.subtotal
        val xdm = CommerceXdmBuilder.purchase(cart, MockCatalog.products, subtotal)

        analyticsService.track(CommerceEvent(CommerceEventType.PURCHASES, xdm))
        analyticsService.trackAction("order-complete", mapOf("orderTotal" to "%.2f".format(subtotal)))
        cart.clear()
        _uiState.value = buildUiState()
    }

    private fun fire(type: CommerceEventType, product: Product) {
        analyticsService.track(CommerceEvent(type, CommerceXdmBuilder.productEvent(type, product)))
    }

    private fun subtotal(): Double =
        MockCatalog.products.sumOf { product -> (cart[product.id] ?: 0) * product.price }

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