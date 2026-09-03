package com.adobe.marketing.nimbus.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.adobe.marketing.nimbus.viewmodels.LoginViewModel

@Composable
fun LoginGateScreen(
    viewModel: LoginViewModel = hiltViewModel()
) {
    LoginGateContent(
        onLogin = viewModel::login,
        onContinueAsGuest = viewModel::continueAsGuest
    )
}

@Composable
private fun LoginGateContent(
    onLogin: (String) -> Unit,
    onContinueAsGuest: () -> Unit
) {
    var username by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(64.dp))
        GateIconBadge(icon = Icons.Default.Person)
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Sign in to Nimbus",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "Sign in to sync your profile across devices, or continue as a guest.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        EmailField(value = username, onValueChange = { username = it })
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = { onLogin(username) },
            enabled = username.isNotBlank(),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Log In", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(10.dp))
        OutlinedButton(
            onClick = onContinueAsGuest,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text("Continue as Guest", style = MaterialTheme.typography.titleSmall)
        }
    }
}
