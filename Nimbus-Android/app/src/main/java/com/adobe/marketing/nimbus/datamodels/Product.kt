package com.adobe.marketing.nimbus.datamodels

data class Product (
    val id: String,
    val name: String,
    val price: Double,
    val category: ShopCategory
){
    val sku: String get() = id
}