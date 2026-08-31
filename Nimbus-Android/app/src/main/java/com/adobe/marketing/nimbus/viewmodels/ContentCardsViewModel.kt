package com.adobe.marketing.nimbus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adobe.marketing.nimbus.datamodels.ContentCard
import com.adobe.marketing.nimbus.datamodels.ContentCardsUiState
import com.adobe.marketing.nimbus.datamodels.ContentSurface
import com.adobe.marketing.nimbus.repositories.ContentCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContentCardsViewModel @Inject constructor(
    private val contentCardRepository: ContentCardRepository
): ViewModel() {

    private val _uiState = MutableStateFlow(ContentCardsUiState())
    val uiState: StateFlow<ContentCardsUiState> = _uiState.asStateFlow()

    private val loadedSurfaces = mutableSetOf<ContentSurface>()

    fun ensureLoaded(surface: ContentSurface) {
        if (surface in loadedSurfaces) return
        viewModelScope.launch {
            val cards = contentCardRepository.fetchContentCards(surface) ?: return@launch
            loadedSurfaces.add(surface)
            _uiState.value = _uiState.value.copy(
                cardsBySurface = _uiState.value.cardsBySurface + (surface to cards)
            )
        }
    }

    fun onCardDisplayed(cardId: String) {
        contentCardRepository.trackDisplay(cardId)
    }

    fun onCardInteracted(cardId: String) {
        contentCardRepository.trackInteract(cardId)
    }

    fun onCardDismissed(surface: ContentSurface, cardId: String) {
        contentCardRepository.trackDismiss(cardId)
        _uiState.value = _uiState.value.copy(
            cardsBySurface = _uiState.value.cardsBySurface.mapValues { (key, cards) ->
                if (key == surface) cards.filterNot { it.id == cardId } else cards
            }
        )
    }
}