package com.nicolascarmona.paytrack.ui

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nicolascarmona.paytrack.PayTrackItem
import com.nicolascarmona.paytrack.PayTrackViewModel
import java.time.LocalDate
import java.util.Locale
import java.time.format.TextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PayTrackScreen(
    payTrackViewModel: PayTrackViewModel = viewModel()
) {

    val subscriptionList by payTrackViewModel.subscriptions.collectAsStateWithLifecycle()
    val totalMonth by payTrackViewModel.totalSpent.collectAsStateWithLifecycle()

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dayStr by remember { mutableStateOf("") }
    var priceStr by remember { mutableStateOf("") }

    var showDialog by remember { mutableStateOf(false) }

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
                    showDialog = true
                }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Añadir")
            }
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { innerPadding ->
        if (showDialog) {
            Dialog(onDismissRequest = {
                name = ""
                description = ""
                dayStr = ""
                priceStr = ""

                showDialog = false
            }) {
                Card {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Nombre")
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Descripción")
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Día")
                        OutlinedTextField(
                            value = dayStr,
                            onValueChange = {
                                if (it.all { it.isDigit() }) {
                                    dayStr = it
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Text("Precio")
                        OutlinedTextField(
                            value = priceStr,
                            onValueChange = { priceStr = it.replace(',', '.') },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Conversion para validaciones lógicas
                        val dayInt = dayStr.toIntOrNull() ?: 0
                        val priceFloat = priceStr.toFloatOrNull() ?: 0f

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.weight(1f))
                            Button(onClick = {
                                name = ""
                                description = ""
                                dayStr = ""
                                priceStr = ""

                                showDialog = false
                            }) { Text("Atrás") }
                            Spacer(modifier = Modifier.width(10.dp))

                            Button(
                                onClick = {
                                    payTrackViewModel.addSubscription(
                                        name = name,
                                        description = description,
                                        day = dayInt,
                                        price = priceFloat
                                    )

                                    showDialog = false
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Calendar(totalMonth = totalMonth)

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

@SuppressLint("DefaultLocale")
@Composable
fun Calendar(totalMonth: Float) {
    val datePickerState = rememberDatePickerState()

    Column(
        Modifier.padding(bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = Color(0x8FC02626)
            ),
            showModeToggle = false
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = String.format("Total a pagar cada mes: %.2f €", totalMonth),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleMedium
        )

    }

}

