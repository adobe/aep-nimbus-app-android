package com.adobe.marketing.nimbus.services

import android.content.Context
import com.adobe.marketing.nimbus.R
import androidx.core.app.NotificationCompat
import androidx.core.graphics.drawable.IconCompat
import com.adobe.marketing.mobile.messaging.liveupdate.ILiveUpdateStyleProvider
import com.adobe.marketing.mobile.messaging.liveupdate.LiveUpdatePayload
import com.adobe.marketing.nimbus.datamodels.OrderStep

class NimbusLiveUpdateStyleProvider(
    private val context: Context
) : ILiveUpdateStyleProvider {

    override fun provideStyle(payload: LiveUpdatePayload): NotificationCompat.Style? {
        OrderPayloadParser.parse(payload)?.let { order ->
            return NotificationCompat.ProgressStyle()
                .setProgress(order.step.ordinal * SEGMENT_LENGTH)
                .setProgressTrackerIcon(
                    IconCompat.createWithResource(context, R.drawable.shipping_truck)
                )
                .setProgressSegments(
                    SEGMENT_COLORS.map {
                        NotificationCompat.ProgressStyle.Segment(SEGMENT_LENGTH).setColor(it)
                    }
                )
        }
        SalePayloadParser.parse(payload)?.let { sale ->
            return NotificationCompat.BigTextStyle()
                .bigText("${sale.discountPercent}% off. Today only!")
        }
        return null
    }

    private companion object {
        val SEGMENT_LENGTH = 100 / (OrderStep.entries.size - 1)
        val SEGMENT_COLORS = listOf(
            0xFF03A9F4.toInt(), // placed -> packed
            0xFF3F51B5.toInt(), // packed -> shipped
            0xFF9C27B0.toInt(), // shipped -> out for delivery
            0xFF4CAF50.toInt()  // out for delivery -> delivered
        )
    }
}