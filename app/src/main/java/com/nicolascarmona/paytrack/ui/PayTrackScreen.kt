package com.nicolascarmona.paytrack.ui

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nicolascarmona.paytrack.PayTrackItem
import com.nicolascarmona.paytrack.PayTrackViewModel
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayTrackScreen(
    payTrackViewModel: PayTrackViewModel = viewModel()
) {

    val subscriptionList by payTrackViewModel.subscriptions.collectAsStateWithLifecycle()
//    val totalMonth by payTrackViewModel.totalSpent.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dayStr by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }

    var showAddDialog by remember { mutableStateOf(false) }
    var showDayDialog by remember { mutableStateOf(false) }

    val closeAndClearDialog = {
        showAddDialog = false
        name = ""
        description = ""
        dayStr = ""
        priceStr = ""
    }

    var selectedDay by remember { mutableStateOf<Int?>(null) }

    val displayedSubscriptions = if (selectedDay != null) {
        subscriptionList.filter { it.day == selectedDay }
    } else {
        subscriptionList
    }

    Scaffold(
        // TÍTULO APP
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = {
                    Text("PayTrack", fontWeight = FontWeight.Bold)
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
                description = description,
                onDescriptionChange = { description = it },
                dayStr = dayStr,
                onDayStrChange = { dayStr = it },
                priceStr = priceStr,
                onPriceStrChange = { priceStr = it },
                onDismiss = closeAndClearDialog,
                payTrackViewModel = payTrackViewModel
            )
        }
        if (showDayDialog && selectedDay != null) {
            DayDialog(
                displayedSubscriptions = displayedSubscriptions,
                onDismiss = {
                    showDayDialog = false
                    selectedDay = null
                },
                selectedDay = selectedDay!!
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            MyCalendar(
                onDayClick = { dayClicked ->
                    selectedDay = dayClicked
                    showDayDialog = true
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            Payments(items = subscriptionList)
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun Payments(items: List<PayTrackItem>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(items, key = { it.id }) { item ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(item.name, fontWeight = FontWeight.Bold)
                        Text(
                            "Día de cobro: ${item.day}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Text(String.format("%-2f €", item.price), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun MyDialog(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    dayStr: String,
    onDayStrChange: (String) -> Unit,
    priceStr: String,
    onPriceStrChange: (String) -> Unit,
    onDismiss: () -> Unit,
    payTrackViewModel: PayTrackViewModel = viewModel()
) {
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

                Text("Descripción")
                OutlinedTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
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

                // Conversion para validaciones lógicas
                val dayInt = dayStr.toIntOrNull() ?: 0
                val priceFloat = priceStr.toFloatOrNull() ?: 0f

                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = onDismiss) { Text("Atrás") }
                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            payTrackViewModel.addSubscription(
                                name = name,
                                description = description,
                                day = dayInt,
                                price = priceFloat
                            )

                            onDismiss()
                        },
                        enabled = if (
                            name.isNotBlank() &&
                            description.isNotBlank() &&
                            dayInt in 1..31 &&
                            priceFloat > 0f
                        ) true else false
                    ) { Text("Guardar") }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyCalendar(
    onDayClick: (Int) -> Unit
) {
    var actualDate by remember { mutableStateOf(LocalDate.now()) }
//    val monthDay = actualDate.dayOfMonth
//    val dayName = actualDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val month = actualDate.monthValue
    val monthName = actualDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val totalMonthDays = actualDate.lengthOfMonth()
    val year = actualDate.year
    //
//    val maxMonthDays = YearMonth.of(year, month).lengthOfMonth()
    val firstMonthDay = LocalDate.of(year, month, 1)
    val numberWeekDay = firstMonthDay.dayOfWeek.value

//    var firstDayCalendar by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
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

        Text(
            text = "${monthName.replaceFirstChar { it.uppercase() }} $year",
            fontWeight = FontWeight.Bold
        )
        WeekDayName(onDayClick = onDayClick, items = weekDaysNameList)

        Row(modifier = Modifier.padding(8.dp)) {
            Spacer(modifier = Modifier.weight(1f))
            Button(onClick = { actualDate = actualDate.minusMonths(1) }) {
                Text("-")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(onClick = { actualDate = actualDate.plusMonths(1) }) {
                Text("+")
            }
        }


    }
}

@Composable
fun WeekDayName(items: List<String>, onDayClick: (Int) -> Unit) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7)
    ) {
        items(items) { item ->
            val isNotDigit = item.all { it.isLetter() } || item.isEmpty()
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(60.dp)
                    .background(
                        color = if (isNotDigit) Color.Transparent else Color.LightGray,
                        shape = if (isNotDigit) {
                            RectangleShape
                        } else {
                            CircleShape
                        }
                    )
                    .clickable(onClick = { onDayClick(item.toInt()) }, enabled = !isNotDigit)
            ) {
                Text(
                    item,
                    Modifier.padding(vertical = 8.dp),
                    color = if (isNotDigit) Color.Black else Color.White,
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
    selectedDay: Int) {
    Dialog(onDismissRequest = { }) {
        Card {
            Column(
                modifier = Modifier.background(Color.White).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Pagos del día $selectedDay", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(16.dp))

                if (displayedSubscriptions.isEmpty()) {
                    Text("No hay pagos para este día.")
                } else {
                    Payments(items = displayedSubscriptions)
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