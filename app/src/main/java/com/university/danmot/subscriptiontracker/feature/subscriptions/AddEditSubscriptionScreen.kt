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
    val currentFamily by viewModel.currentFamily.collectAsState()
    val currentUserId = viewModel.userId

    // Проверяем, является ли текущий пользователь главой семьи
    val isFamilyOwner = currentFamily != null && currentFamily?.ownerId == currentUserId

    var name by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var cycle by remember { mutableStateOf(BillingCycle.MONTHLY) }
    var isFamilySub by remember { mutableStateOf(false) }
    var existingOwnerId by remember { mutableStateOf("me") }
    var existingFamilyId by remember { mutableStateOf<String?>(null) }

    var isFormValid by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    LaunchedEffect(subscriptionId) {
        if (subscriptionId != null) {
            viewModel.getSubscriptionById(subscriptionId).collect { subscription ->
                if (subscription != null) {
                    name = subscription.name
                    priceText = subscription.price.toString()
                    date = subscription.nextPaymentDate.toString()
                    cycle = subscription.cycle
                    isFamilySub = subscription.familyId != null
                    existingOwnerId = subscription.ownerId
                    existingFamilyId = subscription.familyId
                    isLoading = false
                }
            }
        } else {
            isLoading = false
        }
    }

    // Если это редактирование уже существующей семейной подписки, но пользователь НЕ глава семьи — блокируем редактирование
    val canEditThisSubscription = if (subscriptionId != null && existingFamilyId != null) {
        isFamilyOwner
    } else {
        true
    }

    LaunchedEffect(name, priceText, date, canEditThisSubscription) {
        isFormValid = canEditThisSubscription &&
                name.isNotBlank() &&
                priceText.toDoubleOrNull() != null &&
                date.isNotBlank()
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
                if (!canEditThisSubscription) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Только глава семьи может редактировать семейные подписки.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { if (canEditThisSubscription) name = it },
                    readOnly = !canEditThisSubscription,
                    label = { Text("Название (например, Яндекс Плюс)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { if (canEditThisSubscription) priceText = it },
                    readOnly = !canEditThisSubscription,
                    label = { Text("Цена") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Тип подписки",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = !isFamilySub,
                        onClick = { if (canEditThisSubscription) isFamilySub = false },
                        enabled = canEditThisSubscription,
                        label = { Text("👤 Личная") }
                    )
                    FilterChip(
                        selected = isFamilySub,
                        onClick = {
                            if (isFamilyOwner && canEditThisSubscription) isFamilySub = true
                        },
                        enabled = isFamilyOwner && canEditThisSubscription,
                        label = {
                            val labelText = when {
                                currentFamily == null -> "👨‍👩‍👧 Семейная (нет семьи)"
                                !isFamilyOwner -> "👨‍👩‍👧 Семейная (только глава)"
                                else -> "👨‍👩‍👧 Семейная"
                            }
                            Text(labelText)
                        }
                    )
                }

                Text(
                    text = "Период оплаты",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BillingCycle.entries.forEach { c ->
                        FilterChip(
                            selected = cycle == c,
                            onClick = { if (canEditThisSubscription) cycle = c },
                            enabled = canEditThisSubscription,
                            label = { Text(c.label) }
                        )
                    }
                }

                OutlinedTextField(
                    value = date,
                    onValueChange = { },
                    label = { Text("Дата следующей оплаты") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(
                            onClick = { if (canEditThisSubscription) showDatePicker = true },
                            enabled = canEditThisSubscription
                        ) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Выбрать дату"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.weight(1f))

                if (canEditThisSubscription) {
                    Button(
                        onClick = {
                            val price = priceText.toDoubleOrNull() ?: 0.0
                            if (subscriptionId == null) {
                                viewModel.addSubscription(name, price, cycle, date, isFamilySub)
                            } else {
                                val targetFamilyId = if (isFamilySub) (existingFamilyId ?: currentFamily?.familyId) else null
                                viewModel.updateSubscription(
                                    Subscription(
                                        id = subscriptionId,
                                        name = name,
                                        price = price,
                                        currency = "RUB",
                                        cycle = cycle,
                                        nextPaymentDate = LocalDate.parse(date),
                                        ownerId = existingOwnerId,
                                        familyId = targetFamilyId
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
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
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