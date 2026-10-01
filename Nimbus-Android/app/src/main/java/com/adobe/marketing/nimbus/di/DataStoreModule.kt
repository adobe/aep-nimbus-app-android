package com.adobe.marketing.nimbus.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.consentDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "consent_preferences"
)

private val Context.optimizeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "optimize_preferences"
)

private val Context.loginDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "login_preferences"
)

private val Context.liveUpdateDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "live_update_preferences"
)

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    @ConsentDataStore
    fun provideConsentDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.consentDataStore

    @Provides
    @Singleton
    @LoginDataStore
    fun provideLoginDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.loginDataStore

    @Provides
    @Singleton
    @OptimizeDataStore
    fun provideOptimizeDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.optimizeDataStore

    @Provides
    @Singleton
    @LiveUpdateDataStore
    fun provideLiveUpdateDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        context.liveUpdateDataStore


}