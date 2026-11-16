package com.nicolascarmona.paytrack.ui

import android.annotation.SuppressLint
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
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

@SuppressLint("LocalContextGetResourceValueCall")
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
    var categoryStr by remember { mutableStateOf("General") }
    var isIncomeState by remember { mutableStateOf(false) }

    val subscriptionList by payTrackViewModel.subscriptions.collectAsStateWithLifecycle()
    val monthlyGoal by payTrackViewModel.monthlySavingsGoal.collectAsStateWithLifecycle()

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
    var showMonthlyGoalDialog by remember { mutableStateOf(false) }
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
        categoryStr = "General"
        isIncomeState = false
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
        categoryStr = item.category
        isIncomeState = item.isIncome
        showAddDialog = true
    }

    val realToday = LocalDate.now()
    val isCurrentMonth =
        actualDate.year == realToday.year && actualDate.monthValue == realToday.monthValue

    val context = LocalContext.current
    var showBackupMenu by remember { mutableStateOf(false) }

    val createBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let {
            payTrackViewModel.exportBackupJson { jsonString ->
                try {
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(jsonString.toByteArray())
                    }
                    Toast.makeText(
                        context,
                        context.getString(R.string.backup_exported_ok),
                        Toast.LENGTH_SHORT
                    ).show()
                } catch (e: Exception) {
                    Toast.makeText(
                        context,
                        context.getString(R.string.backup_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    val restoreBackupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                val jsonString =
                    context.contentResolver.openInputStream(it)?.bufferedReader()?.use { reader ->
                        reader.readText()
                    }
                if (!jsonString.isNullOrBlank()) {
                    payTrackViewModel.importBackupJson(
                        jsonString = jsonString,
                        onSuccess = {
                            Toast.makeText(
                                context,
                                context.getString(R.string.backup_restored_ok),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onError = {
                            Toast.makeText(
                                context,
                                context.getString(R.string.backup_error),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    context.getString(R.string.backup_error),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

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
                    Box {
                        IconButton(onClick = { showBackupMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = stringResource(R.string.backup_menu_description)
                            )
                        }
                        DropdownMenu(
                            expanded = showBackupMenu,
                            onDismissRequest = { showBackupMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.savings_goal_menu)) },
                                onClick = {
                                    showBackupMenu = false
                                    showMonthlyGoalDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.export_backup)) },
                                onClick = {
                                    showBackupMenu = false
                                    val fileName = "PayTrack_Backup_${LocalDate.now()}.json"
                                    createBackupLauncher.launch(fileName)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.import_backup)) },
                                onClick = {
                                    showBackupMenu = false
                                    restoreBackupLauncher.launch(arrayOf("application/json"))
                                }
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    closeAndClearDialog()
                    showAddDialog = true
                }
            ) {
                Icon(
                    Icons.Filled.Add,
                    contentDescription = stringResource(R.string.add_description)
                )
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
                initialSpecificMonths = specificMonthsStr,
                initialCategory = categoryStr,
                initialIsIncome = isIncomeState
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
        if (showMonthlyGoalDialog) {
            MonthlyGoalDialog(
                currentGoal = monthlyGoal,
                onDismiss = { showMonthlyGoalDialog = false },
                onSave = { newGoal -> payTrackViewModel.setMonthlySavingsGoal(newGoal) }
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
                monthlyGoal = monthlyGoal,
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
    monthlyGoal: Float = 0f,
    modifier: Modifier,
    showCategoryFilter: Boolean = true
) {
    val allLabel = stringResource(R.string.all_categories)
    var selectedCategoryFilter by remember { mutableStateOf(allLabel) }

    val availableCategories = remember(items, allLabel) {
        listOf(allLabel) + items.map { it.category }.distinct().sorted()
    }

    val activeFilter = if (availableCategories.contains(selectedCategoryFilter)) {
        selectedCategoryFilter
    } else {
        allLabel
    }

    val filteredItems = remember(items, activeFilter, allLabel) {
        if (activeFilter == allLabel) items
        else items.filter { it.category == activeFilter }
    }

    val sortedItems = filteredItems.sortedWith(compareBy({ it.day }, { it.price }))
    val totalIncome = filteredItems.filter { it.isIncome }.sumOf { it.price.toDouble() }.toFloat()
    val totalExpense = filteredItems.filter { !it.isIncome }.sumOf { it.price.toDouble() }.toFloat()
    val netBalance = totalIncome - totalExpense
    val priceFormat = stringResource(R.string.price_format)

    Column {
        if (showCategoryFilter && availableCategories.size > 2) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableCategories.forEach { categoryName ->
                    val isSelected = activeFilter == categoryName
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { selectedCategoryFilter = categoryName }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = categoryName,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Ingresos vs Gastos
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.income_total, String.format(priceFormat, totalIncome)),
                color = Color(0xFF4CAF50),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = stringResource(R.string.expense_total, String.format(priceFormat, totalExpense)),
                color = Color(0xFFE53935),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (isCurrentMonth) {
            val paidExpenses = filteredItems.filter { !it.isIncome && it.day <= currentDay }
                .sumOf { it.price.toDouble() }.toFloat()
            val remainingExpenses = totalExpense - paidExpenses

            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.paid_amount, String.format(priceFormat, paidExpenses)),
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = stringResource(R.string.remaining_amount, String.format(priceFormat, remainingExpenses)),
                    color = Color(0xFFE53935),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.net_balance, String.format(priceFormat, netBalance)),
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // --- BARRA DE OBJETIVO DE AHORRO MENSUAL ---
        if (showCategoryFilter && monthlyGoal > 0f && activeFilter == allLabel) {
            val progress = (netBalance / monthlyGoal).coerceIn(0f, 1f)
            val isGoalMet = netBalance >= monthlyGoal
            val progressColor = if (isGoalMet) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary

            Column(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(
                            R.string.savings_goal_progress,
                            String.format(priceFormat, netBalance),
                            String.format(priceFormat, monthlyGoal)
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = if (isGoalMet) {
                            stringResource(
                                R.string.savings_goal_achieved,
                                String.format(priceFormat, netBalance - monthlyGoal)
                            )
                        } else {
                            stringResource(
                                R.string.savings_goal_remaining,
                                String.format(priceFormat, monthlyGoal - netBalance)
                            )
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = progressColor,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(50)),
                    color = progressColor
                )
            }
        }

        LazyColumn(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sortedItems, key = { it.id }) { item ->
                val composeColor = Color(item.color)
                val isLightBackground = composeColor.luminance() > 0.5f
                val textColor = if (isLightBackground) Color.Black else Color.White

                val repeatText = when {
                    item.specificMonths == "UNIQUE" -> stringResource(R.string.unique)
                    item.specificMonths.isNotEmpty() -> stringResource(R.string.specific)
                    item.frequency == 1 && item.timeUnit == TimeUnit.MONTH -> stringResource(R.string.monthly)
                    item.frequency == 1 -> stringResource(R.string.every_singular, item.timeUnit.labelSingular)
                    else -> stringResource(R.string.every_plural, item.frequency, item.timeUnit.labelPlural)
                }.lowercase()

                val formattedAmount = if (item.isIncome) {
                    "+${String.format(priceFormat, item.price)}"
                } else {
                    String.format(priceFormat, item.price)
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
                                text = stringResource(
                                    R.string.days_repeat,
                                    item.day,
                                    repeatText,
                                    item.category
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = textColor.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Text(
                            text = formattedAmount,
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

@Composable
fun MonthlyGoalDialog(
    currentGoal: Float,
    onDismiss: () -> Unit,
    onSave: (Float) -> Unit
) {
    var goalStr by remember {
        mutableStateOf(if (currentGoal > 0f) currentGoal.toString() else "")
    }
    val goalFloat = goalStr.toFloatOrNull() ?: 0f

    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.savings_goal_dialog_title),
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = goalStr,
                    onValueChange = { goalStr = it.replace(',', '.') },
                    placeholder = { Text(stringResource(R.string.savings_goal_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (currentGoal > 0f) {
                        TextButton(
                            onClick = {
                                onSave(0f)
                                onDismiss()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
                        ) {
                            Text(stringResource(R.string.savings_goal_disable))
                        }
                    } else {
                        Spacer(modifier = Modifier.width(1.dp))
                    }

                    Button(
                        onClick = {
                            onSave(goalFloat)
                            onDismiss()
                        },
                        enabled = goalFloat > 0f
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = stringResource(R.string.save_description)
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
    initialSpecificMonths: String,
    initialCategory: String = "General",
    initialIsIncome: Boolean = false
) {
    val categoryList by payTrackViewModel.categories.collectAsStateWithLifecycle()
    var selectedCategory by remember { mutableStateOf(initialCategory.ifEmpty { "General" }) }
    var isIncome by remember { mutableStateOf(initialIsIncome) }
    var showManageCategoriesDialog by remember { mutableStateOf(false) }

    val monthsList = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12")

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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val types = listOf(
                            false to stringResource(R.string.expense_type),
                            true to stringResource(R.string.income_type)
                        )
                        types.forEach { (incomeFlag, label) ->
                            val isSelected = isIncome == incomeFlag
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { isIncome = incomeFlag }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            stringResource(R.string.category_label),
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = { showManageCategoriesDialog = true }) {
                            Text(stringResource(R.string.manage_categories))
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryList.forEach { cat ->
                            val isSelected = selectedCategory == cat.name
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { selectedCategory = cat.name }
                                    .padding(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Text(
                                    text = cat.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Text(
                        stringResource(R.string.payment_frequency),
                        fontWeight = FontWeight.SemiBold
                    )

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
                        Text(
                            stringResource(R.string.select_months),
                            fontWeight = FontWeight.SemiBold
                        )

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

                    Text(
                        stringResource(R.string.identifying_color),
                        fontWeight = FontWeight.SemiBold
                    )

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
                                specificMonths = specificMonthsString,
                                category = selectedCategory,
                                isIncome = isIncome
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
                        Icon(
                            imageVector = Icons.Filled.Save,
                            contentDescription = stringResource(R.string.save_description)
                        )
                    }
                }
            }
        }
    }

    if (showManageCategoriesDialog) {
        var newCategoryName by remember { mutableStateOf("") }

        Dialog(onDismissRequest = { showManageCategoriesDialog = false }) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(stringResource(R.string.manage_categories), fontWeight = FontWeight.Bold)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            placeholder = { Text(stringResource(R.string.new_category_hint)) },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = {
                                payTrackViewModel.addCategory(newCategoryName)
                                selectedCategory = newCategoryName.trim()
                                newCategoryName = ""
                            },
                            enabled = newCategoryName.isNotBlank()
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = stringResource(R.string.add_description)
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        categoryList.forEach { cat ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat.name)
                                if (cat.name != "General") {
                                    IconButton(
                                        onClick = {
                                            if (selectedCategory == cat.name) selectedCategory = "General"
                                            payTrackViewModel.deleteCategory(cat)
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteForever,
                                            contentDescription = stringResource(R.string.delete_description),
                                            tint = Color.Red
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { showManageCategoriesDialog = false },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text(stringResource(R.string.close_button))
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
                        modifier = Modifier,
                        showCategoryFilter = false
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
                        Icon(
                            Icons.Filled.Add,
                            contentDescription = stringResource(R.string.add_description)
                        )
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
            val modeDescription =
                if (mode == ThemeMode.LIGHT) stringResource(R.string.light_mode) else stringResource(
                    R.string.dark_mode
                )

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

    if (specificMonths == "UNIQUE") {
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