package com.nicolascarmona.paytrack.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.room.TypeConverter
import com.nicolascarmona.paytrack.model.TimeUnit
import java.time.LocalDate

// Room solo sabe guardar textos, números enteros y decimales. Vamos a traducir los colores y fechas
// a texto/número para guardarlos, y viceversa para leerlos

class Converters {

    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.toString()

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate = LocalDate.parse(dateString)


    @TypeConverter
    fun fromTimeUnit(timeUnit: TimeUnit): String = timeUnit.name

    @TypeConverter
    fun toTimeUnit(name: String): TimeUnit = TimeUnit.valueOf(name)


}