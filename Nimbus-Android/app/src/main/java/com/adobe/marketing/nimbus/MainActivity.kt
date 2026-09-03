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
import com.adobe.marketing.mobile.services.Log
import com.adobe.marketing.nimbus.datamodels.AppTab
import com.adobe.marketing.nimbus.repositories.AssuranceRepository
import com.adobe.marketing.nimbus.repositories.DeepLinkRepository
import com.adobe.marketing.nimbus.ui.NimbusRootScreen
import com.adobe.marketing.nimbus.ui.theme.NimbusTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var assuranceRepository: AssuranceRepository
    @Inject lateinit var deepLinkRepository: DeepLinkRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!handleAssuranceDeepLink(intent)) autoConnectAssurance()
        handleNavigationDeepLink(intent)
        setContent {
            NimbusTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NimbusRootScreen()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleAssuranceDeepLink(intent)
        handleNavigationDeepLink(intent)
    }

    private fun handleAssuranceDeepLink(intent: Intent): Boolean {
        val uri = intent.data ?: return false
        if (uri.getQueryParameter("adb_validation_sessionid") != null) {
            Assurance.startSession(uri.toString())
            assuranceRepository.recordSessionStarted(uri.toString())
            return true
        }
        return false
    }

    private fun handleNavigationDeepLink(intent: Intent) {
        val uri = intent.data
        Log.debug("Nimbus", "MainActivity", "handleNavigationDeepLink uri=$uri scheme=${uri?.scheme} host=${uri?.host}")
        if (uri == null) return
        if (uri.scheme != "nimbus") return
        val tab = when (uri.host) {
            "home" -> AppTab.HOME
            "shop" -> AppTab.SHOP
            "cart" -> AppTab.CART
            "inbox" -> AppTab.INBOX
            else -> return
        }
        Log.debug("Nimbus", "MainActivity", "requesting navigation to $tab via deepLinkRepository=$deepLinkRepository")
        deepLinkRepository.requestNavigation(tab)
    }

    private fun autoConnectAssurance() {
        if (!BuildConfig.DEBUG) return
        val url = BuildConfig.ASSURANCE_DEBUG_URL
        if (url.isBlank()) return
        Assurance.startSession(url)
        assuranceRepository.recordSessionStarted(url)
    }
}
