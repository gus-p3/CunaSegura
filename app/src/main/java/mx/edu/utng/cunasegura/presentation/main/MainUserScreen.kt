package mx.edu.utng.cunasegura.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.material.icons.filled.Shield
import mx.edu.utng.cunasegura.presentation.contacts.ContactsScreen
import mx.edu.utng.cunasegura.presentation.devices.DevicesScreen
import mx.edu.utng.cunasegura.presentation.home.HomeScreen
import mx.edu.utng.cunasegura.presentation.map.CommunityMapScreen
import mx.edu.utng.cunasegura.presentation.navigation.Screen
import mx.edu.utng.cunasegura.presentation.networks.NetworksScreen
import mx.edu.utng.cunasegura.presentation.profile.UserProfileScreen

@Composable
fun MainUserScreen(
    rootNavController: NavHostController,
    mainNavController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val AzulCunaSegura = androidx.compose.material3.MaterialTheme.colorScheme.primary

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentRoute == Screen.Home.route,
                    onClick = {
                        if (currentRoute != Screen.Home.route) {
                            mainNavController.navigate(Screen.Home.route) {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        selectedTextColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Networks.route,
                    onClick = {
                        if (currentRoute != Screen.Networks.route) {
                            mainNavController.navigate(Screen.Networks.route) {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Red Vecinal") },
                    label = { Text("Red Vecinal") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        selectedTextColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Contacts.route,
                    onClick = {
                        if (currentRoute != Screen.Contacts.route) {
                            mainNavController.navigate(Screen.Contacts.route) {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Call, contentDescription = "Contactos") },
                    label = { Text("Contactos") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        selectedTextColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.CommunityMap.route,
                    onClick = {
                        if (currentRoute != Screen.CommunityMap.route) {
                            mainNavController.navigate(Screen.CommunityMap.route) {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Mapa") },
                    label = { Text("Mapa") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        selectedTextColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "user_profile",
                    onClick = {
                        if (currentRoute != "user_profile") {
                            mainNavController.navigate("user_profile") {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        selectedTextColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = mainNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToEmergency = { alertaId ->
                        rootNavController.navigate(Screen.EmergencyActive.createRoute(alertaId))
                    }
                )
            }
            composable(Screen.Networks.route) {
                NetworksScreen(
                    onBack = {
                        mainNavController.navigate(Screen.Home.route) {
                            popUpTo(mainNavController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Contacts.route) {
                ContactsScreen()
            }
            composable(Screen.Devices.route) {
                DevicesScreen(
                    onNavigateToWatchConfig = {
                        rootNavController.navigate(Screen.WatchConfig.route)
                    },
                    onNavigateToTvConfig = {
                        rootNavController.navigate(Screen.TvConfig.route)
                    }
                )
            }
            composable(Screen.CommunityMap.route) {
                CommunityMapScreen()
            }
            composable("user_profile") {
                UserProfileScreen(
                    onLogout = {
                        rootNavController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToNetworks = {
                        mainNavController.navigate(Screen.Networks.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToWatchConfig = {
                        rootNavController.navigate(Screen.WatchConfig.route)
                    },
                    onNavigateToTvConfig = {
                        rootNavController.navigate(Screen.TvConfig.route)
                    }
                )
            }
        }
    }
}
