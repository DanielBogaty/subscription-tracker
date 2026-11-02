package com.university.danmot.subscriptiontracker.core.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.university.danmot.subscriptiontracker.data.repository.AuthRepository
import com.university.danmot.subscriptiontracker.feature.auth.LoginScreen
import com.university.danmot.subscriptiontracker.feature.auth.RegisterScreen
import com.university.danmot.subscriptiontracker.feature.family.FamilyScreen
import com.university.danmot.subscriptiontracker.feature.stats.StatsScreen
import com.university.danmot.subscriptiontracker.feature.subscriptions.AddEditSubscriptionScreen
import com.university.danmot.subscriptiontracker.feature.subscriptions.SubscriptionDetailScreen
import com.university.danmot.subscriptiontracker.feature.subscriptions.SubscriptionsListScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val SUBSCRIPTIONS = "subscriptions"
    const val FAMILY = "family"
    const val STATS = "stats"
    const val ADD_SUBSCRIPTION = "add_subscription"
    const val SUBSCRIPTION_DETAIL = "subscription_detail/{subscriptionId}"
    const val EDIT_SUBSCRIPTION = "edit_subscription/{subscriptionId}"

    fun subscriptionDetail(id: Long) = "subscription_detail/$id"
    fun editSubscription(id: Long) = "edit_subscription/$id"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    val authRepository = remember { AuthRepository() }
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomBarRoutes = listOf(Routes.SUBSCRIPTIONS, Routes.FAMILY, Routes.STATS)
    val showBottomBar = currentRoute in bottomBarRoutes

    val startDestination = if (authRepository.isLoggedIn) Routes.SUBSCRIPTIONS else Routes.LOGIN

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    NavigationBarItem(
                        selected = currentRoute == Routes.SUBSCRIPTIONS,
                        onClick = {
                            navController.navigate(Routes.SUBSCRIPTIONS) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text("📋") },
                        label = { Text("Подписки") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.FAMILY,
                        onClick = {
                            navController.navigate(Routes.FAMILY) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text("👨‍👩‍👧") },
                        label = { Text("Семья") }
                    )
                    NavigationBarItem(
                        selected = currentRoute == Routes.STATS,
                        onClick = {
                            navController.navigate(Routes.STATS) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text("📊") },
                        label = { Text("Статистика") }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Routes.SUBSCRIPTIONS) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateToRegister = {
                        navController.navigate(Routes.REGISTER)
                    }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        navController.navigate(Routes.SUBSCRIPTIONS) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }

            composable(Routes.SUBSCRIPTIONS) {
                SubscriptionsListScreen(
                    onNavigateToAdd = { navController.navigate(Routes.ADD_SUBSCRIPTION) },
                    onNavigateToDetail = { id -> navController.navigate(Routes.subscriptionDetail(id)) },
                    onLogout = {
                        authRepository.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.FAMILY) {
                FamilyScreen()
            }

            composable(Routes.STATS) {
                StatsScreen()
            }

            composable(Routes.ADD_SUBSCRIPTION) {
                AddEditSubscriptionScreen(
                    subscriptionId = null,
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(
                route = Routes.SUBSCRIPTION_DETAIL,
                arguments = listOf(navArgument("subscriptionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val subscriptionId = backStackEntry.arguments?.getLong("subscriptionId") ?: return@composable
                SubscriptionDetailScreen(
                    subscriptionId = subscriptionId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToEdit = { id -> navController.navigate(Routes.editSubscription(id)) }
                )
            }

            composable(
                route = Routes.EDIT_SUBSCRIPTION,
                arguments = listOf(navArgument("subscriptionId") { type = NavType.LongType })
            ) { backStackEntry ->
                val subscriptionId = backStackEntry.arguments?.getLong("subscriptionId") ?: return@composable
                AddEditSubscriptionScreen(
                    subscriptionId = subscriptionId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}