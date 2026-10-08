package com.adobe.marketing.nimbus.viewmodels

import androidx.lifecycle.ViewModel
import com.adobe.marketing.nimbus.datamodels.OrderTrackingState
import com.adobe.marketing.nimbus.repositories.LiveUpdateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject

@HiltViewModel
class LiveUpdateViewModel @Inject constructor(
    private val liveUpdateRepository: LiveUpdateRepository
) : ViewModel() {

    val orderState: StateFlow<OrderTrackingState?> = liveUpdateRepository.orderState

    private val _actionFailed = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val actionFailed: SharedFlow<Unit> = _actionFailed.asSharedFlow()

    fun startOrder() = reportFailure(liveUpdateRepository.startOrder())
    fun advanceOrder() = reportFailure(liveUpdateRepository.advanceOrder())
    fun endOrder() = reportFailure(liveUpdateRepository.endOrder())

    private fun reportFailure(succeeded: Boolean) {
        if (!succeeded) _actionFailed.tryEmit(Unit)
    }
}
