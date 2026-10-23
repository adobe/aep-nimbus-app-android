package com.adobe.marketing.nimbus.utils

import java.util.Locale

fun Double.asPrice(): String = "$" + "%.2f".format(Locale.US,this)