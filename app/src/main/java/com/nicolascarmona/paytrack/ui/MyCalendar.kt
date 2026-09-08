package com.nicolascarmona.paytrack.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyCalendar() {
    var actualDate by remember { mutableStateOf(LocalDate.now()) }
    val monthDay = actualDate.dayOfMonth
    val dayName = actualDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val month = actualDate.monthValue
    val monthName = actualDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    val totalMonthDays = actualDate.lengthOfMonth()
    val year = actualDate.year
    //
    val maxMonthDays = YearMonth.of(year, month).lengthOfMonth()
    val firstMonthDay = LocalDate.of(year, month, 1)
    val numeroDiaSemana = firstMonthDay.dayOfWeek.value

    var firstDayCalendar by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {


        val weekDaysNameList = mutableListOf("L", "M", "X", "J", "V", "S", "D")

        // 2 porque es calendario inglés
        for (i in 2 until numeroDiaSemana) {
            weekDaysNameList.add("")
        }

        weekDaysNameList += firstDayCalendar
        for (i in 1..totalMonthDays) {
            weekDaysNameList += i.toString()
        }
        Text(text = monthName.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.Bold)
        WeekDayName(weekDaysNameList)

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
fun WeekDayName(items: List<String>) {
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
                        color = if (isNotDigit) Color.Transparent else Color.Blue,
                        shape = if (isNotDigit) {
                            RectangleShape
                        } else {
                            CircleShape
                        }
                    )
                    .clickable(enabled = !(isNotDigit)) {}
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