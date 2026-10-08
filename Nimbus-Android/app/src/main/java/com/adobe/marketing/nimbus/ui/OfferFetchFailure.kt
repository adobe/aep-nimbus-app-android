package com.adobe.marketing.nimbus.ui

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun OfferFetchFailureToast(fetchFailed: SharedFlow<Unit>) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        fetchFailed.collect {
            Toast.makeText(context, "Couldn't load offers. Pull to refresh to try again.", Toast.LENGTH_SHORT).show()
        }
    }
}