package com.nicolascarmona.paytrack.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicolascarmona.paytrack.data.SubscriptionDao
import com.nicolascarmona.paytrack.model.PayTrackItem
import com.nicolascarmona.paytrack.model.TimeUnit
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

// Avisamos que Hilt va a gestionar este ViewModel
@HiltViewModel
class PayTrackViewModel @Inject constructor(    // Hilt inyectará automáticamente el DAO aquí
    // Inyección del DAO
    private val dao: SubscriptionDao
) : ViewModel() {

    // Leemos de la base de datos automáticamente. Se actualiza solo si hay cambios
    val subscriptions = dao.getAllSubscriptions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )


    // Estado interno (mutable) protegido para que solo el VM pueda modificarlo
    private val _subscriptions = MutableStateFlow<List<PayTrackItem>>(emptyList())

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
    fun addSubscription(item: PayTrackItem) {
        // Añadimos el item completo a la lista actual
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertSubscription(item)
        }
    }

    // Actualizar una suscripción existente
    fun updateSubscription(updatedItem: PayTrackItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateSubscription(updatedItem)
        }
    }

    // Borrar una suscripción recibiendo solo su ID (para el diálogo)
    fun deleteSubscriptionById(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteSubscriptionsById(id)
        }
    }

}