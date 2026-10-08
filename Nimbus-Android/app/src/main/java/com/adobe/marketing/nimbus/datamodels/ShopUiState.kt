package com.adobe.marketing.nimbus.datamodels

data class ShopUiState  (
    val products: List<Product> = emptyList(),
    val cart: Map<String, Int> = emptyMap(),
    val cartLines: List<CartLine> = emptyList(),
    val selectedCategory: ShopCategory? = null,
    val cartCount: Int = 0,
    val subtotal: Double = 0.0
)

data class CartLine(val product: Product, val quantity: Int)