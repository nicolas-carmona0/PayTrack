package com.nicolascarmona.paytrack.ui

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nicolascarmona.paytrack.model.PayTrackItem
import com.nicolascarmona.paytrack.model.TimeUnit
import com.nicolascarmona.paytrack.ui.theme.ThemeMode
import com.nicolascarmona.paytrack.viewmodel.PayTrackViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.UUID

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayTrackScreen(
    currentTheme: ThemeMode,
    onThemeChange: (ThemeMode) -> Unit,
    payTrackViewModel: PayTrackViewModel = hiltViewModel()
) {

    val subscriptionList by payTrackViewModel.subscriptions.collectAsStateWithLifecycle()
//    val totalMonth by payTrackViewModel.totalSpent.collectAsStateWithLifecycle()

    var actualDate by remember { mutableStateOf(LocalDate.now()) }
    val currentMaxMonthDays = actualDate.lengthOfMonth()

    val effectiveSubscriptionList = subscriptionList.map { item ->
        item.copy(day = minOf(item.day, currentMaxMonthDays))
    }

    val paymentDays = effectiveSubscriptionList.map { it.day }.distinct()

    var name by remember { mutableStateOf("") }
    var dayStr by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }
    var frequencyStr by remember { mutableStateOf("1") }
    var timeUnit by remember { mutableStateOf(TimeUnit.MONTH) }

    var showAddDialog by remember { mutableStateOf(false) }
    var showDayDialog by remember { mutableStateOf(false) }


    var selectedDay by remember { mutableStateOf<Int?>(null) }

    val displayedSubscriptions = if (selectedDay != null) {
        effectiveSubscriptionList.filter { it.day == selectedDay }
    } else {
        effectiveSubscriptionList
    }

    val colorList = listOf(
        Color(0xFFF44336), Color(0xFFE91E63), Color(0xFF9C27B0), Color(0xFF673AB7),
        Color(0xFF3F51B5), Color(0xFF2196F3), Color(0xFF03A9F4), Color(0xFF00BCD4),
        Color(0xFF009688), Color(0xFF4CAF50), Color(0xFF8BC34A), Color(0xFFCDDC39),
        Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722)
    )

    var selectedColor by remember { mutableStateOf(colorList[0]) }

    var editingId by remember { mutableStateOf<String?>(null) }

    val closeAndClearDialog = {
        showAddDialog = false
        editingId = null
        name = ""
        dayStr = ""
        priceStr = ""
        selectedColor = colorList[0]
        frequencyStr = "1"
        timeUnit = TimeUnit.MONTH
    }


    val openEditDialog = { item: PayTrackItem ->
        editingId = item.id
        name = item.name
        frequencyStr = item.frequency.toString()
        timeUnit = item.timeUnit
        dayStr = item.day.toString()
        priceStr = item.price.toString()
        selectedColor = Color(item.color)
        showAddDialog = true
    }

    val realToday = LocalDate.now()
    val isCurrentMonth =
        actualDate.year == realToday.year && actualDate.monthValue == realToday.monthValue

    Scaffold(
        // TÍTULO APP
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = {
                    // El texto se alineará perfectamente a la izquierda con el margen por defecto
                    Text("PayTrack", fontWeight = FontWeight.Bold)
                },
                actions = {
                    ThemeSwitcher(
                        currentTheme = currentTheme,
                        onThemeSelected = onThemeChange
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }
            )
        },

        // BOTÓN AÑADIR EVENTO
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    showAddDialog = true
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir")
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        if (showAddDialog) {
            MyDialog(
                name = name,
                onNameChange = { name = it },
                dayStr = dayStr,
                onDayStrChange = { dayStr = it },
                priceStr = priceStr,
                onPriceStrChange = { priceStr = it },
                onDismiss = closeAndClearDialog,
                payTrackViewModel = payTrackViewModel,
                selectedColor = selectedColor,
                onColorChange = { selectedColor = it },
                colorList = colorList,
                frequencyStr = frequencyStr,
                onFrequencyChange = { frequencyStr = it },
                timeUnit = timeUnit,
                onTimeUnitChange = { timeUnit = it },
                editingId = editingId
            )
        }
        if (showDayDialog && selectedDay != null) {
            DayDialog(
                displayedSubscriptions = displayedSubscriptions,
                onDismiss = {
                    showDayDialog = false
                    selectedDay = null
                },
                selectedDay = selectedDay!!,
                onEdit = openEditDialog
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MyCalendar(
                actualDate = actualDate,
                paymentDays = paymentDays,
                onMonthChange = { newDate -> actualDate = newDate },
                onDayClick = { dayClicked, maxDays ->
                    selectedDay = dayClicked
                    showDayDialog = true
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Payments(
                items = effectiveSubscriptionList,
                onEdit = openEditDialog,
                isCurrentMonth = isCurrentMonth,
                currentDay = realToday.dayOfMonth,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun Payments(
    items: List<PayTrackItem>,
    onEdit: (PayTrackItem) -> Unit,
    isCurrentMonth: Boolean = false,
    currentDay: Int = 0,
    modifier: Modifier
) {
    val sortedItems = items.sortedWith(compareBy({ it.day }, { it.price }))
    val totalPayments = items.map { it.price }.sum()

    Column {

        if (isCurrentMonth) {
            val paidAmount = items.filter { it.day <= currentDay }.map { it.price }.sum()
            val remainingAmount = totalPayments - paidAmount

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Pagado: ${String.format("%.2f €", paidAmount)}",
                    color = Color(0xFF4CAF50), // Verde
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Restante: ${String.format("%.2f €", remainingAmount)}",
                    color = Color(0xFFE53935), // Rojo
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Pago total: ${String.format("%.2f €", totalPayments)}",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        LazyColumn(
            modifier = modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedItems, key = { it.id }) { item ->

                val composeColor = Color(item.color)
                val isLightBackground = composeColor.luminance() > 0.5f
                val textColor = if (isLightBackground) Color.Black else Color.White

                val repeatText = if (item.frequency == 1) {
                    "Cada ${item.timeUnit.labelSingular.lowercase()}"
                } else {
                    "Cada ${item.frequency} ${item.timeUnit.labelPlural.lowercase()}"
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEdit(item) }, // Al pulsar abre MyDialog
                    colors = CardDefaults.cardColors(containerColor = composeColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(item.name, fontWeight = FontWeight.Bold, color = textColor)
                            Text(
                                "Día de cobro: ${item.day} ($repeatText)",
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor.copy(alpha = 0.8f)
                            )
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            String.format("%.2f €", item.price),
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyDialog(
    name: String,
    onNameChange: (String) -> Unit,
    dayStr: String,
    onDayStrChange: (String) -> Unit,
    priceStr: String,
    onPriceStrChange: (String) -> Unit,
    frequencyStr: String,
    onFrequencyChange: (String) -> Unit,
    timeUnit: TimeUnit,
    onTimeUnitChange: (TimeUnit) -> Unit,
    onDismiss: () -> Unit,
    payTrackViewModel: PayTrackViewModel = viewModel(),
    selectedColor: Color,
    onColorChange: (Color) -> Unit,
    colorList: List<Color>,
    editingId: String?
) {

    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Nombre")
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text("Día")
                OutlinedTextField(
                    value = dayStr,
                    onValueChange = {
                        if (it.all { it.isDigit() }) {
                            onDayStrChange(it)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text("Precio")
                OutlinedTextField(
                    value = priceStr,
                    onValueChange = { onPriceStrChange(it.replace(',', '.')) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))

                Text("Se repite cada:")
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = frequencyStr,
                        onValueChange = {
                            if (it.isEmpty() || it.all { char -> char.isDigit() }) onFrequencyChange(
                                it
                            )
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f) // Ocupa menos espacio
                    )

                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded },
                        modifier = Modifier.weight(2f) // Ocupa el doble de espacio
                    ) {
                        val isPlural = (frequencyStr.toIntOrNull() ?: 0) != 1

                        OutlinedTextField(
                            value = if (isPlural) timeUnit.labelPlural else timeUnit.labelSingular,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            TimeUnit.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(if (isPlural) option.labelPlural else option.labelSingular) },
                                    onClick = {
                                        onTimeUnitChange(option)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))

                Text("Color identificativo")
                Spacer(modifier = Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 36.dp),
                    modifier = Modifier.height(150.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(colorList) { color ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(color)
                                .clickable { onColorChange(color) }
                                .border(
                                    width = if (selectedColor == color) 3.dp else 0.dp,
                                    color = if (selectedColor == color) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                val dayInt = dayStr.toIntOrNull() ?: 0
                val priceFloat = priceStr.toFloatOrNull() ?: 0f

                Row(modifier = Modifier.fillMaxWidth()) {

                    if (editingId != null) {
                        TextButton(
                            onClick = {
                                // Llamamos al borrado por ID
                                payTrackViewModel.deleteSubscriptionById(editingId)
                                onDismiss()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) { Text("Eliminar") }
                    }

                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = onDismiss) { Text("Atrás") }
                    Spacer(modifier = Modifier.width(10.dp))

                    val freqInt = frequencyStr.toIntOrNull() ?: 1

                    Button(
                        onClick = {
                            // 1. Construimos el objeto aquí mismo
                            val newItem = PayTrackItem(
                                id = editingId ?: UUID.randomUUID()
                                    .toString(), // Mantiene el id si editamos
                                name = name,
                                frequency = freqInt,
                                timeUnit = timeUnit,
                                day = dayInt,
                                price = priceFloat,
                                color = selectedColor.toArgb()
                            )

                            // 2. Decidimos si actualizamos o creamos uno nuevo
                            if (editingId != null) {
                                payTrackViewModel.updateSubscription(newItem)
                            } else {
                                payTrackViewModel.addSubscription(newItem)
                            }

                            onDismiss()
                        },
                        enabled = name.isNotBlank() && freqInt > 0 && dayInt in 1..31 && priceFloat > 0f
                    ) { Text("Guardar") }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyCalendar(
    actualDate: LocalDate,
    paymentDays: List<Int>,
    onMonthChange: (LocalDate) -> Unit,
    onDayClick: (Int, Int) -> Unit
) {

    val currentLocale = LocalConfiguration.current.locales[0]

//    val monthDay = actualDate.dayOfMonth
//    val dayName = actualDate.dayOfWeek.getDisplayName(TextStyle.FULL, currentLocale)
    val month = actualDate.monthValue
    val monthName = actualDate.month.getDisplayName(TextStyle.FULL, currentLocale)
    val totalMonthDays = actualDate.lengthOfMonth()
    val year = actualDate.year

//    val maxMonthDays = YearMonth.of(year, month).lengthOfMonth()
    val firstMonthDay = LocalDate.of(year, month, 1)
    val numberWeekDay = firstMonthDay.dayOfWeek.value

    val realToday = LocalDate.now()
    val isCurrentMonthAndYear =
        actualDate.year == realToday.year && actualDate.monthValue == realToday.monthValue
    val todayDay = if (isCurrentMonthAndYear) realToday.dayOfMonth else null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        val weekDaysNameList = mutableListOf("L", "M", "X", "J", "V", "S", "D")

        for (i in 1 until numberWeekDay) {
            weekDaysNameList.add("")
        }

        for (i in 1..totalMonthDays) {
            weekDaysNameList.add(i.toString())
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onMonthChange(actualDate.minusMonths(1)) }
            ) {
                Icon(
                    Icons.Filled.KeyboardDoubleArrowLeft,
                    contentDescription = "Mes anterior"
                )
            }

            Text(
                text = "${monthName.replaceFirstChar { it.uppercase() }} $year",
                fontWeight = FontWeight.Bold
            )

            IconButton(
                onClick = { onMonthChange(actualDate.plusMonths(1)) }
            ) {
                Icon(
                    Icons.Filled.KeyboardDoubleArrowRight,
                    contentDescription = "Mes siguiente"
                )
            }

        }

        WeekDayName(
            onDayClick = { dayClicked ->
                onDayClick(dayClicked, totalMonthDays)
            },
            items = weekDaysNameList,
            paymentDays = paymentDays,
            todayDay = todayDay
        )
    }
}

@Composable
fun WeekDayName(
    items: List<String>,
    onDayClick: (Int) -> Unit,
    paymentDays: List<Int>,
    todayDay: Int? = null
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7)
    ) {
        items(items) { item ->

            val isNotDigit = item.all { it.isLetter() } || item.isEmpty()
            val dayNum = item.toIntOrNull()
            val hasPayment = dayNum != null && paymentDays.contains(dayNum)
            val isToday = dayNum != null && dayNum == todayDay

            val shape = if (isNotDigit) RectangleShape else CircleShape

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .padding(1.dp)
                    .aspectRatio(1f)
                    .background(
                        color = when {
                            isNotDigit -> Color.Transparent
                            hasPayment -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        shape = shape
                    )
                    .then(
                        if (isToday) Modifier.border(
                            3.dp,
                            MaterialTheme.colorScheme.secondary,
                            shape
                        ) else Modifier
                    )
                    .clickable(onClick = { onDayClick(item.toInt()) }, enabled = !isNotDigit)
            ) {
                Text(
                    text = item,
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = when {
                        isNotDigit -> MaterialTheme.colorScheme.onSurface
                        hasPayment -> MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.Bold
                )
            }

        }
    }
}

@Composable
fun DayDialog(
    displayedSubscriptions: List<PayTrackItem>,
    onDismiss: () -> Unit,
    selectedDay: Int,
    onEdit: (PayTrackItem) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Pagos del día $selectedDay", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                if (displayedSubscriptions.isEmpty()) {
                    Text("No hay pagos para este día.")
                } else {
                    Payments(
                        items = displayedSubscriptions,
                        onEdit = {
                            onEdit(it)
                            onDismiss()
                        },
                        modifier = Modifier
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar")
                }
            }
        }
    }
}

@Composable
fun ThemeSwitcher(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    // Envolvemos el interruptor en un Row para centrarlo en la pantalla

    Row(
        modifier = Modifier
            .width(120.dp) // Tamaño compacto para 2 iconos
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Solo dejamos las opciones de Claro (Sol) y Oscuro (Luna)
        val options = listOf(
            ThemeMode.LIGHT to Icons.Default.LightMode,
            ThemeMode.DARK to Icons.Default.DarkMode
        )

        options.forEach { (mode, icon) ->
            val isSelected = currentTheme == mode
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(50))
                    .clickable { onThemeSelected(mode) }
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = if (mode == ThemeMode.LIGHT) "Modo Claro" else "Modo Oscuro",
                    // El icono cambia de color si está seleccionado o no
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

}