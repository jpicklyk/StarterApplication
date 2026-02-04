package com.example.starterapplication.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object License : Screen("license")
    data object Policies : Screen("policies")
    data object PolicyDetail : Screen("policies/{policyKey}") {
        fun createRoute(policyKey: String) = "policies/$policyKey"
    }
}
