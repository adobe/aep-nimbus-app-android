package com.adobe.marketing.nimbus.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.adobe.marketing.nimbus.di.OptimizeDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class OptimizeScopeStore @Inject constructor(
    @OptimizeDataStore private val dataStore: DataStore<Preferences>
) {
    val decisionScopeName: Flow<String> = dataStore.data.map { prefs ->
        prefs[DECISION_SCOPE_KEY] ?: ""
    }

    val targetActivityName: Flow<String> = dataStore.data.map { prefs ->
        prefs[TARGET_ACTIVITY_KEY] ?: ""
    }

    suspend fun setDecisionScopeName(name: String) {
        dataStore.edit { prefs -> prefs[DECISION_SCOPE_KEY] = name }
    }

    suspend fun setTargetActivityName(name: String) {
        dataStore.edit { prefs -> prefs[TARGET_ACTIVITY_KEY] = name }
    }

    private companion object {
        val DECISION_SCOPE_KEY = stringPreferencesKey("optimize_decision_scope_name")
        val TARGET_ACTIVITY_KEY = stringPreferencesKey("optimize_target_activity_name")
    }
}