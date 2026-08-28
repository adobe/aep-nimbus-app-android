package com.adobe.marketing.nimbus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adobe.marketing.nimbus.datamodels.ConsentGateState
import com.adobe.marketing.nimbus.datamodels.ConsentState
import com.adobe.marketing.nimbus.repositories.ConsentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConsentViewModel @Inject constructor(
    private val consentRepository: ConsentRepository
): ViewModel() {

    val uiState: StateFlow<ConsentGateState> = consentRepository.consentGateState.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ConsentGateState(ConsentState.PENDING, false)
    )

    fun onConsentChosen(state: ConsentState) {
        viewModelScope.launch {
            consentRepository.chooseConsent(state)
        }
    }
}