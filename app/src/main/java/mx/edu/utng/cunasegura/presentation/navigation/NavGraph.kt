package mx.edu.utng.cunasegura.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.edu.utng.cunasegura.presentation.admin.AdminPanelScreen
import mx.edu.utng.cunasegura.presentation.contacts.ContactsScreen
import mx.edu.utng.cunasegura.presentation.login.LoginScreen
import mx.edu.utng.cunasegura.presentation.splash.SplashScreen
import mx.edu.utng.cunasegura.presentation.home.HomeScreen
import mx.edu.utng.cunasegura.presentation.emergency.EmergencyActiveScreen
import mx.edu.utng.cunasegura.presentation.devices.DevicesScreen
import mx.edu.utng.cunasegura.presentation.watchconfig.WatchConfigScreen
import mx.edu.utng.cunasegura.presentation.tvconfig.TvConfigScreen
import mx.edu.utng.cunasegura.presentation.map.CommunityMapScreen

import mx.edu.utng.cunasegura.presentation.login.RegisterScreen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startAlertaId: Int? = null,
    onAlertaHandled: () -> Unit = {}
) {
    androidx.compose.runtime.LaunchedEffect(startAlertaId) {
        if (startAlertaId != null) {
            navController.navigate(Screen.EmergencyActive.createRoute(startAlertaId))
            onAlertaHandled()
        }
    }

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
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminPanel.route) {
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
                },
                onAdminSuccess = {
                    navController.navigate(Screen.AdminPanel.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Panel de administración (solo admins)
        composable(Screen.AdminPanel.route) {
            mx.edu.utng.cunasegura.presentation.main.MainAdminScreen(rootNavController = navController)
        }

        // MainUserScreen handles Home, Contacts, Devices, and Map internally
        composable(Screen.Home.route) {
            mx.edu.utng.cunasegura.presentation.main.MainUserScreen(rootNavController = navController)
        }

        // EmergencyActiveScreen con argumento alertaId
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

        composable(Screen.WatchConfig.route) {
            WatchConfigScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TvConfig.route) {
            TvConfigScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}