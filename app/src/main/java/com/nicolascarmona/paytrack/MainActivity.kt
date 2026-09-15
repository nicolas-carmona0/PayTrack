package com.nicolascarmona.paytrack

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import com.nicolascarmona.paytrack.ui.PayTrackScreen
import com.nicolascarmona.paytrack.ui.theme.PayTrackTheme
import com.nicolascarmona.paytrack.ui.theme.ThemeMode
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint  // Hilt necesita esto en la actividad principal
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Pedir permiso de notificaciones (Solo para Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }

        // 2. Programar la alarma diaria a las 9:00 AM
        scheduleDailyNotification()

        setContent {

            val systemIsDark = isSystemInDarkTheme()

            var themeMode by remember {
                mutableStateOf(if (systemIsDark) ThemeMode.DARK else ThemeMode.LIGHT)
            }

            PayTrackTheme(themeMode = themeMode) {

                PayTrackScreen(
                    currentTheme = themeMode,
                    onThemeChange = { nuevoTema -> themeMode = nuevoTema }
                )
            }
        }
    }

    private fun scheduleDailyNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, PaymentNotificationReceiver::class.java)

        // FLAG_IMMUTABLE es obligatorio desde Android 12
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // Configuramos la hora
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
        }

        // Si ya pasaron las 9:00 AM de hoy, la programamos para mañana
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Le decimos a Android que la repita aproximadamente cada día
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }
}
