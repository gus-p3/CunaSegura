package mx.edu.utng.cunasegura.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.edu.utng.cunasegura.presentation.contacts.ContactsScreen
import mx.edu.utng.cunasegura.presentation.login.LoginScreen
import mx.edu.utng.cunasegura.presentation.splash.SplashScreen
import mx.edu.utng.cunasegura.presentation.home.HomeScreen
import mx.edu.utng.cunasegura.presentation.emergency.EmergencyActiveScreen
import mx.edu.utng.cunasegura.presentation.devices.DevicesScreen
import mx.edu.utng.cunasegura.presentation.watchconfig.WatchConfigScreen
import mx.edu.utng.cunasegura.presentation.tvconfig.TvConfigScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        // HomeScreen real
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToEmergency = { alertaId ->
                    navController.navigate(Screen.EmergencyActive.createRoute(alertaId))
                },
                onNavigateToContacts = {
                    navController.navigate(Screen.Contacts.route)
                },
                onNavigateToDevices = {
                    navController.navigate(Screen.Devices.route)
                },
                onNavigateToMap = {
                    navController.navigate(Screen.CommunityMap.route)
                }
            )
        }

        // EmergencyActiveScreen real con argumento alertaId
        composable(
            route = Screen.EmergencyActive.route,
            arguments = listOf(
                navArgument("alertaId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val alertaId = backStackEntry.arguments?.getInt("alertaId") ?: 0
            EmergencyActiveScreen(
                alertaId = alertaId,
                onBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Contacts.route) {
            ContactsScreen()
        }

        // DevicesScreen real
        composable(Screen.Devices.route) {
            DevicesScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route)
                },
                onNavigateToContacts = {
                    navController.navigate(Screen.Contacts.route)
                },
                onNavigateToWatchConfig = {
                    navController.navigate(Screen.WatchConfig.route)
                },
                onNavigateToTvConfig = {
                    navController.navigate(Screen.TvConfig.route)
                },
                onNavigateToMap = {
                    navController.navigate(Screen.CommunityMap.route)
                }
            )
        }

        // WatchConfigScreen real
        composable(Screen.WatchConfig.route) {
            WatchConfigScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // TvConfigScreen real
        composable(Screen.TvConfig.route) {
            TvConfigScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // TODO (Gustavo - ISSUE-10): reemplazar por CommunityMapScreen real
        composable(Screen.CommunityMap.route) {
            PlaceholderScreen("Mapa Comunitario — En construcción (ISSUE-10)")
        }
    }
}

@Composable
private fun PlaceholderScreen(texto: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(texto)
    }
}