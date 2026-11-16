package com.nicolascarmona.paytrack

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.nicolascarmona.paytrack.data.SubscriptionDao
import com.nicolascarmona.paytrack.model.PayTrackItem
import com.nicolascarmona.paytrack.ui.shouldShowIn
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint
class PaymentNotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var dao: SubscriptionDao

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val todayDate = LocalDate.now()
                val maxDaysInMonth = todayDate.lengthOfMonth()

                // Filtramos solo gastos (!item.isIncome) que correspondan al día y mes actual
                val allSubscriptions = dao.getAllSubscriptionList()
                val todaysPayments = allSubscriptions.filter { item ->
                    val effectiveDay = minOf(item.day, maxDaysInMonth)
                    !item.isIncome &&
                            effectiveDay == todayDate.dayOfMonth &&
                            item.shouldShowIn(todayDate.monthValue, todayDate.year)
                }

                if (todaysPayments.isNotEmpty()) {
                    showNotification(context, todaysPayments)
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private fun showNotification(context: Context, payments: List<PayTrackItem>) {
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "paytrack_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Pago",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val totalAmount = payments.sumOf { it.price.toDouble() }
        val contentText = if (payments.size == 1) {
            "Hoy toca pagar ${payments[0].name} (${String.format("%.2f", payments[0].price)} €)"
        } else {
            "Tienes ${payments.size} pagos hoy por un total de ${String.format("%.2f", totalAmount)} €"
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("¡Día de pago en PayTrack!")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }
}