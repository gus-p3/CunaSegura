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

class MainActivity : ComponentActivity() {
    private var startAlertaId by mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Solicitar permisos de llamada y SMS necesarios para Wear OS
        solicitarPermisosEmergencia()

        // Handle initial intent
        val alertaId = intent.getIntExtra("EXTRA_ALERTA_ID", -1)
        if (alertaId != -1) {
            startAlertaId = alertaId
        }

        // Start location tracking service
        val serviceIntent = Intent(this, mx.edu.utng.cunasegura.data.location.LocationTrackerService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        setContent {
            CunaSeguraTheme {
                NavGraph(
                    startAlertaId = startAlertaId,
                    onAlertaHandled = { startAlertaId = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val alertaId = intent.getIntExtra("EXTRA_ALERTA_ID", -1)
        if (alertaId != -1) {
            startAlertaId = alertaId
        }
    }

    private fun solicitarPermisosEmergencia() {
        val permisos = mutableListOf<String>()
        
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