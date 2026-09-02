package com.adobe.marketing.nimbus.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoginPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
){
    val hasChosenGuest: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[HAS_CHOSEN_GUEST_KEY] ?: false
    }

    suspend fun setChoseGuest() {
        dataStore.edit {
            prefs ->
            prefs[HAS_CHOSEN_GUEST_KEY] = true
        }
    }

    suspend fun clearChoseGuest() {
        dataStore.edit { prefs -> prefs[HAS_CHOSEN_GUEST_KEY] = false }
    }

    private companion object {
        val HAS_CHOSEN_GUEST_KEY = booleanPreferencesKey("has_chosen_guest")
    }
}