package mx.edu.utng.cunasegura.presentation.navigation

/**
 * Rutas de navegación de la app, como sealed class para evitar strings sueltos.
 */
sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Home : Screen("home")
    object EmergencyActive : Screen("emergency_active/{alertaId}") {
        fun createRoute(alertaId: Long) = "emergency_active/$alertaId"
    }
    object Contacts : Screen("contacts")
    object Devices : Screen("devices")
    object WatchConfig : Screen("watch_config")
    object TvConfig : Screen("tv_config")
    object CommunityMap : Screen("community_map")
}