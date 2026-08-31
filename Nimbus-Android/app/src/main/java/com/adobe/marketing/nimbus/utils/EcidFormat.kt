package com.adobe.marketing.nimbus.utils

fun String.truncatedEcid(): String =
    if (length > 14) "${take(6)}...${takeLast(5)}" else this