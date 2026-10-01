package com.adobe.marketing.nimbus.ui

import android.Manifest
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Loyalty
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adobe.marketing.nimbus.datamodels.ConsentState
import com.adobe.marketing.nimbus.datamodels.PersonalizedOffer
import com.adobe.marketing.nimbus.datamodels.ProfileUiState
import com.adobe.marketing.nimbus.services.NotificationEnableAction
import com.adobe.marketing.nimbus.utils.toIdentityMapJson
import com.adobe.marketing.nimbus.utils.truncatedEcid
import com.adobe.marketing.nimbus.viewmodels.ProfileViewModel

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

    ProfileContent(
        uiState = uiState,
        onConsentToggle = { enabled ->
            viewModel.setConsent(if (enabled) ConsentState.YES else ConsentState.NO)
        },
        onLogin = viewModel::login,
        onLogout = viewModel::logout,
        onEnableNotifications = {
            when (viewModel.notificationEnableAction()) {
                NotificationEnableAction.REQUEST_PERMISSION -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                NotificationEnableAction.OPEN_SETTINGS -> context.startActivity(
                    Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                        putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                    }
                )
            }
        },
        onConnectAssurance = viewModel::connectAssurance,
        onDecisionScopeChange = viewModel::setDecisionScopeName,
        onTargetActivityChange = viewModel::setTargetActivityName,
        onFetchOffers = viewModel::fetchPersonalizedOffers,
        onOfferDisplayed = viewModel::onOfferDisplayed,
        onOfferTapped = viewModel::onOfferTapped
    )
}

@Composable
private fun ProfileContent(
    uiState: ProfileUiState,
    onConsentToggle: (Boolean) -> Unit,
    onLogin: (String) -> Unit,
    onLogout: () -> Unit,
    onEnableNotifications: () -> Unit,
    onConnectAssurance: (String) -> Unit,
    onDecisionScopeChange: (String) -> Unit,
    onTargetActivityChange: (String) -> Unit,
    onFetchOffers: () -> Unit,
    onOfferDisplayed: (String) -> Unit,
    onOfferTapped: (String) -> Unit
) {
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = "Profile & Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Identity Section
        SectionCard(icon = Icons.Default.Person, title = "Identity") {
            ListItem(
                headlineContent = { Text("ECID", fontWeight = FontWeight.SemiBold) },
                supportingContent = {
                    Text(
                        uiState.ecid?.truncatedEcid() ?: "Unknown",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingContent = {
                    IconButton(onClick = {
                        uiState.ecid?.let {
                            clipboardManager.setText(AnnotatedString(it))
                        }
                    }) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy ECID")
                    }
                }
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            if (uiState.signedInUser != null) {
                var showLogoutConfirm by remember { mutableStateOf(false) }

                ListItem(
                    headlineContent = { Text("Linked Account", fontWeight = FontWeight.SemiBold) },
                    supportingContent = { Text(uiState.signedInUser) },
                    trailingContent = {
                        OutlinedButton(onClick = { showLogoutConfirm = true }) {
                            Text("Log Out")
                        }
                    }
                )

                if (showLogoutConfirm) {
                    AlertDialog(
                        onDismissRequest = { showLogoutConfirm = false },
                        title = { Text("Log out?") },
                        text = { Text("You'll be signed out of ${uiState.signedInUser} on this device.") },
                        confirmButton = {
                            TextButton(onClick = {
                                showLogoutConfirm = false
                                onLogout()
                            }) {
                                Text("Log Out")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showLogoutConfirm = false }) {
                                Text("Cancel")
                            }
                        }
                    )
                }
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    var username by remember { mutableStateOf("") }
                    var showLoginConfirm by remember { mutableStateOf(false) }

                    EmailField(value = username, onValueChange = { username = it })
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { if (username.isNotBlank()) showLoginConfirm = true },
                        enabled = username.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Log In")
                    }

                    if (showLoginConfirm) {
                        AlertDialog(
                            onDismissRequest = { showLoginConfirm = false },
                            title = { Text("Log in as $username?") },
                            text = { Text("You'll be linked to this account for the rest of your session.") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showLoginConfirm = false
                                    onLogin(username)
                                }) {
                                    Text("Log In")
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { showLoginConfirm = false }) {
                                    Text("Cancel")
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Identity Map Section
        SectionCard(icon = Icons.Default.Badge, title = "Identity Map") {
            Column(modifier = Modifier.padding(16.dp)) {
                if (uiState.identityMap.isEmpty()) {
                    Text(
                        text = "No identities available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val identityMapJson = remember(uiState.identityMap) {
                        uiState.identityMap.toIdentityMapJson()
                    }
                    OutlinedTextField(
                        value = identityMapJson,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("identityMap JSON") },
                        trailingIcon = {
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(identityMapJson))
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy identity map JSON")
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .verticalScroll(rememberScrollState())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Consent Section
        SectionCard(icon = Icons.Default.PrivacyTip, title = "Consent & Privacy") {
            ListItem(
                headlineContent = { Text("Allow Data Collection", fontWeight = FontWeight.SemiBold) },
                supportingContent = {
                    Text(
                        "Collect analytics to personalize experience",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingContent = {
                    Switch(
                        checked = uiState.consentState == ConsentState.YES,
                        onCheckedChange = onConsentToggle
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications Section
        SectionCard(icon = Icons.Default.Notifications, title = "Notifications") {
            ListItem(
                headlineContent = { Text("Push Notifications", fontWeight = FontWeight.SemiBold) },
                supportingContent = {
                    Text(
                        if (uiState.pushEnabled) "Enabled" else "Disabled",
                        color = if (uiState.pushEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingContent = {
                    if (!uiState.pushEnabled) {
                        Button(onClick = onEnableNotifications) {
                            Text("Enable")
                        }
                    }
                }
            )

            val pushToken = uiState.pushToken
            if (pushToken != null) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Column(modifier = Modifier.padding(16.dp)) {
                    OutlinedTextField(
                        value = pushToken,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Push Token (FCM)") },
                        trailingIcon = {
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(pushToken))
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy push token")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Assurance Section
        SectionCard(icon = Icons.Default.Science, title = "Adobe Assurance") {
            Column(modifier = Modifier.padding(16.dp)) {
                if (uiState.assuranceSessionUrl != null) {
                    OutlinedTextField(
                        value = uiState.assuranceSessionUrl,
                        onValueChange = {},
                        enabled = false,
                        label = { Text("Active Session URL") },
                        trailingIcon = {
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(uiState.assuranceSessionUrl))
                            }) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy session URL")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    var assuranceUrl by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = assuranceUrl,
                        onValueChange = { assuranceUrl = it },
                        label = { Text("Enter Assurance Session URL") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = { if (assuranceUrl.isNotBlank()) onConnectAssurance(assuranceUrl) },
                        enabled = assuranceUrl.isNotBlank(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Connect Session")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Profile Attributes Section
        SectionCard(icon = Icons.Default.Loyalty, title = "Profile") {
            ListItem(
                headlineContent = { Text("Loyalty Tier", fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text(uiState.profileAttributes.loyaltyTier) }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ListItem(
                headlineContent = { Text("Member Since", fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text(uiState.profileAttributes.memberSince) }
            )
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            ListItem(
                headlineContent = { Text("Lifetime Purchases", fontWeight = FontWeight.SemiBold) },
                supportingContent = { Text(uiState.profileAttributes.lifetimePurchases.toString()) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Personalized Offers Section
        SectionCard(icon = Icons.Default.AutoAwesome, title = "Personalized Offers") {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = uiState.decisionScopeName,
                    onValueChange = onDecisionScopeChange,
                    label = { Text("Decision Scope") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = uiState.targetActivityName,
                    onValueChange = onTargetActivityChange,
                    label = { Text("Target Activity") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = onFetchOffers,
                    enabled = !uiState.isFetchingOffers &&
                        (uiState.decisionScopeName.isNotBlank() || uiState.targetActivityName.isNotBlank()),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isFetchingOffers) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Fetch Offers")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Personalized | Offer Decisioning",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OfferGroup(
                    offers = uiState.personalizedOffers.offerDecisioning,
                    onOfferDisplayed = onOfferDisplayed,
                    onOfferTapped = onOfferTapped
                )

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Personalized | Adobe Target",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OfferGroup(
                    offers = uiState.personalizedOffers.target,
                    onOfferDisplayed = onOfferDisplayed,
                    onOfferTapped = onOfferTapped
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun OfferGroup(
    offers: List<PersonalizedOffer>,
    onOfferDisplayed: (String) -> Unit,
    onOfferTapped: (String) -> Unit
) {
    if (offers.isEmpty()) {
        Text(
            "No offers to show",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
        return
    }

    PersonalizedOfferRow(
        offers = offers,
        onDisplayed = onOfferDisplayed,
        onTapped = onOfferTapped,
        contentPadding = PaddingValues(top = 4.dp)
    )
}

@Composable
private fun SectionCard(
    icon: ImageVector,
    title: String,
    content: @Composable () -> Unit
) {
    ElevatedCard(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            content()
        }
    }
}
