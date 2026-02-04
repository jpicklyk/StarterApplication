package com.example.starterapplication.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.starterapplication.feature.home.HomeScreen
import com.example.starterapplication.feature.license.LicenseScreen
import com.example.starterapplication.feature.policies.PolicyListScreen

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                onNavigateToLicense = {
                    navController.navigate(Screen.License.route)
                },
                onNavigateToPolicies = {
                    navController.navigate(Screen.Policies.route)
                }
            )
        }

        composable(route = Screen.License.route) {
            LicenseScreen()
        }

        composable(route = Screen.Policies.route) {
            PolicyListScreen(
                onPolicyClick = { policyKey ->
                    navController.navigate(Screen.PolicyDetail.createRoute(policyKey))
                },
                onNavigateToLicense = {
                    navController.navigate(Screen.License.route)
                }
            )
        }

        composable(route = Screen.PolicyDetail.route) { backStackEntry ->
            val policyKey = backStackEntry.arguments?.getString("policyKey") ?: ""
            // PolicyDetailScreen to be implemented
            PolicyDetailPlaceholder(policyKey)
        }
    }
}

@Composable
private fun PolicyDetailPlaceholder(policyKey: String) {
    Text(
        text = "Policy Detail: $policyKey",
        modifier = Modifier.padding(16.dp)
    )
}
