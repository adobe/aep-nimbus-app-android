package com.adobe.marketing.nimbus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adobe.marketing.nimbus.datamodels.AppTab
import com.adobe.marketing.nimbus.repositories.DeepLinkRepository
import com.adobe.marketing.nimbus.services.AnalyticsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Powered by MobileCore's `trackState` (via [AnalyticsService]) fired on every bottom-tab
 * switch. This is the capability behind the app's screen-tracking coverage: each tab change
 * qualifies AJO trigger rules keyed on `Track state = home/shop/cart/inbox/profile`, which is
 * exactly how the Home "flash sale" style content-card triggers get exercised in this app.
 * Also owns tab selection driven by deep links via [DeepLinkRepository].
 */
@HiltViewModel
class MainTabViewModel @Inject constructor(
    private val analyticsService: AnalyticsService,
    private val deepLinkRepository: DeepLinkRepository
): ViewModel() {

    private val _selectedTab = MutableStateFlow(AppTab.HOME)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    // Tracks the initial Home landing and listens for deep-link-driven tab changes.
    init {
        analyticsService.trackState(AppTab.HOME.name.lowercase())
        viewModelScope.launch {
            deepLinkRepository.navigationRequests.collect { tab -> selectTab(tab) }
        }
    }

    /** Switches the active tab and fires a trackState for the new screen, if it actually changed. */
    fun selectTab(tab: AppTab) {
        if (tab == _selectedTab.value) return
        _selectedTab.update { tab }
        analyticsService.trackState(tab.name.lowercase())
    }
}
