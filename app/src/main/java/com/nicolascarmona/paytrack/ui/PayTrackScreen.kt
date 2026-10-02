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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.KeyboardDoubleArrowLeft
import androidx.compose.material.icons.filled.KeyboardDoubleArrowRight
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nicolascarmona.paytrack.R
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
    var specificMonthsStr by remember { mutableStateOf("") }
    var startMonth by remember { mutableIntStateOf(LocalDate.now().monthValue) }
    var startYear by remember { mutableIntStateOf(LocalDate.now().year) }

    val subscriptionList by payTrackViewModel.subscriptions.collectAsStateWithLifecycle()
    var actualDate by remember { mutableStateOf(LocalDate.now()) }
    val currentMaxMonthDays = actualDate.lengthOfMonth()

    val effectiveSubscriptionList = remember(subscriptionList, actualDate) {
        subscriptionList.filter { item ->
            item.shouldShowIn(actualDate.monthValue, actualDate.year)
        }.map { item ->
            item.copy(day = minOf(item.day, currentMaxMonthDays))
        }
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
        Color(0xFFFFEB3B), Color(0xFFFFC107), Color(0xFFFF9800), Color(0xFFFF5722),
        Color(0xFFFFFFFF), Color(0xFF000000)
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
        startMonth = LocalDate.now().monthValue
        startYear = LocalDate.now().year
        specificMonthsStr = ""
    }

    val openEditDialog = { item: PayTrackItem ->
        editingId = item.id
        name = item.name
        frequencyStr = item.frequency.toString()
        timeUnit = item.timeUnit
        dayStr = item.day.toString()
        priceStr = item.price.toString()
        selectedColor = Color(item.color)
        startMonth = item.startMonth
        startYear = item.startYear
        specificMonthsStr = item.specificMonths
        showAddDialog = true
    }

    val realToday = LocalDate.now()
    val isCurrentMonth = actualDate.year == realToday.year && actualDate.monthValue == realToday.monthValue

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = {
                    Text(stringResource(R.string.app_title), fontWeight = FontWeight.Bold)
                },
                actions = {
                    ThemeSwitcher(currentTheme = currentTheme, onThemeSelected = onThemeChange)
                    Spacer(modifier = Modifier.width(16.dp))
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_description))
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
                editingId = editingId,
                startMonth = startMonth,
                startYear = startYear,
                initialSpecificMonths = specificMonthsStr
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
                onEdit = openEditDialog,
                onAddClick = { day ->
                    closeAndClearDialog()
                    dayStr = day.toString()
                    showDayDialog = false
                    selectedDay = null
                    showAddDialog = true
                }
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
                onDayClick = { dayClicked, _ ->
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
    val priceFormat = stringResource(R.string.price_format)

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
                    text = stringResource(R.string.paid_amount, String.format(priceFormat, paidAmount)),
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = stringResource(R.string.remaining_amount, String.format(priceFormat, remainingAmount)),
                    color = Color(0xFFE53935),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.total_payment, String.format(priceFormat, totalPayments)),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedItems, key = { it.id }) { item ->
                val composeColor = Color(item.color)
                val isLightBackground = composeColor.luminance() > 0.5f
                val textColor = if (isLightBackground) Color.Black else Color.White

                val repeatText = if (item.frequency == 1) {
                    stringResource(R.string.every_singular, item.timeUnit.labelSingular.lowercase())
                } else {
                    stringResource(R.string.every_plural, item.frequency, item.timeUnit.labelPlural.lowercase())
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onEdit(item) },
                    colors = CardDefaults.cardColors(containerColor = composeColor)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = item.name,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = stringResource(R.string.day_repeat, item.day, repeatText),
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = String.format(priceFormat, item.price),
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            maxLines = 1
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
    editingId: String?,
    startMonth: Int,
    startYear: Int,
    initialSpecificMonths: String
) {
    val monthsList = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")

    // Obtenemos los strings para el control segmentado
    val strMonthly = stringResource(R.string.monthly)
    val strUnique = stringResource(R.string.unique)
    val strSpecific = stringResource(R.string.specific)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            var paymentType by remember {
                mutableStateOf(
                    when {
                        initialSpecificMonths == "UNIQUE" -> strUnique
                        initialSpecificMonths.isEmpty() -> strMonthly
                        else -> strSpecific
                    }
                )
            }

            var selectedMonths by remember {
                mutableStateOf(
                    if (initialSpecificMonths.isEmpty() || initialSpecificMonths == "UNIQUE") emptySet()
                    else initialSpecificMonths.split(",").mapNotNull { it.toIntOrNull() }.toSet()
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(weight = 1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.name_label), fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = name,
                        onValueChange = onNameChange,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(stringResource(R.string.day_label), fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = dayStr,
                        onValueChange = {
                            if (it.all { char -> char.isDigit() }) onDayStrChange(it)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(stringResource(R.string.price_label), fontWeight = FontWeight.SemiBold)
                    OutlinedTextField(
                        value = priceStr,
                        onValueChange = { onPriceStrChange(it.replace(',', '.')) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(stringResource(R.string.payment_frequency), fontWeight = FontWeight.SemiBold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val opciones = listOf(strMonthly, strUnique, strSpecific)

                        opciones.forEach { opcion ->
                            val isSelected = paymentType == opcion
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { paymentType = opcion }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = opcion,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    if (paymentType == strSpecific) {
                        Text(stringResource(R.string.select_months), fontWeight = FontWeight.SemiBold)

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            monthsList.chunked(6).forEach { rowMonths ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    rowMonths.forEach { monthText ->
                                        val monthNumber = monthText.toInt()
                                        val isSelected = selectedMonths.contains(monthNumber)

                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.background)
                                                .clickable {
                                                    selectedMonths = if (isSelected) {
                                                        selectedMonths - monthNumber
                                                    } else {
                                                        selectedMonths + monthNumber
                                                    }
                                                }
                                                .border(
                                                    width = if (isSelected) 3.dp else 0.dp,
                                                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                        ) {
                                            Text(
                                                text = monthText,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text(stringResource(R.string.identifying_color), fontWeight = FontWeight.SemiBold)

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        colorList.chunked(6).forEach { rowColors ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                rowColors.forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
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
                                repeat(6 - rowColors.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                }

                Spacer(modifier = Modifier.height(10.dp))

                val dayInt = dayStr.toIntOrNull() ?: 0
                val priceFloat = priceStr.toFloatOrNull() ?: 0f

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_description)
                        )
                    }

                    if (editingId != null) {
                        TextButton(
                            onClick = {
                                payTrackViewModel.deleteSubscriptionById(editingId)
                                onDismiss()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.DeleteForever,
                                contentDescription = stringResource(R.string.delete_description)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            val specificMonthsString = when (paymentType) {
                                strUnique -> "UNIQUE"
                                strMonthly -> ""
                                else -> selectedMonths.joinToString(",")
                            }

                            val newItem = PayTrackItem(
                                id = editingId ?: UUID.randomUUID().toString(),
                                name = name,
                                day = dayInt,
                                price = priceFloat,
                                color = selectedColor.toArgb(),
                                startMonth = startMonth,
                                startYear = startYear,
                                frequency = 1,
                                timeUnit = TimeUnit.MONTH,
                                specificMonths = specificMonthsString
                            )

                            if (editingId != null) {
                                payTrackViewModel.updateSubscription(newItem)
                            } else {
                                payTrackViewModel.addSubscription(newItem)
                            }

                            onDismiss()
                        },
                        enabled = name.isNotBlank() && dayInt in 1..31 && priceFloat > 0f && (paymentType != strSpecific || selectedMonths.isNotEmpty())
                    ) {
                        Icon(imageVector = Icons.Filled.Save, contentDescription = stringResource(R.string.save_description))
                    }
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

    val month = actualDate.monthValue
    val monthName = actualDate.month.getDisplayName(TextStyle.FULL, currentLocale)
    val totalMonthDays = actualDate.lengthOfMonth()
    val year = actualDate.year

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
        val weekDaysNameList = mutableListOf(
            stringResource(R.string.day_l),
            stringResource(R.string.day_m),
            stringResource(R.string.day_x),
            stringResource(R.string.day_j),
            stringResource(R.string.day_v),
            stringResource(R.string.day_s),
            stringResource(R.string.day_d)
        )

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
                    contentDescription = stringResource(R.string.previous_month)
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
                    contentDescription = stringResource(R.string.next_month)
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
    onEdit: (PayTrackItem) -> Unit,
    onAddClick: (Int) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.payments_for_day, selectedDay),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (displayedSubscriptions.isEmpty()) {
                    Text(stringResource(R.string.no_payments_for_this_day))
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Button(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back_description)
                        )
                    }
                    Button(onClick = { onAddClick(selectedDay) }) {
                        Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.add_description))
                    }
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
    Row(
        modifier = Modifier
            .width(120.dp)
            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(50)),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        val options = listOf(
            ThemeMode.LIGHT to Icons.Default.LightMode,
            ThemeMode.DARK to Icons.Default.DarkMode
        )

        options.forEach { (mode, icon) ->
            val isSelected = currentTheme == mode
            val modeDescription = if (mode == ThemeMode.LIGHT) stringResource(R.string.light_mode) else stringResource(R.string.dark_mode)

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
                    contentDescription = modeDescription,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun PayTrackItem.shouldShowIn(targetMonth: Int, targetYear: Int): Boolean {
    val startAbsoluteMonths = (startYear * 12) + startMonth
    val targetAbsoluteMonths = (targetYear * 12) + targetMonth

    if (targetAbsoluteMonths < startAbsoluteMonths) return false

    if (specificMonths == "UNIQUE"){
        return targetAbsoluteMonths == startAbsoluteMonths
    }

    if (specificMonths.isNotEmpty()) {
        val mesesElegidos = specificMonths.split(",").mapNotNull { it.toIntOrNull() }
        return mesesElegidos.contains(targetMonth)
    }

    val monthsBetween = targetAbsoluteMonths - startAbsoluteMonths

    val cycleInMonths = when (timeUnit) {
        TimeUnit.MONTH -> frequency
        TimeUnit.YEAR -> frequency * 12
    }

    return monthsBetween % cycleInMonths == 0
}