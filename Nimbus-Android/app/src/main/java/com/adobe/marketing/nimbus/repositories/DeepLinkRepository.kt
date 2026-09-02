package com.adobe.marketing.nimbus.repositories

import com.adobe.marketing.nimbus.datamodels.AppTab
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DeepLinkRepository @Inject constructor() {
    private val _navigationRequests = MutableSharedFlow<AppTab>(extraBufferCapacity = 1)
    val navigationRequests: SharedFlow<AppTab> = _navigationRequests.asSharedFlow()

    fun requestNavigation(tab: AppTab) {
        _navigationRequests.tryEmit(tab)
    }
}