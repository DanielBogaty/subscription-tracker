package com.university.danmot.subscriptiontracker.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.university.danmot.subscriptiontracker.feature.subscriptions.AddEditSubscriptionScreen
import com.university.danmot.subscriptiontracker.feature.subscriptions.SubscriptionDetailScreen
import com.university.danmot.subscriptiontracker.feature.subscriptions.SubscriptionsListScreen

object Routes {
    const val SUBSCRIPTIONS = "subscriptions"
    const val ADD_SUBSCRIPTION = "add_subscription"
    const val SUBSCRIPTION_DETAIL = "subscription_detail/{subscriptionId}"
    const val EDIT_SUBSCRIPTION = "edit_subscription/{subscriptionId}"

    fun subscriptionDetail(id: Long) = "subscription_detail/$id"
    fun editSubscription(id: Long) = "edit_subscription/$id"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.SUBSCRIPTIONS
    ) {
        composable(Routes.SUBSCRIPTIONS) {
            SubscriptionsListScreen(
                onNavigateToAdd = { navController.navigate(Routes.ADD_SUBSCRIPTION) },
                onNavigateToDetail = { id -> navController.navigate(Routes.subscriptionDetail(id)) }
            )
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