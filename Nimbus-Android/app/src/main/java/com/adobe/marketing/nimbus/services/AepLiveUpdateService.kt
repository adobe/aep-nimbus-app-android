package com.adobe.marketing.nimbus.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import com.adobe.marketing.mobile.messaging.liveupdate.ILiveUpdateListener
import com.adobe.marketing.mobile.messaging.liveupdate.LiveUpdatePayload
import com.adobe.marketing.mobile.messaging.liveupdate.LiveUpdates
import com.adobe.marketing.nimbus.MainActivity
import com.adobe.marketing.nimbus.data.LiveUpdatePreferences
import com.adobe.marketing.nimbus.datamodels.OrderStep
import com.adobe.marketing.nimbus.datamodels.OrderTrackingState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class AepLiveUpdateService @Inject constructor(
    @param: ApplicationContext private val context: Context,
    private val preferences: LiveUpdatePreferences
) : LiveUpdateService {

    private val _orderState = MutableStateFlow<OrderTrackingState?>(null)
    override val orderState: StateFlow<OrderTrackingState?> = _orderState.asStateFlow()

    private val lastTimestamp = AtomicLong(0L)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    init {
        ensureLiveUpdateChannelsExist()
        LiveUpdates.setLiveUpdateListener(object : ILiveUpdateListener {
            override fun onLiveUpdateReceived(payload: LiveUpdatePayload) {
                if (SalePayloadParser.isSale(payload)) return
                if (payload.eventType == LiveUpdatePayload.EVENT_TYPE_END) {
                    _orderState.value = null
                } else {
                    OrderPayloadParser.parse(payload)?.let { _orderState.value = it }
                }
            }

            override fun onDismissed(payload: LiveUpdatePayload) {
                if (SalePayloadParser.isSale(payload)) return
                _orderState.value = null
            }

            override fun onClick(payload: LiveUpdatePayload) {
                context.startActivity(
                    Intent(context, MainActivity::class.java)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                )
            }
        })
        scope.launch { restoreThenPersist() }
    }

    override fun startOrder(): Boolean =
        post(
            orderNumber = Random.nextInt(1000, 10000).toString(),
            eventType = LiveUpdatePayload.EVENT_TYPE_LOCAL_START,
            step = OrderStep.PLACED,
            etaDays = INITIAL_ETA_DAYS
        )

    override fun advanceOrder(): Boolean {
        val current = _orderState.value ?: return false
        val next = current.step.next ?: return false
        return post(
            orderNumber = current.orderNumber,
            eventType = LiveUpdatePayload.EVENT_TYPE_UPDATE,
            step = next,
            etaDays = (current.etaDays - 1).coerceAtLeast(0)
        )
    }

    override fun endOrder(): Boolean {
        val current = _orderState.value ?: return false
        return post(
            orderNumber = current.orderNumber,
            eventType = LiveUpdatePayload.EVENT_TYPE_END,
            step = OrderStep.DELIVERED,
            etaDays = 0,
            dismissAfterSeconds = END_DISMISS_AFTER_SECONDS
        )
    }

    private fun post(
        orderNumber: String,
        eventType: String,
        step: OrderStep,
        etaDays: Int,
        dismissAfterSeconds: Long? = null
    ): Boolean {
        val timestamp = lastTimestamp.updateAndGet { maxOf(System.currentTimeMillis() / 1000, it + 1) }
        val payload = LiveUpdatePayload.create(
            notificationId = orderNumber,
            channelId = CHANNEL_ID,
            eventType = eventType,
            title = "Order #$orderNumber",
            timestamp = timestamp,
            body = step.label,
            criticalText = step.label,
            dismissAfterSeconds = dismissAfterSeconds,
            contentState = OrderPayloadParser.contentState(step, etaDays)
        )
        val posted = LiveUpdates.triggerLocalLiveUpdate(context, payload)
        if (posted) {
            _orderState.value = if (eventType == LiveUpdatePayload.EVENT_TYPE_END) {
                null
            } else {
                OrderTrackingState(orderNumber, step, etaDays)
            }
        }
        return posted
    }

    // Restores a persisted order only if its notification is still showing, then keeps
    // the persisted copy in sync with every later state change (a null state clears it).
    private suspend fun restoreThenPersist() {
        preferences.load()?.let { saved ->
            if (isNotificationShowing(saved.orderNumber)) _orderState.compareAndSet(null, saved)
        }
        _orderState.collect { preferences.save(it) }
    }

    // The Live Updates SDK posts each notification under notificationId.hashCode().
    private fun isNotificationShowing(orderNumber: String): Boolean {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return false
        return manager.activeNotifications.any { it.id == orderNumber.hashCode() }
    }

    // Pre-creates both Live Update channels with friendly names, so neither falls back
    // to the SDK's auto-created default label if a remote/local push arrives first.
    private fun ensureLiveUpdateChannelsExist() {
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        if (manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Order Tracking",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Live Updates for order tracking" }
            )
        }
        if (manager.getNotificationChannel(FLASH_SALE_CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    FLASH_SALE_CHANNEL_ID,
                    "Flash Sale Alerts",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply { description = "Live Updates for flash sale broadcasts" }
            )
        }
    }

    private companion object {
        const val CHANNEL_ID = "nimbus_order_updates"
        const val INITIAL_ETA_DAYS = 5
        const val END_DISMISS_AFTER_SECONDS = 8L
    }
}
