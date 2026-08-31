package com.adobe.marketing.nimbus.utils

import com.adobe.marketing.nimbus.datamodels.ContentSurface
import com.adobe.marketing.nimbus.datamodels.ShopCategory

fun ShopCategory?.toContentSurface(): ContentSurface = when (this) {
    null -> ContentSurface.SHOP_ALL
    ShopCategory.APPAREL -> ContentSurface.SHOP_APPAREL
    ShopCategory.ACCESSORIES -> ContentSurface.SHOP_ACCESSORIES
    ShopCategory.GIFTS -> ContentSurface.SHOP_GIFTS
}