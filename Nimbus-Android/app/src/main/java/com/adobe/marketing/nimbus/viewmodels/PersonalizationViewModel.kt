package com.adobe.marketing.nimbus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adobe.marketing.nimbus.datamodels.PersonalizedOffers
import com.adobe.marketing.nimbus.repositories.PersonalizationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PersonalizationViewModel @Inject constructor(
    private val personalizationRepository: PersonalizationRepository
) : ViewModel() {

    val offers: StateFlow<PersonalizedOffers> =
        personalizationRepository.offers

    fun refresh() {
        viewModelScope.launch { personalizationRepository.refreshOffers() }
    }

    fun onOfferDisplayed(offerId: String) =
        personalizationRepository.trackOfferDisplayed(offerId)

    fun onOfferTapped(offerId: String) =
        personalizationRepository.trackOfferTapped(offerId)
}