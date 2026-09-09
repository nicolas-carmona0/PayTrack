package com.nicolascarmona.paytrack

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import com.nicolascarmona.paytrack.ui.PayTrackScreen
import com.nicolascarmona.paytrack.ui.theme.PayTrackTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PayTrackTheme {
                PayTrackScreen()
                //MyCalendar()
            }
        }
    }
}
