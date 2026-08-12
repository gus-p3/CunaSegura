package mx.edu.utng.cunasegura.presentation.navigation

/**
 * Jerarquía sellada que define todas las rutas de navegación de la aplicación Cuna Segura.
 *
 * Previene el uso de cadenas mágicas y facilita el paso seguro de argumentos tipados en Jetpack Compose Navigation.
 *
 * @property route Cadena de plantilla que identifica la pantalla en el NavHost.
 */
sealed class Screen(val route: String) {
    /** Pantalla inicial de verificación de sesión y redirección. */
    object Splash : Screen("splash")
    /** Pantalla de inicio de sesión con correo y contraseña. */
    object Login : Screen("login")
    /** Pantalla principal del ciudadano con barra de navegación inferior. */
    object Home : Screen("home")
    /** Pantalla de emergencia activa en curso con cronómetro de gracia. */
    object EmergencyActive : Screen("emergency_active/{alertaId}") {
        /** Construye la ruta con el identificador concreto de la alerta. */
        fun createRoute(alertaId: Int) = "emergency_active/$alertaId"
    }
    /** Pantalla de gestión de contactos de emergencia. */
    object Contacts : Screen("contacts")
    /** Pantalla de vinculación y estado de SmartWatch y Smart TV. */
    object Devices : Screen("devices")
    /** Pantalla de mapeo de toques para reloj Wear OS. */
    object WatchConfig : Screen("watch_config")
    /** Pantalla de vinculación comunitaria de Smart TV mediante PIN o QR. */
    object TvConfig : Screen("tv_config")
    /** Pantalla de visualización de mapa comunitario en vivo. */
    object CommunityMap : Screen("community_map")
    /** Panel de control maestro para administradores. */
    object AdminPanel : Screen("admin_panel")
    /** Pantalla de registro de nuevos usuarios ciudadanos. */
    object Register : Screen("register")
    /** Pantalla de exploración y adhesión a redes vecinales. */
    object Networks : Screen("networks")
}