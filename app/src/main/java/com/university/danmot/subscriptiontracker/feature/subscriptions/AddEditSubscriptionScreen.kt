package com.university.danmot.subscriptiontracker.feature.subscriptions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.university.danmot.subscriptiontracker.domain.model.BillingCycle
import com.university.danmot.subscriptiontracker.domain.model.Subscription
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionScreen(
    subscriptionId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: SubscriptionViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var cycle by remember { mutableStateOf(BillingCycle.MONTHLY) }
    var isFormValid by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    // Состояние для календаря
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    // Если это редактирование, загружаем данные
    LaunchedEffect(subscriptionId) {
        if (subscriptionId != null) {
            viewModel.getSubscriptionById(subscriptionId).collect { subscription ->
                if (subscription != null) {
                    name = subscription.name
                    priceText = subscription.price.toString()
                    date = subscription.nextPaymentDate.toString()
                    cycle = subscription.cycle
                    isLoading = false
                }
            }
        } else {
            isLoading = false
        }
    }

    LaunchedEffect(name, priceText, date) {
        isFormValid = name.isNotBlank() && priceText.toDoubleOrNull() != null && date.isNotBlank()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (subscriptionId == null) "Новая подписка" else "Редактировать") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название (например, Яндекс Плюс)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Цена") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Период оплаты",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BillingCycle.entries.forEach { c ->
                        FilterChip(
                            selected = cycle == c,
                            onClick = { cycle = c },
                            label = { Text(c.label) }
                        )
                    }
                }

                // НОВОЕ: Поле даты с иконкой календаря
                OutlinedTextField(
                    value = date,
                    onValueChange = { }, // Поле только для чтения, дата выбирается через календарь
                    label = { Text("Дата следующей оплаты") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Выбрать дату"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                Button(
                    onClick = {
                        val price = priceText.toDoubleOrNull() ?: 0.0
                        if (subscriptionId == null) {
                            viewModel.addSubscription(name, price, cycle, date)
                        } else {
                            viewModel.updateSubscription(
                                Subscription(
                                    id = subscriptionId,
                                    name = name,
                                    price = price,
                                    currency = "RUB",
                                    cycle = cycle,
                                    nextPaymentDate = LocalDate.parse(date),
                                    ownerId = "me"
                                )
                            )
                        }
                        onNavigateBack()
                    },
                    enabled = isFormValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (subscriptionId == null) "Сохранить" else "Обновить")
                }
            }
        }
    }

    // НОВОЕ: Диалоговое окно календаря
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        // Конвертируем миллисекунды в LocalDate и форматируем в строку (ГГГГ-ММ-ДД)
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        date = localDate.toString()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Отмена")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}