package com.nicolascarmona.paytrack.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class TimeUnit(val labelSingular: String, val labelPlural: String){
    MONTH("Mes", "Meses"),
    YEAR("Año", "Años"),
}

@Entity(tableName = "subscriptions")
data class PayTrackItem(
    // Genera un ID único automático por cada suscripción
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),

    val name: String,
    val day: Int,
    val price: Float,
    val color: Int,
    val frequency: Int,
    val timeUnit: TimeUnit
)