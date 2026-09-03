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
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainTabViewModel @Inject constructor(
    private val analyticsService: AnalyticsService,
    private val deepLinkRepository: DeepLinkRepository
): ViewModel() {

    private val _selectedTab = MutableStateFlow(AppTab.HOME)
    val selectedTab: StateFlow<AppTab> = _selectedTab.asStateFlow()

    init {
        analyticsService.trackState(AppTab.HOME.name.lowercase())
        viewModelScope.launch {
            deepLinkRepository.navigationRequests.collect { tab -> selectTab(tab) }
        }
    }

    fun selectTab(tab: AppTab) {
        if (tab == _selectedTab.value) return
        _selectedTab.value = tab
        analyticsService.trackState(tab.name.lowercase())
    }
}
