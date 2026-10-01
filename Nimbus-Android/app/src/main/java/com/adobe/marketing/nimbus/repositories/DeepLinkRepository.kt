package com.adobe.marketing.nimbus.repositories

import com.adobe.marketing.nimbus.datamodels.AppTab
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Not an AEP feature itself, but the shared plumbing several of them route through: the
 * `nimbus://` scheme used by content-card action URLs, in-app-message buttons, and Assurance
 * session links all resolve to a tab here via `MainActivity`'s intent-filter, so any of those
 * AJO-driven surfaces can navigate the app the same way a bottom-tab tap would.
 */
@Singleton
class DeepLinkRepository @Inject constructor() {
    private val _navigationRequests = MutableSharedFlow<AppTab>(replay = 1, extraBufferCapacity = 1)
    val navigationRequests: SharedFlow<AppTab> = _navigationRequests.asSharedFlow()

    /** Emits a tab-switch request for MainTabViewModel to pick up. */
    fun requestNavigation(tab: AppTab) {
        _navigationRequests.tryEmit(tab)
    }
}