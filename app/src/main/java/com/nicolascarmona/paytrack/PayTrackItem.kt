package com.nicolascarmona.paytrack

import androidx.compose.ui.graphics.Color
import java.util.UUID

enum class TimeUnit(val labelSingular: String, val labelPlural: String){
    DAY("Día", "Días"),
    WEEK("Semana", "Semanas"),
    MONTH("Mes", "Meses"),
    YEAR("Año", "Años"),
}

data class PayTrackItem(
    // Genera un ID único automático por cada suscripción
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val day: Int,
    val price: Float,
    val color: Color,
    val frequency: Int,
    val timeUnit: TimeUnit
)