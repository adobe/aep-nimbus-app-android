package com.adobe.marketing.nimbus.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.adobe.marketing.nimbus.di.LoginDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LoginPreferences @Inject constructor(
    @LoginDataStore private val dataStore: DataStore<Preferences>
){
    val hasPassedLoginGate: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[HAS_PASSED_LOGIN_GATE_KEY] ?: false
    }

    suspend fun markLoginGatePassed() {
        dataStore.edit { prefs ->
            prefs[HAS_PASSED_LOGIN_GATE_KEY] = true
        }
    }

//    suspend fun clearChoseGuest() {
//        dataStore.edit { prefs -> prefs[HAS_CHOSEN_GUEST_KEY] = false }
//    }

    private companion object {
        val HAS_PASSED_LOGIN_GATE_KEY = booleanPreferencesKey("has_passed_login_gate")
    }
}