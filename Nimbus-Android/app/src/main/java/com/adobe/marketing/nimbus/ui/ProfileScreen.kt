package com.adobe.marketing.nimbus.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adobe.marketing.nimbus.datamodels.ConsentState
import com.adobe.marketing.nimbus.viewmodels.ProfileViewModel
import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.adobe.marketing.nimbus.datamodels.ProfileUiState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import com.adobe.marketing.nimbus.services.NotificationEnableAction
import com.adobe.marketing.nimbus.utils.truncatedEcid

@Composable
fun ProfileScreen(viewModel: ProfileViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        viewModel.refreshPushState()
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.refreshPushState()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    ProfileContent(uiState = uiState, onConsentToggle = { enabled ->
        viewModel.setConsent(if (enabled) ConsentState.YES else
            ConsentState.NO)
    }, onLogin = viewModel::login, onLogout = viewModel::logout,
        onEnableNotifications = {
            when (viewModel.notificationEnableAction()) {
                NotificationEnableAction.REQUEST_PERMISSION ->

                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                NotificationEnableAction.OPEN_SETTINGS ->
                    context.startActivity(
                        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply
                        {
                            putExtra(Settings.EXTRA_APP_PACKAGE,
                                context.packageName)
                        }
                    )
            }
        })
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onConsentToggle: (Boolean) -> Unit,
    onLogin: (String) -> Unit,
    onLogout: () -> Unit,
    onEnableNotifications: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SectionHeader(icon = Icons.Default.Person, title = "Identity")
        ListItem(headlineContent = { Text("ECID") }, supportingContent = {
            Text(uiState.ecid?.truncatedEcid() ?: "unknown")
        }, trailingContent = {
            IconButton(onClick = {
                uiState.ecid?.let {
                    clipboardManager.setText(AnnotatedString(it))
                }
            }) {
                Icon(
                    Icons.Default.ContentCopy, contentDescription = "Copy ECID "
                )
            }
        })
        if (uiState.signedInUser != null) {
            ListItem(
                headlineContent = { Text("Linked account") },
                supportingContent = { Text(uiState.signedInUser) })
            TextButton(onClick = onLogout) { Text("Log out") }
        } else {
            var username by remember { mutableStateOf("") }
            EmailField(value = username, onValueChange = { username = it })
            TextButton(
                onClick = { if (username.isNotBlank()) onLogin(username) }) { Text("Log in") }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

        SectionHeader(icon = Icons.Default.PrivacyTip, title = "Consent")
        ListItem(headlineContent = { Text("Allow data collection") }, trailingContent = {
            Switch(
                checked = uiState.consentState == ConsentState.YES,
                onCheckedChange = onConsentToggle
            )
        })

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))
        SectionHeader(icon = Icons.Default.Notifications, title = "Notifications")
        ListItem(headlineContent = { Text("Push notifications") }, supportingContent = {
            Text(
                if (uiState.pushEnabled) "Enabled" else "Not enabled"
            )
        }, trailingContent = {
            if (!uiState.pushEnabled) {
                TextButton(onClick = onEnableNotifications) { Text("Enable") }
            }
        })
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(title, style = MaterialTheme.typography.titleMedium)
    }
}