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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PersonalizationRepository @Inject constructor(
    private val personalizationService: PersonalizationService,
    private val scopeStore: OptimizeScopeStore
) {
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
    val offers: StateFlow<PersonalizedOffers> = _offers.asStateFlow()

    suspend fun setDecisionScope(name: String) = scopeStore.setDecisionScopeName(name)

    suspend fun setTargetActivity(name: String) = scopeStore.setTargetActivityName(name)

    suspend fun refreshOffers() {
        val config = scopeConfig.first()
        refreshOffers(config.decisionScopeName, config.targetActivityName)
    }

    suspend fun refreshOffers(decisionScopeName: String, targetActivityName: String) {
        val decisionScope = decisionScopeName.takeIf { it.isNotBlank() }
        val targetScope = targetActivityName.takeIf { it.isNotBlank() && it != decisionScope }

        val scopeNames = listOfNotNull(decisionScope, targetScope)
        if (scopeNames.isEmpty()) {
            _offers.value = PersonalizedOffers()
            return
        }

        val result = personalizationService.fetchOffers(scopeNames)
        _offers.value = PersonalizedOffers(
            offerDecisioning = decisionScope?.let { result[it] }.orEmpty(),
            target = targetScope?.let { result[it] }.orEmpty()
        )
    }

    fun trackOfferDisplayed(offerId: String) = personalizationService.trackDisplayed(offerId)

    fun trackOfferTapped(offerId: String) = personalizationService.trackTapped(offerId)
}
