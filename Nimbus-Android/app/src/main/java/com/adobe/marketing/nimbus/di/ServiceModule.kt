package com.adobe.marketing.nimbus.di

import com.adobe.marketing.nimbus.services.AepAnalyticsService
import com.adobe.marketing.nimbus.services.AepConsentService
import com.adobe.marketing.nimbus.services.AepIdentityService
import com.adobe.marketing.nimbus.services.AepLiveUpdateService
import com.adobe.marketing.nimbus.services.AepMessagingService
import com.adobe.marketing.nimbus.services.AepPersonalizationService
import com.adobe.marketing.nimbus.services.AnalyticsService
import com.adobe.marketing.nimbus.services.AndroidNotificationService
import com.adobe.marketing.nimbus.services.BroadcastTopicService
import com.adobe.marketing.nimbus.services.ConsentService
import com.adobe.marketing.nimbus.services.FirebaseBroadcastService
import com.adobe.marketing.nimbus.services.IdentityService
import com.adobe.marketing.nimbus.services.LiveUpdateService
import com.adobe.marketing.nimbus.services.MessagingService
import com.adobe.marketing.nimbus.services.NotificationService
import com.adobe.marketing.nimbus.services.PersonalizationService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    abstract fun bindIdentityService(impl: AepIdentityService): IdentityService

    @Binds
    abstract fun bindConsentService(impl: AepConsentService): ConsentService

    @Binds
    abstract fun bindAnalyticsService(impl: AepAnalyticsService): AnalyticsService

    @Binds
    abstract fun bindMessagingService(impl: AepMessagingService): MessagingService

    @Binds
    abstract fun bindNotificationService(impl: AndroidNotificationService): NotificationService

    @Binds
    @Singleton
    abstract fun bindPersonalizationService(impl: AepPersonalizationService): PersonalizationService

    @Binds
    @Singleton
    abstract fun bindLiveUpdateService(impl: AepLiveUpdateService): LiveUpdateService

    @Binds
    @Singleton
    abstract fun bindBroadcastTopicService(impl: FirebaseBroadcastService): BroadcastTopicService
}
