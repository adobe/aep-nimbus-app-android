package com.adobe.marketing.nimbus.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adobe.marketing.nimbus.viewmodels.FlashSaleViewModel
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun FlashSaleCard(viewModel: FlashSaleViewModel = hiltViewModel()) {
    val optedIn by viewModel.optedIn.collectAsStateWithLifecycle()
    FlashSaleFailureToast(viewModel.actionFailed)
    FlashSaleCardContent(
        optedIn = optedIn,
        onOptInChanged = viewModel::setOptIn
    )
}

@Composable
private fun FlashSaleFailureToast(actionFailed: SharedFlow<Unit>) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        actionFailed.collect {
            Toast.makeText(
                context,
                "Couldn't update flash sale notifications. Check notification settings.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@Composable
fun FlashSaleCardContent(
    optedIn: Boolean,
    onOptInChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Flash sale alerts",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Get a Live Update when a flash sale starts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = optedIn, onCheckedChange = onOptInChanged)
        }
    }
}
