package com.adobe.marketing.nimbus.ui

import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adobe.marketing.nimbus.datamodels.OrderStep
import com.adobe.marketing.nimbus.datamodels.OrderStyle
import com.adobe.marketing.nimbus.datamodels.OrderTrackingState
import com.adobe.marketing.nimbus.utils.OrderEtaFormat
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
        onEnd = viewModel::endOrder,
        onStyleSelected = viewModel::setOrderStyle
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
    onEnd: () -> Unit,
    onStyleSelected: (OrderStyle) -> Unit
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
                        text = OrderEtaFormat.text(order.step, order.etaDays),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OrderStyleDropdown(selected = order.style, onSelected = onStyleSelected)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderStyleDropdown(selected: OrderStyle, onSelected: (OrderStyle) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val metricSupported = Build.VERSION.SDK_INT >= 37
    val shown = if (selected == OrderStyle.METRIC && !metricSupported) OrderStyle.PROGRESS else selected
    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = shown.label,
            onValueChange = {},
            readOnly = true,
            label = { Text("Notification style") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            OrderStyle.entries.forEach { style ->
                val supported = style != OrderStyle.METRIC || metricSupported
                DropdownMenuItem(
                    text = { Text(if (supported) style.label else "${style.label} (Android 17+)") },
                    enabled = supported,
                    onClick = {
                        expanded = false
                        onSelected(style)
                    }
                )
            }
        }
    }
}
