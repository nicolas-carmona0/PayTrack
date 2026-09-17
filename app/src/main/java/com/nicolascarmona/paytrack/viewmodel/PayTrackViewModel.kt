package com.nicolascarmona.paytrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nicolascarmona.paytrack.data.SubscriptionDao
import com.nicolascarmona.paytrack.model.PayTrackItem
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Avisamos que Hilt va a gestionar este ViewModel
@HiltViewModel
class PayTrackViewModel @Inject constructor(
    private val dao: SubscriptionDao
) : ViewModel() {


    val subscriptions: StateFlow<List<PayTrackItem>> = dao.getAllSubscriptions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // Ahora Kotlin ya sabe que esta lista es de PayTrackItem
        )
//    // 1. Estados para saber qué mes y año estamos mirando en el calendario
//    // Empezamos por defecto en el mes y año actuales reales
//    private val currentDate = LocalDate.now()
//    private val _selectedMonth = MutableStateFlow(currentDate.monthValue) // 1 a 12
//    private val _selectedYear = MutableStateFlow(currentDate.year)
//
//    // Exponemos el mes y año a la UI por si quieres mostrar "Septiembre 2026" arriba
//    val selectedMonth: StateFlow<Int> = _selectedMonth
//    val selectedYear: StateFlow<Int> = _selectedYear
//
//    // 2. Leemos la lista completa y cruda de la base de datos
//    private val allSubscriptions = dao.getAllSubscriptions()
//
//    // 3. LA MAGIA: Combinamos la BD con el mes seleccionado.
//    // Si cambia la BD o si cambias de mes, esta lista se recalcula sola.
//    val visibleSubscriptions = combine(
//        allSubscriptions,
//        _selectedMonth,
//        _selectedYear
//    ) { listaCompleta, month, year ->
//        listaCompleta.filter { pago ->
//            pago.shouldShowIn(month, year)
//        }
//    }.stateIn(
//        scope = viewModelScope,
//        started = SharingStarted.WhileSubscribed(5000),
//        initialValue = emptyList()
//    )
//
//    // 4. El gasto total ahora calcula solo sobre los pagos VISIBLES de este mes
//    val totalSpent: StateFlow<Float> = visibleSubscriptions
//        .map { lista -> lista.sumOf { it.price.toDouble() }.toFloat() }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5000),
//            initialValue = 0f
//        )

    // --- FUNCIONES PARA NAVEGAR POR EL CALENDARIO ---

//    fun nextMonth() {
//        if (_selectedMonth.value == 12) {
//            _selectedMonth.value = 1
//            _selectedYear.value += 1
//        } else {
//            _selectedMonth.value += 1
//        }
//    }
//
//    fun previousMonth() {
//        if (_selectedMonth.value == 1) {
//            _selectedMonth.value = 12
//            _selectedYear.value -= 1
//        } else {
//            _selectedMonth.value -= 1
//        }
//    }

    // --- FUNCIONES DE BASE DE DATOS ---

    fun addSubscription(item: PayTrackItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.insertSubscription(item)
        }
    }

    fun updateSubscription(updatedItem: PayTrackItem) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.updateSubscription(updatedItem)
        }
    }

    fun deleteSubscriptionById(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            dao.deleteSubscriptionsById(id)
        }
    }

    // --- LÓGICA DE FILTRADO ---

//    private fun PayTrackItem.shouldShowIn(targetMonth: Int, targetYear: Int): Boolean {
//        val startAbsoluteMonths = (startYear * 12) + startMonth
//        val targetAbsoluteMonths = (targetYear * 12) + targetMonth
//
//        if (targetAbsoluteMonths < startAbsoluteMonths) return false
//
//        val monthsBetween = targetAbsoluteMonths - startAbsoluteMonths
//
//        val cycleInMonths = when (timeUnit) {
//            TimeUnit.MONTH -> frequency
//            TimeUnit.YEAR -> frequency * 12
//        }
//
//        return monthsBetween % cycleInMonths == 0
//    }
}