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
                }
            )
        }

        // Panel de administración (solo admins)
        composable(Screen.AdminPanel.route) {
            AdminPanelScreen(
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.AdminPanel.route) { inclusive = true }
                    }
                }
            )
        }

        // HomeScreen
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

        composable(Screen.Contacts.route) {
            ContactsScreen()
        }

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

        // CommunityMapScreen real (ISSUE-10)
        composable(Screen.CommunityMap.route) {
            CommunityMapScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onNavigateToContacts = {
                    navController.navigate(Screen.Contacts.route)
                },
                onNavigateToDevices = {
                    navController.navigate(Screen.Devices.route)
                }
            )
        }
    }
}