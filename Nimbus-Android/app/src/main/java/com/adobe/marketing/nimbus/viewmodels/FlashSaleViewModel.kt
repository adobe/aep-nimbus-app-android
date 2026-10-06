package com.adobe.marketing.nimbus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adobe.marketing.nimbus.repositories.FlashSaleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FlashSaleViewModel @Inject constructor(
    private val flashSaleRepository: FlashSaleRepository
): ViewModel() {

    val optedIn: StateFlow<Boolean> = flashSaleRepository.optedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    private val _actionFailed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val actionFailed: SharedFlow<Unit> = _actionFailed.asSharedFlow()

    fun setOptIn(enabled: Boolean) {
        viewModelScope.launch {
            if (!flashSaleRepository.setOptIn(enabled)) {
                _actionFailed.tryEmit(Unit)
            }
        }
    }
}