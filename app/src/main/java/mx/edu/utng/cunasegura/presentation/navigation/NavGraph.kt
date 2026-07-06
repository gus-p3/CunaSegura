package mx.edu.utng.cunasegura.presentation.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import mx.edu.utng.cunasegura.presentation.contacts.ContactsScreen
import mx.edu.utng.cunasegura.presentation.login.LoginScreen
import mx.edu.utng.cunasegura.presentation.splash.SplashScreen

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

        // TODO (Lizeth - ISSUE-05): reemplazar por HomeScreen real
        composable(Screen.Home.route) {
            PlaceholderScreen("Home — En construcción (ISSUE-05)")
        }

        // TODO (Lizeth - ISSUE-07): reemplazar por EmergencyActiveScreen real
        composable(Screen.EmergencyActive.route) {
            PlaceholderScreen("Emergencia Activa — En construcción (ISSUE-07)")
        }

        composable(Screen.Contacts.route) {
            ContactsScreen()
        }

        // TODO (Lizeth - ISSUE-09): reemplazar por DevicesScreen real
        composable(Screen.Devices.route) {
            PlaceholderScreen("Dispositivos — En construcción (ISSUE-09)")
        }

        composable(Screen.WatchConfig.route) {
            PlaceholderScreen("En construcción — Sprint 2")
        }

        composable(Screen.TvConfig.route) {
            PlaceholderScreen("En construcción — Sprint 2")
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