package com.nicolascarmona.paytrack

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PayTrackViewModel : ViewModel() {

    // Estado interno (mutable) protegido para que solo el VM pueda modificarlo
    private val _subscriptions = MutableStateFlow<List<PayTrackItem>>(emptyList())

    // Estado expuesto a la vista (de solo lectura)
    val subscriptions: StateFlow<List<PayTrackItem>> = _subscriptions.asStateFlow()

    // Flujo derivado que calcula automáticamente el gasto total mensual
    val totalSpent: StateFlow<Float> = _subscriptions
        .map { lista -> lista.sumOf { it.price.toDouble() }.toFloat() }
        .stateIn(
            // Vincula el ciclo de vida al VM
            scope = viewModelScope,
            // Optimiza la memoria si la pantalla no está visible
            started = SharingStarted.WhileSubscribed(5000),
            // Valor inicial por defecto
            initialValue = 0f
        )


    // Agregar nueva suscripción
    fun addSubscription(name: String, description: String, day: Int, price: Float) {
        val newSubscription = PayTrackItem(
            name = name,
            description = description,
            day = day,
            price = price
        )
        _subscriptions.value += newSubscription
    }

}