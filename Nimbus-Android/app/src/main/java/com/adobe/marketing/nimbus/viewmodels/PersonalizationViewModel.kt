package com.adobe.marketing.nimbus.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adobe.marketing.nimbus.datamodels.PersonalizedOffers
import com.adobe.marketing.nimbus.repositories.PersonalizationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Powered by the AEP Optimize extension via [PersonalizationRepository]. This is the ViewModel
 * behind Home's "Recommended for You" section: it refreshes on screen appear and pull-to-
 * refresh, using whatever decision scope/Target activity is currently configured in Profile's
 * dev lab, and reports display/tap engagement back to Edge so AJO/Target campaign reporting
 * reflects real on-screen impressions rather than every fetched offer.
 */
@HiltViewModel
class PersonalizationViewModel @Inject constructor(
    private val personalizationRepository: PersonalizationRepository
) : ViewModel() {

    /** Mirrors the repository's cached personalized offers directly. */
    val offers: StateFlow<PersonalizedOffers> =
        personalizationRepository.offers

    /** Refetches offers using the currently persisted decision scope/Target activity. */
    fun refresh() {
        viewModelScope.launch { personalizationRepository.refreshOffers() }
    }

    /** Reports a personalized offer actually scrolled into view. */
    fun onOfferDisplayed(offerId: String) =
        personalizationRepository.trackOfferDisplayed(offerId)

    /** Reports a tap on a personalized offer. */
    fun onOfferTapped(offerId: String) =
        personalizationRepository.trackOfferTapped(offerId)
}