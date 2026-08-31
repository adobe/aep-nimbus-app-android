package com.adobe.marketing.nimbus.utils

import com.adobe.marketing.nimbus.datamodels.CommerceEventType
import com.adobe.marketing.nimbus.datamodels.Product

object CommerceXdmBuilder {

    fun productEvent(type: CommerceEventType, product: Product): Map<String, Any> {
        val key = when (type) {
            CommerceEventType.PRODUCT_LIST_ADDS -> "productListAdds"
            CommerceEventType.PRODUCT_LIST_REMOVES -> "productListRemoves"
            CommerceEventType.PURCHASES -> "purchases"
        }
        return mapOf(
            "eventType" to "commerce.$key",
            "commerce" to mapOf(key to mapOf("value" to 1)),
            "productListItems" to listOf(
                mapOf(
                    "SKU" to product.sku,
                    "name" to product.name,
                    "quantity" to 1,
                    "priceTotal" to product.price
                )
            )
        )
    }

    fun purchase(cart: Map<String, Int>, catalog: List<Product>, subtotal: Double): Map<String, Any> {
        val items = catalog.mapNotNull { product ->
            val qty = cart[product.id]
            if (qty == null || qty <= 0) return@mapNotNull null
            mapOf(
                "SKU" to product.sku,
                "name" to product.name,
                "quantity" to qty,
                "priceTotal" to product.price * qty
            )
        }
        return mapOf(
            "eventType" to "commerce.purchases",
            "commerce" to mapOf(
                "purchases" to mapOf("value" to 1),
                "order" to mapOf("priceTotal" to subtotal, "currencyCode" to "USD")
            ),
            "productListItems" to items
        )
    }
}