package com.adobe.marketing.nimbus.data

import com.adobe.marketing.nimbus.datamodels.Product
import com.adobe.marketing.nimbus.datamodels.ShopCategory

object MockCatalog {
    val products: List<Product> = listOf(
        Product(
            id = "TEE-01",
            name = "Everyday Tee",
            price = 20.0,
            category = ShopCategory.APPAREL,
            imageUrl = "https://www.shutterstock.com/image-photo/black-blank-sport-tshirt-mockup-260nw-2667090699.jpg"
        ),
        Product(
            id = "HOOD-01",
            name = "Cozy Hoodie",
            price = 45.0,
            category = ShopCategory.APPAREL,
            imageUrl = "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcTszXVlxr_htabX2LTE7mXVy64vA8X_uhKtBg9nFPueFHhKFZBKY14va9oA&s=10"
        ),
        Product(
            id = "SHADE-01",
            name = "Sun Shades",
            price = 25.0,
            category = ShopCategory.ACCESSORIES,
            imageUrl = "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=500&auto=format&fit=crop"
        ),
        Product(
            id = "BAG-01",
            name = "Weekender Bag",
            price = 40.0,
            category = ShopCategory.ACCESSORIES,
            imageUrl = "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=500&auto=format&fit=crop"
        ),
        Product(
            id = "GIFT-01",
            name = "Gift Card",
            price = 50.0,
            category = ShopCategory.GIFTS,
            imageUrl = "https://www.adobe.com/creativecloud/business/teams/resources/how-to/media_1696fe3b83b6a54598013eacefade0998bbcc2263.png?width=750&format=png&optimize=medium"
        ),
        Product(
            id = "BOX-01",
            name = "Gift Box",
            price = 35.0,
            category = ShopCategory.GIFTS,
            imageUrl = "https://www.hatchcollection.com/cdn/shop/products/gift-box_fa97df41-ca68-49ce-8aec-2b749dfe1f81.jpg?v=1757523251&width=337"
        )
    )
}