package com.adobe.marketing.nimbus.aep

import android.app.Application
import com.adobe.marketing.mobile.Assurance
import com.adobe.marketing.mobile.Edge
import com.adobe.marketing.mobile.Lifecycle
import com.adobe.marketing.mobile.LoggingMode
import com.adobe.marketing.mobile.MobileCore
import com.adobe.marketing.mobile.edge.consent.Consent
import com.adobe.marketing.mobile.edge.identity.Identity

object AepBootstrapper {

    fun start(application: Application) {
        MobileCore.setApplication(application)
        MobileCore.setLogLevel(LoggingMode.DEBUG)

        val extensions = listOf(
            Edge.EXTENSION,
            Identity.EXTENSION,
            Consent.EXTENSION,
            Lifecycle.EXTENSION,
            Assurance.EXTENSION
        )
        MobileCore.registerExtensions(extensions) {
            MobileCore.configureWithAppID(AepConfig.APP_ID)
        }
    }

    fun lifecycleStart() {
        MobileCore.lifecycleStart(null)
    }

    fun lifecyclePause() {
        MobileCore.lifecyclePause()
    }
}