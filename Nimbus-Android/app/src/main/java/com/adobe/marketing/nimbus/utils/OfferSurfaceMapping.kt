package com.adobe.marketing.nimbus.utils

import com.adobe.marketing.nimbus.datamodels.OfferSurface
import com.adobe.marketing.nimbus.datamodels.ShopCategory

fun ShopCategory?.toOfferSurface(): OfferSurface = when (this) {
    null -> OfferSurface.SHOP_ALL
    ShopCategory.APPAREL -> OfferSurface.SHOP_APPAREL
    ShopCategory.ACCESSORIES -> OfferSurface.SHOP_ACCESSORIES
    ShopCategory.GIFTS -> OfferSurface.SHOP_GIFTS
}