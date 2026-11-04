package com.adobe.marketing.nimbus.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adobe.marketing.nimbus.viewmodels.ConsentViewModel
import com.adobe.marketing.nimbus.viewmodels.LoginViewModel

@Composable
fun NimbusRootScreen (
    consentViewModel: ConsentViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val consentState by consentViewModel.uiState.collectAsStateWithLifecycle()
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()

    RequestNotificationPermissionOnLaunch()

    when {
        consentState.isLoading || loginState.isChecking -> LoadingScreen()
        !consentState.hasChosenConsent -> ConsentGateScreen(viewModel = consentViewModel)
        !loginState.hasSeenLoginPrompt -> LoginGateScreen(viewModel = loginViewModel)
        else -> NimbusMainScaffold()
    }
}

/**
 * Requests the POST_NOTIFICATIONS runtime permission once at app launch.
 *
 * Only applies on Android 13 (TIRAMISU) and above, where notifications require a
 * runtime grant; on older versions notifications are enabled by default and no
 * request is needed. If the permission is already granted (or the user has
 * permanently denied it), the system returns immediately without showing a dialog.
 */
@Composable
private fun RequestNotificationPermissionOnLaunch() {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return

    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Result is not acted on here; the FCM fallback guards on the permission. */ }

    LaunchedEffect(Unit) {
        val alreadyGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED

        if (!alreadyGranted) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
