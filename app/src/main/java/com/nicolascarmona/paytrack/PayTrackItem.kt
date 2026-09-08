package com.nicolascarmona.paytrack

import androidx.compose.ui.graphics.Color
import java.util.UUID

data class PayTrackItem(
    // Genera un ID único automático por cada suscripción
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val description: String,
    val day: Int,
    val price: Float,
    // Color único asignado a esa tarjeta
//    val backgroundColor: Color
)