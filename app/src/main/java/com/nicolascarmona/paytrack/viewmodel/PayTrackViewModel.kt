package com.nicolascarmona.paytrack.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.nicolascarmona.paytrack.data.SubscriptionDao
import com.nicolascarmona.paytrack.model.CategoryItem
import com.nicolascarmona.paytrack.model.PayTrackItem
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class PayTrackViewModel @Inject constructor(
    private val dao: SubscriptionDao,
    @ApplicationContext context: Context
) : ViewModel() {

    data class PayTrackBackup(
        val items: List<PayTrackItem> = emptyList(),
        val monthlySavingsGoal: Float = 0f
    )

    private val prefs = context.getSharedPreferences("paytrack_prefs", Context.MODE_PRIVATE)
    private val _monthlySavingsGoal = MutableStateFlow(prefs.getFloat("monthly_savings_goal", 0f))
    val monthlySavingsGoal: StateFlow<Float> = _monthlySavingsGoal.asStateFlow()

    private val gson = Gson()

    val categories: StateFlow<List<CategoryItem>> = dao.getAllCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val subscriptions: StateFlow<List<PayTrackItem>> = dao.getAllSubscriptions()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch(Dispatchers.IO) {
            if (dao.getAllCategoriesList().isEmpty()) {
                listOf("General", "Gastos fijos", "Ocio", "Suscripciones").forEach {
                    dao.insertCategory(CategoryItem(it))
                }
            }
        }
    }

    fun setMonthlySavingsGoal(amount: Float) {
        val safeAmount = amount.coerceAtLeast(0f)
        prefs.edit().putFloat("monthly_savings_goal", safeAmount).apply()
        _monthlySavingsGoal.value = safeAmount
    }

    fun exportBackupJson(onResult: (String) -> Unit) {
        viewModelScope.launch {
            val backup = withContext(Dispatchers.IO) {
                PayTrackBackup(
                    items = dao.getAllSubscriptionList(),
                    monthlySavingsGoal = _monthlySavingsGoal.value
                )
            }
            onResult(gson.toJson(backup))
        }
    }

    fun importBackupJson(jsonString: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val trimmed = jsonString.trim()
                if (trimmed.startsWith("[")) {
                    val listType = object : TypeToken<List<PayTrackItem>>() {}.type
                    val oldItems: List<PayTrackItem> = gson.fromJson(trimmed, listType)
                    restoreItemsList(oldItems)
                } else {
                    val backup = gson.fromJson(trimmed, PayTrackBackup::class.java)
                    restoreItemsList(backup.items)
                    if (backup.monthlySavingsGoal > 0f) {
                        setMonthlySavingsGoal(backup.monthlySavingsGoal)
                    }
                }
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Formato de archivo inválido")
            }
        }
    }

    private suspend fun restoreItemsList(rawItems: List<PayTrackItem>) {
        if (rawItems.isEmpty()) return
        val normalized = rawItems.map { item ->
            if (item.category.isNullOrBlank()) item.copy(category = "General") else item
        }
        withContext(Dispatchers.IO) {
            normalized.map { it.category }.distinct().forEach { catName ->
                dao.insertCategory(CategoryItem(catName))
            }
            dao.insertAll(normalized)
        }
    }

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

    fun addCategory(name: String) {
        val cleanName = name.trim()
        if (cleanName.isNotEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                dao.insertCategory(CategoryItem(cleanName))
            }
        }
    }

    fun deleteCategory(category: CategoryItem) {
        if (category.name == "General") return
        viewModelScope.launch(Dispatchers.IO) {
            dao.resetCategoryToDefault(category.name)
            dao.deleteCategory(category)
        }
    }
}