package com.adobe.marketing.nimbus.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.adobe.marketing.nimbus.datamodels.OrderStep
import com.adobe.marketing.nimbus.datamodels.OrderTrackingState
import com.adobe.marketing.nimbus.di.LiveUpdateDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LiveUpdatePreferences @Inject constructor(
    @LiveUpdateDataStore private val dataStore: DataStore<Preferences>
) {
    suspend fun load(): OrderTrackingState? {
        val prefs = dataStore.data.first()
        val orderNumber = prefs[ORDER_NUMBER_KEY] ?: return null
        val step = OrderStep.fromIndex(prefs[STEP_KEY] ?: return null) ?: return null
        return OrderTrackingState(
            orderNumber = orderNumber,
            step = step,
            etaDays = prefs[ETA_DAYS_KEY] ?: 0
        )
    }

    suspend fun save(state: OrderTrackingState?) {
        dataStore.edit { prefs ->
            if (state == null) {
                prefs.remove(ORDER_NUMBER_KEY)
                prefs.remove(STEP_KEY)
                prefs.remove(ETA_DAYS_KEY)
            } else {
                prefs[ORDER_NUMBER_KEY] = state.orderNumber
                prefs[STEP_KEY] = state.step.ordinal
                prefs[ETA_DAYS_KEY] = state.etaDays
            }
        }
    }

    private companion object {
        val ORDER_NUMBER_KEY = stringPreferencesKey("order_number")
        val STEP_KEY = intPreferencesKey("step")
        val ETA_DAYS_KEY = intPreferencesKey("eta_days")
    }
}
