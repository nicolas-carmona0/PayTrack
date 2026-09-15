package com.nicolascarmona.paytrack

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Hilt necesita un punto de anclaje general para saber cuándo se inicia y cuándo se cierra la app.
// Para esto necesitamos la clase Application personalizada y registrarla en el manifest de Android

@HiltAndroidApp
class PayTrackApp: Application()