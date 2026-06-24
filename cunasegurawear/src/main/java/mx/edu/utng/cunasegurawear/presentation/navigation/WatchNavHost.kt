package mx.edu.utng.cunasegurawear.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import mx.edu.utng.cunasegurawear.domain.model.AlertPhase
import mx.edu.utng.cunasegurawear.presentation.screens.AlertActiveScreen
import mx.edu.utng.cunasegurawear.presentation.screens.ConfigScreen
import mx.edu.utng.cunasegurawear.presentation.screens.CountdownScreen
import mx.edu.utng.cunasegurawear.presentation.screens.LifeCheckScreen
import mx.edu.utng.cunasegurawear.presentation.screens.StatusScreen
import mx.edu.utng.cunasegurawear.presentation.viewmodel.WatchViewModel

@Composable
fun WatchNavHost(viewModel: WatchViewModel) {
    val state by viewModel.state.collectAsState()
    val navController = rememberSwipeDismissableNavController()

    LaunchedEffect(state.phase) {
        when (state.phase) {
            AlertPhase.COUNTDOWN  -> navController.navigate("countdown")
            AlertPhase.ACTIVE     -> navController.navigate("alert_active")
            AlertPhase.LIFE_CHECK -> navController.navigate("life_check")
            AlertPhase.IDLE, AlertPhase.CANCELLED -> navController.navigate("status") {
                popUpTo("status") { inclusive = true }
            }
        }
    }

    // Solo llama onSwipeBackToCountdown cuando el usuario hace swipe de regreso
    // desde alert_active hacia countdown. Rastrear la ruta anterior evita que
    // se dispare al navegar a countdown por primera vez (que cancelaba el countdown
    // recién iniciado y lo relanzaba como SOS_GENERAL).
    LaunchedEffect(navController) {
        var previousRoute: String? = null
        navController.currentBackStackEntryFlow.collect { entry ->
            val currentRoute = entry.destination.route
            if (currentRoute == "countdown"
                && previousRoute == "alert_active"
                && state.phase == AlertPhase.ACTIVE
            ) {
                viewModel.onSwipeBackToCountdown()
            }
            previousRoute = currentRoute
        }
    }

    SwipeDismissableNavHost(navController, startDestination = "status") {
        composable("status") {
            StatusScreen(
                state = state,
                onSosClick = { viewModel.onSosPress() },
                onSimulate1Tap = { viewModel.onSimulateTaps(1) },
                onSimulate2Taps = { viewModel.onSimulateTaps(2) },
                onSimulate3Taps = { viewModel.onSimulateTaps(3) },
                onSimulate4Taps = { viewModel.onSimulateTaps(4) },
                onConfig = { navController.navigate("config") }
            )
        }
        composable("config") {
            ConfigScreen(
                configs = state.touchConfigs,
                onUpdateConfig = { tapNumber, action -> viewModel.updateTouchConfig(tapNumber, action) },
                onBack = { navController.popBackStack() }
            )
        }
        composable("countdown") {
            CountdownScreen(
                seconds = state.countdownSeconds,
                activeActionLabel = state.activeActionLabel,
                onCancel = { viewModel.onCancelCountdown() }
            )
        }
        composable("alert_active") {
            AlertActiveScreen(state = state)
        }
        composable("life_check") {
            LifeCheckScreen(
                onYes = { viewModel.onLifeCheckYes() },
                onNo = { viewModel.onLifeCheckNo() }
            )
        }
    }
}