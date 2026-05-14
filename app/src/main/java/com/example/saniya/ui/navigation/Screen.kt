package com.example.saniya.ui.navigation

sealed class Screen(val route: String) {
    object SelectRoute : Screen("select_route")
    object RouteDashboard : Screen("route_dashboard")
    object PingHub : Screen("ping_hub")
    object SafeReachAlerts : Screen("safe_reach_alerts")
}
