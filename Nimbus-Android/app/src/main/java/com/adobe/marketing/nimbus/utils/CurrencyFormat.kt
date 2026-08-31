package com.adobe.marketing.nimbus.utils

fun Double.asPrice(): String = "$" + "%.2f".format(this)