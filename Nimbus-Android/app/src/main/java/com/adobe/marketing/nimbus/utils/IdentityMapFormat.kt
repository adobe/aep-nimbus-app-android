package com.adobe.marketing.nimbus.utils

import com.adobe.marketing.nimbus.datamodels.IdentityMapEntry
import org.json.JSONArray
import org.json.JSONObject

/** Formats a flat list of identity entries back into the SDK's nested identityMap JSON shape. */
fun List<IdentityMapEntry>.toIdentityMapJson(): String {
    val namespaces = JSONObject()
    groupBy { it.namespace }.forEach { (namespace, entries) ->
        val items = JSONArray()
        entries.forEach { entry ->
            items.put(
                JSONObject().apply {
                    put("id", entry.id)
                    put("authenticatedState", entry.authenticatedState.lowercase())
                    put("primary", entry.isPrimary)
                }
            )
        }
        namespaces.put(namespace, items)
    }
    return JSONObject().apply { put("identityMap", namespaces) }.toString(2)
}
