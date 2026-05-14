package com.example.saniya

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.collectLatest
import com.example.saniya.ui.navigation.Screen
import com.example.saniya.ui.screens.PingHubScreen
import com.example.saniya.ui.screens.RouteDashboardScreen
import com.example.saniya.ui.screens.SafeReachAlertsScreen
import com.example.saniya.ui.screens.SelectYourRouteScreen
import com.example.saniya.ui.theme.SaniyaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SaniyaTheme {
                VidyaVahiniApp()
            }
        }
    }
}

@Composable
fun VidyaVahiniApp(transportViewModel: TransportViewModel = viewModel()) {
    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by transportViewModel.uiState.collectAsState()

    LaunchedEffect(transportViewModel) {
        transportViewModel.messages.collectLatest { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    NavHost(navController = navController, startDestination = Screen.SelectRoute.route) {
        composable(Screen.SelectRoute.route) {
            SelectYourRouteScreen(
                navController = navController,
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onInstitutionSelected = transportViewModel::selectInstitution,
                onQueryChanged = transportViewModel::updateRouteQuery,
                onClearFilters = transportViewModel::clearRouteFilters,
                onRouteSelected = transportViewModel::choosePlannerRoute,
                onConfirmSelection = transportViewModel::confirmPlannerSelection
            )
        }
        composable(Screen.RouteDashboard.route) {
            RouteDashboardScreen(
                navController = navController,
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onRefresh = transportViewModel::refreshDashboard
            )
        }
        composable(Screen.PingHub.route) {
            PingHubScreen(
                navController = navController,
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onQuickPing = transportViewModel::sendQuickPing,
                onReportBreakdown = transportViewModel::reportBreakdown
            )
        }
        composable(Screen.SafeReachAlerts.route) {
            SafeReachAlertsScreen(
                navController = navController,
                uiState = uiState,
                snackbarHostState = snackbarHostState,
                onNotifySafeArrival = transportViewModel::notifySafeArrival,
                onToggleContactEnabled = transportViewModel::toggleContactEnabled,
                onCallContact = transportViewModel::callContact
            )
        }
    }
}
