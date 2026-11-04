package com.adobe.marketing.nimbus.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.adobe.marketing.nimbus.datamodels.ConsentState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ConsentPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    val consentState: Flow<ConsentState> = dataStore.data.map { prefs ->
        ConsentState.valueOf(prefs[CONSENT_KEY] ?: ConsentState.PENDING.name)
    }

    val hasChosenConsent: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[HAS_CHOSEN_KEY] ?: false
    }

    suspend fun setConsent(state: ConsentState) {
        dataStore.edit { prefs ->
            prefs[CONSENT_KEY] = state.name
            prefs[HAS_CHOSEN_KEY] = true
        }
    }

    private companion object {
        val CONSENT_KEY = stringPreferencesKey("consent_state")
        val HAS_CHOSEN_KEY = booleanPreferencesKey("has_chosen_consent")
    }
}