package com.adobe.marketing.nimbus.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adobe.marketing.nimbus.datamodels.OrderStep
import com.adobe.marketing.nimbus.datamodels.OrderTrackingState
import com.adobe.marketing.nimbus.viewmodels.LiveUpdateViewModel
import kotlinx.coroutines.flow.SharedFlow

@Composable
fun LiveUpdateCard(viewModel: LiveUpdateViewModel = hiltViewModel()) {
    val order by viewModel.orderState.collectAsStateWithLifecycle()
    LiveUpdateFailureToast(viewModel.actionFailed)
    LiveUpdateCardContent(
        order = order,
        onStart = viewModel::startOrder,
        onAdvance = viewModel::advanceOrder,
        onEnd = viewModel::endOrder
    )
}

@Composable
private fun LiveUpdateFailureToast(actionFailed: SharedFlow<Unit>) {
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        actionFailed.collect {
            Toast.makeText(
                context,
                "Couldn't update the live notification. Check notification settings.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@Composable
fun LiveUpdateCardContent(
    order: OrderTrackingState?,
    onStart: () -> Unit,
    onAdvance: () -> Unit,
    onEnd: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Order tracking",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            if (order == null) {
                Text(
                    text = "Start a demo order to see a Live Update in the status bar.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(onClick = onStart) { Text("Start demo order") }
            } else {
                Text(
                    text = "Order #${order.orderNumber}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                LinearProgressIndicator(
                    progress = { order.step.ordinal / (OrderStep.entries.size - 1f) },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = order.step.label,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (!order.step.isTerminal) {
                    Text(
                        text = if (order.etaDays == 1) "Arriving in 1 day" else "Arriving in ${order.etaDays} days",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onAdvance, enabled = !order.step.isTerminal) {
                        Text("Advance step")
                    }
                    OutlinedButton(onClick = onEnd) { Text("End") }
                }
            }
        }
    }
}
