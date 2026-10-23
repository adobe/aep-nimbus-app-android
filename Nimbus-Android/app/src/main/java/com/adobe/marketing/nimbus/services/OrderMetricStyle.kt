@file:Suppress("RestrictedApi")

package com.adobe.marketing.nimbus.services

import android.app.Notification
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationBuilderWithBuilderAccessor
import androidx.core.app.NotificationCompat

class OrderMetricStyle (
    private val etaDays: Int,
    private val step: Int,
    private val totalSteps: Int,
    private val percentDone: Int
) : NotificationCompat.Style() {

    override fun apply(builder: NotificationBuilderWithBuilderAccessor) {
        if (Build.VERSION.SDK_INT < 37) return
        builder.builder.setStyle(platformStyle())
    }

    @RequiresApi(37)
    private fun platformStyle(): Notification.MetricStyle =
        Notification.MetricStyle()
            .addMetric(Notification.Metric(Notification.Metric.FixedText("$step of $totalSteps"), "Step"))
            .addMetric(Notification.Metric(Notification.Metric.FixedInt(etaDays, if (etaDays == 1) "day" else "days"), "ETA"))
            .addMetric(Notification.Metric(Notification.Metric.FixedInt(percentDone, "%"), "Done"))
            .setCriticalMetric(0)
}