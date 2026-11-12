package com.university.danmot.subscriptiontracker.feature.stats

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.university.danmot.subscriptiontracker.domain.model.Subscription
import com.university.danmot.subscriptiontracker.feature.subscriptions.SubscriptionViewModel
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: SubscriptionViewModel = viewModel()
) {
    val subscriptions by viewModel.subscriptions.collectAsState()
    val currentFamily by viewModel.currentFamily.collectAsState()

    val membersCount = (currentFamily?.members?.size ?: 1).coerceAtLeast(1)

    // Считаем стоимость подписки персонально для текущего пользователя:
    // личная = 100% цены, семейная = цена / количество участников семьи
    fun userMonthlyCost(sub: Subscription): Double {
        return if (sub.familyId != null) {
            sub.monthlyCost() / membersCount
        } else {
            sub.monthlyCost()
        }
    }

    val totalMonthly = subscriptions.sumOf { userMonthlyCost(it) }
    val top3 = subscriptions.sortedByDescending { userMonthlyCost(it) }.take(3)
    val familySubs = subscriptions.filter { it.familyId != null }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("📊 Статистика расходов") })
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Общая сумма трат в месяц (с учетом доли в семейных)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Общая сумма трат в месяц", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = String.format(Locale.US, "%.2f RUB", totalMonthly),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // 2. Топ-3 самых дорогих подписки (по доле пользователя)
            item {
                Text("🏆 Топ-3 самых дорогих подписки", style = MaterialTheme.typography.titleMedium)
            }
            if (top3.isEmpty()) {
                item {
                    Text("Нет активных подписок", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                items(top3) { sub ->
                    val costForUser = userMonthlyCost(sub)
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val badge = if (sub.familyId != null) "👨‍👩‍👧 " else "👤 "
                            Text("$badge${sub.name}", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                String.format(Locale.US, "%.2f RUB/мес", costForUser),
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 3. Разбивка по подпискам с прогресс-барами (по доле пользователя)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("📈 Доля в ежемесячных расходах", style = MaterialTheme.typography.titleMedium)
            }
            items(subscriptions) { sub ->
                val costForUser = userMonthlyCost(sub)
                val fraction = if (totalMonthly > 0) (costForUser / totalMonthly).toFloat() else 0f
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(sub.name, style = MaterialTheme.typography.bodyMedium)
                        Text(
                            String.format(Locale.US, "%.0f%% (%.2f RUB)", fraction * 100, costForUser),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { fraction.coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                    )
                }
            }

            // 4. Семейные подписки: кто сколько платит (поровну)
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("👨‍👩‍👧 Семейные подписки (доля каждого)", style = MaterialTheme.typography.titleMedium)
            }
            if (familySubs.isEmpty()) {
                item {
                    Text(
                        "Семейных подписок пока нет",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(familySubs) { sub ->
                    val perMember = sub.monthlyCost() / membersCount
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(sub.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                text = String.format(
                                    Locale.US,
                                    "Всего: %.2f RUB/мес • Участников: %d • С каждого: %.2f RUB/мес",
                                    sub.monthlyCost(),
                                    membersCount,
                                    perMember
                                ),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}