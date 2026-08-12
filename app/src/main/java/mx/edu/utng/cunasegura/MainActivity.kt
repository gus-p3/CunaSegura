package mx.edu.utng.cunasegura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.edu.utng.cunasegura.presentation.navigation.NavGraph
import mx.edu.utng.cunasegura.ui.theme.CunaSeguraTheme

import mx.edu.utng.cunasegura.data.local.prefs.PreferencesManager
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Actividad principal de la aplicación Cuna Segura (Dolores Hidalgo).
 *
 * Responsabilidades:
 * - Servir como punto de entrada (Single Activity Architecture) para Jetpack Compose.
 * - Gestionar la solicitud en tiempo de ejecución de permisos críticos (Ubicación GPS precisa, llamada telefónica directa, envío de SMS y notificaciones).
 * - Procesar intents entrantes con identificadores de alerta activa ([startAlertaId]) provenientes de notificaciones push o servicios en segundo plano.
 * - Inicializar el servicio de rastreo GPS en primer plano ([mx.edu.utng.cunasegura.data.location.LocationTrackerService]).
 */
class MainActivity : ComponentActivity() {
    
    /**
     * Estado observable que almacena el ID de alerta activa entrante para navegación directa a la pantalla de emergencia.
     */
    private var startAlertaId by mutableStateOf<Int?>(null)

    /**
     * Inicializa la actividad, configura el diseño Edge-to-Edge, solicita permisos críticos,
     * inicia el servicio en primer plano e infla el grafo de navegación Compose.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Solicitar permisos de llamada y SMS necesarios para Wear OS y alertas
        solicitarPermisosEmergencia()

        // Procesar intent inicial si fue lanzado por notificación o deep link
        val alertaId = intent.getIntExtra("EXTRA_ALERTA_ID", -1)
        if (alertaId != -1) {
            startAlertaId = alertaId
        }

        // Iniciar servicio de geolocalización y escucha de alertas en primer plano
        try {
            val serviceIntent = Intent(this, mx.edu.utng.cunasegura.data.location.LocationTrackerService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error al iniciar LocationTrackerService: ${e.message}")
        }

        setContent {
            CunaSeguraTheme {
                NavGraph(
                    startAlertaId = startAlertaId,
                    onAlertaHandled = { startAlertaId = null }
                )
            }
        }
    }

    /**
     * Recibe nuevos intents cuando la actividad ya se encuentra en ejecución (SingleTop / Re-launch).
     *
     * @param intent El nuevo intent recibido con posibles extras de alerta.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val alertaId = intent.getIntExtra("EXTRA_ALERTA_ID", -1)
        if (alertaId != -1) {
            startAlertaId = alertaId
        }
    }

    /**
     * Verifica y solicita de manera agrupada los permisos de tiempo de ejecución indispensables
     * para el funcionamiento del sistema de alerta ciudadana (GPS, llamadas 911, SMS SOS y notificaciones).
     */
    private fun solicitarPermisosEmergencia() {
        val permisos = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permisos.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            permisos.add(Manifest.permission.CALL_PHONE)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permisos.add(Manifest.permission.SEND_SMS)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permisos.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permisos.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permisos.toTypedArray(), 101)
        }
    }
}