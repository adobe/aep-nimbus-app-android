package com.adobe.marketing.nimbus

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.adobe.marketing.mobile.Assurance
import com.adobe.marketing.nimbus.repositories.AssuranceRepository
import com.adobe.marketing.nimbus.ui.NimbusRootScreen
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var assuranceRepository: AssuranceRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!handleAssuranceDeepLink(intent)) autoConnectAssurance()
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NimbusRootScreen()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAssuranceDeepLink(intent)
    }

    private fun handleAssuranceDeepLink(intent: Intent): Boolean {
        val uri = intent.data ?: return false
        Assurance.startSession(uri.toString())
        assuranceRepository.recordSessionStarted(uri.toString())
        return true
    }

    private fun autoConnectAssurance() {
        if (!BuildConfig.DEBUG) return
        val url = BuildConfig.ASSURANCE_DEBUG_URL
        if (url.isBlank()) return
        Assurance.startSession(url)
        assuranceRepository.recordSessionStarted(url)
    }
}
