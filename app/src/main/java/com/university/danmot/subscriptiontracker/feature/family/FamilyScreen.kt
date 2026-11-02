package com.university.danmot.subscriptiontracker.feature.family

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.university.danmot.subscriptiontracker.data.repository.AuthRepository
import com.university.danmot.subscriptiontracker.data.repository.FamilyRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyScreen() {
    val authRepository = remember { AuthRepository() }
    val familyRepository = remember { FamilyRepository() }
    val userId = authRepository.currentUserId ?: ""

    val family by familyRepository.observeUserFamily(userId).collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var familyNameInput by remember { mutableStateOf("") }
    var inviteCodeInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("👨‍👩‍👧 Семья") })
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (family == null) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Вы пока не состоите в семье",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Создать новую семью", style = MaterialTheme.typography.titleSmall)
                            OutlinedTextField(
                                value = familyNameInput,
                                onValueChange = { familyNameInput = it },
                                label = { Text("Название семьи") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    isLoading = true
                                    scope.launch {
                                        val res = familyRepository.createFamily(familyNameInput, userId)
                                        isLoading = false
                                        res.onFailure { e ->
                                            val msg = if (e.message?.contains("network", true) == true) "Нет подключения" else e.localizedMessage ?: "Ошибка"
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Создать семью")
                            }
                        }
                    }

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Присоединиться по коду", style = MaterialTheme.typography.titleSmall)
                            OutlinedTextField(
                                value = inviteCodeInput,
                                onValueChange = { inviteCodeInput = it },
                                label = { Text("Ввести 6-значный код приглашения") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    if (inviteCodeInput.isBlank()) return@Button
                                    isLoading = true
                                    scope.launch {
                                        val res = familyRepository.joinFamilyByCode(inviteCodeInput, userId)
                                        isLoading = false
                                        res.onFailure { e ->
                                            val msg = if (e.message?.contains("network", true) == true) "Нет подключения" else e.localizedMessage ?: "Неверный код"
                                            snackbarHostState.showSnackbar(msg)
                                        }
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Присоединиться")
                            }
                        }
                    }
                }
            } else {
                val currentFamily = family!!
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = currentFamily.name,
                                style = MaterialTheme.typography.headlineSmall
                            )
                            Text(
                                text = "Код приглашения: ${currentFamily.inviteCode}",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "Отправьте этот 6-значный код близким, чтобы делить подписки",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Text(
                        text = "Участники семьи (${currentFamily.members.size}):",
                        style = MaterialTheme.typography.titleMedium
                    )

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(currentFamily.members) { index, memberId ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val isMe = memberId == userId
                                    val isOwner = memberId == currentFamily.ownerId
                                    Text(
                                        text = "Участник #${index + 1}" +
                                                (if (isMe) " (Вы)" else "") +
                                                (if (isOwner) " 👑" else ""),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = memberId.take(8) + "...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = {
                            isLoading = true
                            scope.launch {
                                familyRepository.leaveFamily(currentFamily.familyId, userId)
                                isLoading = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Покинуть семью")
                    }
                }
            }
        }
    }
}