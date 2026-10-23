package com.adobe.marketing.nimbus.repositories

import com.adobe.marketing.nimbus.data.OptimizeScopeStore
import com.adobe.marketing.nimbus.datamodels.OptimizeScopeConfig
import com.adobe.marketing.nimbus.datamodels.PersonalizedOffers
import com.adobe.marketing.nimbus.services.PersonalizationService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
/**
 * Powered by the AEP Optimize extension via [PersonalizationService], combined with the
 * user-configurable scope in [OptimizeScopeStore]. This is the single cached source of truth
 * behind both Profile's Personalized Offers dev lab and Home's Recommended for You section,
 * so a fetch triggered from either screen is immediately visible on the other without a
 * duplicate network call. Singleton-scoped because `Optimize.onPropositionsUpdate` is a
 * one-time, process-wide SDK registration that must only ever happen once.
 */
class PersonalizationRepository @Inject constructor(
    private val personalizationService: PersonalizationService,
    private val scopeStore: OptimizeScopeStore
) {
    /** Combines the persisted decision-scope and Target-activity strings into one config. */
    val scopeConfig: Flow<OptimizeScopeConfig> = combine(
        scopeStore.decisionScopeName,
        scopeStore.targetActivityName
    ) { decisionScope, targetActivity ->
        OptimizeScopeConfig(
            decisionScopeName = decisionScope,
            targetActivityName = targetActivity
        )
    }

    private val _offers = MutableStateFlow(PersonalizedOffers())
    /** The last successfully fetched personalized offers, shared by every observer. */
    val offers: StateFlow<PersonalizedOffers> = _offers.asStateFlow()

    private var lastLoadedScopeKey: String? = null
    /** Refetches only if the persisted scope config differs from the last successful fetch. */


    /** Persists the decision-scope name, debounced upstream by the caller. */
    suspend fun setDecisionScope(name: String) = scopeStore.setDecisionScopeName(name)

    /** Persists the Target-activity name, debounced upstream by the caller. */
    suspend fun setTargetActivity(name: String) = scopeStore.setTargetActivityName(name)

    /** Refetches offers using whatever scope config is currently persisted. */
    suspend fun refreshOffers() {
        val config = scopeConfig.first()
        refreshOffers(config.decisionScopeName, config.targetActivityName)
    }

    /** Fetches offers for the given scope names, deduping an identical scope in both fields. */
    suspend fun refreshOffers(decisionScopeName: String, targetActivityName: String) {
        val decisionScope = decisionScopeName.takeIf { it.isNotBlank() }
        val targetScope = targetActivityName.takeIf { it.isNotBlank() && it != decisionScope }

        val scopeNames = listOfNotNull(decisionScope, targetScope)
        if (scopeNames.isEmpty()) {
            _offers.update { PersonalizedOffers() }
            return
        }

        val result = personalizationService.fetchOffers(scopeNames)
        _offers.update {
            PersonalizedOffers(
                offerDecisioning = decisionScope?.let { result[it] }.orEmpty(),
                target = targetScope?.let { result[it] }.orEmpty()
            )
        }
    }

    /** Reports a personalized offer actually scrolled into view. */
    fun trackOfferDisplayed(offerId: String) = personalizationService.trackDisplayed(offerId)

    /** Reports a tap on a personalized offer. */
    fun trackOfferTapped(offerId: String) = personalizationService.trackTapped(offerId)
}
