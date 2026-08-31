package com.adobe.marketing.nimbus.data

import com.adobe.marketing.nimbus.datamodels.Product
import com.adobe.marketing.nimbus.datamodels.ShopCategory

object MockCatalog {
    val products: List<Product> = listOf(
        Product(id = "TEE-01", name = "Everyday Tee", price = 20.0, category = ShopCategory.APPAREL),
        Product(id = "HOOD-01", name = "Cozy Hoodie", price = 45.0, category = ShopCategory.APPAREL),
        Product(id = "SHADE-01", name = "Sun Shades", price = 25.0, category = ShopCategory.ACCESSORIES),
        Product(id = "BAG-01", name = "Weekender Bag", price = 40.0, category = ShopCategory.ACCESSORIES),
        Product(id = "GIFT-01", name = "Gift Card", price = 50.0, category = ShopCategory.GIFTS),
        Product(id = "BOX-01", name = "Gift Box", price = 35.0, category = ShopCategory.GIFTS)
    )
}