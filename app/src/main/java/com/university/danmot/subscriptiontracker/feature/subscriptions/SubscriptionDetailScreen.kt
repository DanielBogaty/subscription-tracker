package com.university.danmot.subscriptiontracker.feature.subscriptions

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailScreen(
    subscriptionId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: SubscriptionViewModel = viewModel()
) {
    val subscription by viewModel.getSubscriptionById(subscriptionId).collectAsState()
    val priceHistory by viewModel.getPriceHistory(subscriptionId).collectAsState()
    val currentFamily by viewModel.currentFamily.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    // Проверяем права: личную подписку может менять сам владелец, а семейную — только глава семьи
    val canModify = remember(subscription, currentFamily, viewModel.userId) {
        val sub = subscription
        if (sub == null) {
            false
        } else if (sub.familyId != null) {
            currentFamily?.ownerId == viewModel.userId
        } else {
            true
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Подписка") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                actions = {
                    if (canModify) {
                        IconButton(onClick = { onNavigateToEdit(subscriptionId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Редактировать")
                        }
                        IconButton(onClick = { showDeleteDialog = true }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Удалить",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (subscription == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoRow(label = "Название", value = subscription!!.name)

                            val currentPrice = String.format(Locale.US, "%.2f", subscription!!.price)
                            InfoRow(label = "Текущая цена", value = "$currentPrice ${subscription!!.currency}")

                            InfoRow(label = "Период", value = subscription!!.cycle.label)
                            InfoRow(label = "Следующая оплата", value = subscription!!.nextPaymentDate.toString())
                        }
                    }
                }

                item {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("История изменений цены", style = MaterialTheme.typography.titleMedium)
                    }
                }

                if (priceHistory.isEmpty()) {
                    item {
                        Text(
                            "Изменений цены пока не было. WorkManager проверит цены в фоновом режиме.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(priceHistory) { historyItem ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = historyItem.date.toString(),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                val historyPrice = String.format(Locale.US, "%.2f", historyItem.price)
                                Text(
                                    text = "$historyPrice ₽",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    if (canModify) {
                        Button(
                            onClick = { onNavigateToEdit(subscriptionId) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Редактировать")
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "👨‍👩‍👧 Это семейная подписка. Изменять или удалять её может только глава семьи.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog && canModify) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Удалить подписку?") },
            text = { Text("Подписка \"${subscription?.name}\" будет удалена без возможности восстановления.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        subscription?.let { viewModel.deleteSubscription(it) }
                        showDeleteDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Удалить") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.titleMedium)
    }
}