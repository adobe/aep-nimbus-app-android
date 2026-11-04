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
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Powered by the AJO Messaging extension via [OffersRepository]. This is the shared ViewModel
 * behind every content-card surface in the app (Home carousel, Inbox list, Shop's CBE hero
 * banner, Cart's triggered offer banner) — it's shared rather than per-screen because Home
 * "prefetches all surfaces" at launch, and each screen just reads its own surface's slice of
 * the same cached state, avoiding a duplicate fetch per screen.
 */
@HiltViewModel
class OffersViewModel @Inject constructor(
    private val offersRepository: OffersRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(OffersUiState())
    val uiState: StateFlow<OffersUiState> = _uiState.asStateFlow()

    private val loadedSurfaces = mutableSetOf<OfferSurface>()

    private val _fetchFailed = MutableSharedFlow<Unit>()
    val fetchFailed: SharedFlow<Unit> = _fetchFailed.asSharedFlow()

    /** Fetches a surface's content cards once, unless it has already loaded successfully. */
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
            _uiState.update { it.copy(offersBySurface = it.offersBySurface + (surface to cards)) }
        }
    }

    /** Reports a card actually scrolled into view. */
    fun onCardDisplayed(cardId: String) {
        this@OffersViewModel.offersRepository.trackDisplay(cardId)
    }

    /** Reports a tap on a card. */
    fun onCardInteracted(cardId: String) {
        this@OffersViewModel.offersRepository.trackInteract(cardId)
    }

    /** Reports a dismiss and removes the card from that surface's list immediately. */
    fun onCardDismissed(surface: OfferSurface, cardId: String) {
        this@OffersViewModel.offersRepository.trackDismiss(cardId)
        _uiState.update { state ->
            state.copy(
                offersBySurface = state.offersBySurface.mapValues { (key, cards) ->
                    if (key == surface) cards.filterNot { it.id == cardId } else cards
                }
            )
        }
    }

    /** Force-refetches a surface's content cards, e.g. from pull-to-refresh. */
    fun refresh(surface: OfferSurface) {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val cards = offersRepository.fetchOffers(surface)
            if (cards != null) {
                loadedSurfaces.add(surface)
                _uiState.update { it.copy(offersBySurface = it.offersBySurface + (surface to cards)) }
            }
            _uiState.update { it.copy(isRefreshing = false) }
        }
    }
}