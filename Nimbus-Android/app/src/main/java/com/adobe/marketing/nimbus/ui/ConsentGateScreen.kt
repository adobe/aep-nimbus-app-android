package com.adobe.marketing.nimbus.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adobe.marketing.nimbus.datamodels.ConsentState
import com.adobe.marketing.nimbus.viewmodels.ConsentViewModel

@Composable
fun ConsentGateScreen(
    viewModel: ConsentViewModel = hiltViewModel()
) {
    ConsentGateContent(
        onAccept = {viewModel.onConsentChosen(ConsentState.YES)},
        onDecline = {viewModel.onConsentChosen(ConsentState.NO)}
    )
}

@Composable
fun ConsentGateContent(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        GateIconBadge(icon = Icons.Default.Lock)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Help us personalise your experience",
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "We'd like to collect data to improve your shopping experience. You can change this anytime.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(1f))
        Button(onClick = onAccept, modifier = Modifier.fillMaxWidth()) {
            Text("Allow")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(onClick = onDecline, modifier = Modifier.fillMaxWidth()) {
            Text("Don't Allow")
        }
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Nimbus never collects data without your permission.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}