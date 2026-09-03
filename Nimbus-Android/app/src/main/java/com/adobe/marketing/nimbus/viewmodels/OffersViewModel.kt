package com.adobe.marketing.nimbus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adobe.marketing.nimbus.datamodels.OffersUiState
import com.adobe.marketing.nimbus.datamodels.OfferSurface
import com.adobe.marketing.nimbus.repositories.OffersRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OffersViewModel @Inject constructor(
    private val offersRepository: OffersRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(OffersUiState())
    val uiState: StateFlow<OffersUiState> = _uiState.asStateFlow()

    private val loadedSurfaces = mutableSetOf<OfferSurface>()

    private val _fetchFailed = MutableSharedFlow<Unit>()
    val fetchFailed: SharedFlow<Unit> = _fetchFailed.asSharedFlow()

    fun ensureLoaded(surface: OfferSurface) {
        if (surface in loadedSurfaces) return
        viewModelScope.launch {
            val cards = offersRepository.fetchOffers(surface)
            if (cards == null) {
                _fetchFailed.emit(Unit)
                return@launch
            }
            if (cards.isNotEmpty()) {
                loadedSurfaces.add(surface)
            }
            _uiState.value = _uiState.value.copy(
                offersBySurface = _uiState.value.offersBySurface + (surface to cards)
            )
        }
    }

    fun onCardDisplayed(cardId: String) {
        this@OffersViewModel.offersRepository.trackDisplay(cardId)
    }

    fun onCardInteracted(cardId: String) {
        this@OffersViewModel.offersRepository.trackInteract(cardId)
    }

    fun onCardDismissed(surface: OfferSurface, cardId: String) {
        this@OffersViewModel.offersRepository.trackDismiss(cardId)
        _uiState.value = _uiState.value.copy(
            offersBySurface = _uiState.value.offersBySurface.mapValues { (key, cards) ->
                if (key == surface) cards.filterNot { it.id == cardId } else cards
            }
        )
    }

    fun refresh(surface: OfferSurface) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRefreshing = true)
            val cards = offersRepository.fetchOffers(surface)
            if (cards != null) {
                loadedSurfaces.add(surface)
                _uiState.value = _uiState.value.copy(
                    offersBySurface = _uiState.value.offersBySurface + (surface to cards)
                )
            }
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }
}