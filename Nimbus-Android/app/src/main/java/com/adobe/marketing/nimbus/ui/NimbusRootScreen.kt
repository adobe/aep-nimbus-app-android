package com.adobe.marketing.nimbus.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    when {
        !consentState.hasChosenConsent -> ConsentGateScreen(viewModel = consentViewModel)
        loginState.isChecking -> LoadingScreen()
        !loginState.hasSeenLoginPrompt -> LoginGateScreen(viewModel = loginViewModel)
        else -> MainContent()
    }
}

@Composable
private fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun MainContent() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Nimbus")
    }
}