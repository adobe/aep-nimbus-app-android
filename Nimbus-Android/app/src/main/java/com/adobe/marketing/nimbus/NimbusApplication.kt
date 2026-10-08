package com.adobe.marketing.nimbus

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.adobe.marketing.nimbus.aep.AepBootstrapper
import com.adobe.marketing.nimbus.repositories.LiveUpdateRepository
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NimbusApplication : Application() {

    @Inject lateinit var liveUpdateRepository: dagger.Lazy<LiveUpdateRepository>

    override fun onCreate() {
        super.onCreate()
        AepBootstrapper.start(this)
        // Constructs the service so its Live Update listener is registered at launch,
        // after MobileCore is set up.
        liveUpdateRepository.get()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) =
                AepBootstrapper.lifecycleStart()

            override fun onActivityPaused(activity: Activity) =
                AepBootstrapper.lifecyclePause()

            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) = Unit

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) = Unit

            override fun onActivityStarted(activity: Activity) = Unit

            override fun onActivityStopped(activity: Activity) = Unit

            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }
}