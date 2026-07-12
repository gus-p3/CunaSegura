package mx.edu.utng.cunasegura.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.edu.utng.cunasegura.presentation.admin.AdminConfigScreen
import mx.edu.utng.cunasegura.presentation.admin.AdminDashboardScreen
import mx.edu.utng.cunasegura.presentation.admin.AdminMembersScreen
import mx.edu.utng.cunasegura.presentation.admin.AdminProfileScreen
import mx.edu.utng.cunasegura.presentation.admin.AdminStatsScreen
import mx.edu.utng.cunasegura.presentation.navigation.Screen

@Composable
fun MainAdminScreen(
    rootNavController: NavHostController,
    mainNavController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "admin_dashboard"

    val AzulCunaSegura = Color(0xFF1F4E79)

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentRoute == "admin_dashboard",
                    onClick = {
                        if (currentRoute != "admin_dashboard") {
                            mainNavController.navigate("admin_dashboard") {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "admin_members",
                    onClick = {
                        if (currentRoute != "admin_members") {
                            mainNavController.navigate("admin_members") {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.People, contentDescription = "Miembros") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "admin_config",
                    onClick = {
                        if (currentRoute != "admin_config") {
                            mainNavController.navigate("admin_config") {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Configuración") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "admin_stats",
                    onClick = {
                        if (currentRoute != "admin_stats") {
                            mainNavController.navigate("admin_stats") {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Estadísticas") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "admin_profile",
                    onClick = {
                        if (currentRoute != "admin_profile") {
                            mainNavController.navigate("admin_profile") {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = mainNavController,
            startDestination = "admin_dashboard",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("admin_dashboard") {
                AdminDashboardScreen()
            }
            composable("admin_members") {
                AdminMembersScreen()
            }
            composable("admin_config") {
                AdminConfigScreen()
            }
            composable("admin_stats") {
                AdminStatsScreen()
            }
            composable("admin_profile") {
                AdminProfileScreen(
                    onLogout = {
                        rootNavController.navigate(Screen.Login.route) {
                            popUpTo(Screen.AdminPanel.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
