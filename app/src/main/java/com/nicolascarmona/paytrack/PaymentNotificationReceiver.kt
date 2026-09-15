package com.nicolascarmona.paytrack

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.nicolascarmona.paytrack.data.SubscriptionDao
import com.nicolascarmona.paytrack.model.PayTrackItem
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@AndroidEntryPoint // Hilt inyectará el DAO aquí
class PaymentNotificationReceiver : BroadcastReceiver(){

    @Inject
    lateinit var dao: SubscriptionDao

    override fun onReceive(context: Context, intent: Intent) {

        // goAsync() mantiene vivo este hilo en segundo plano mientras leemos la base de datos
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val today = LocalDate.now().dayOfMonth
                val todaysPayments = dao.getSubscriptionsForDay(today)

                if (todaysPayments.isNotEmpty()){
                    showNotification(context, todaysPayments)
                }
            } finally {
                pendingResult.finish()
            }
        }

    }

    @SuppressLint("DefaultLocale")
    private fun showNotification(context: Context, payments: List<PayTrackItem>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "paytrack_channel"

        // Crear el canal (Obligatorio en Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Pago",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Calculamos el texto
        val totalAmount = payments.map { it.price }.sum()
        val contentText = if (payments.size == 1) {
            "Hoy toca pagar ${payments[0].name} (${String.format("%.2f", payments[0].price)} €)"
        } else {
            "Tienes ${payments.size} pagos hoy por un total de ${String.format("%.2f", totalAmount)} €"
        }

        // Construimos la notificación
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Icono por defecto de Android
            .setContentTitle("¡Día de pago en PayTrack!")
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1, notification)
    }

}