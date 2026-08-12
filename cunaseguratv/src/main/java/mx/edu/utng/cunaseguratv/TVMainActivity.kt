package mx.edu.utng.cunaseguratv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utng.cunaseguratv.presentation.TvMonitorViewModel
import mx.edu.utng.cunaseguratv.presentation.screens.AlertModalOverlay
import mx.edu.utng.cunaseguratv.presentation.screens.DashboardScreen
import mx.edu.utng.cunaseguratv.presentation.screens.VinculacionScreen
import mx.edu.utng.cunaseguratv.presentation.theme.CunaSeguraTvTheme

/**
 * Actividad principal del módulo Android TV para Cuna Segura.
 *
 * Configurada en orientación fija horizontal (Landscape) y compatible con Leanback Launcher.
 * Aloja el árbol de composición de Jetpack Compose for TV y coordina el flujo de pantallas:
 * 1. [VinculacionScreen]: Cuando la TV no ha sido vinculada mediante código QR a una red vecinal.
 * 2. [DashboardScreen]: Vista cinemática de monitoreo cuando la TV ya está vinculada a una red.
 * 3. [AlertModalOverlay]: Capa emergente superpuesta que se despliega automáticamente ante una alerta SOS crítica.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
class TVMainActivity : ComponentActivity() {
    
    /**
     * Punto de entrada del ciclo de vida de la actividad.
     * Establece el contenido visual declarativo utilizando [CunaSeguraTvTheme] y
     * suscribe el estado de la UI desde [TvMonitorViewModel].
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            CunaSeguraTvTheme {
                val viewModel: TvMonitorViewModel = viewModel()
                val state by viewModel.state.collectAsState()

                Box(modifier = Modifier.fillMaxSize()) {
                    // Si la TV no está vinculada a una red vecinal, muestra el código QR de emparejamiento
                    if (!state.isVinculada) {
                        VinculacionScreen(
                            state = state,
                            onSimularVinculacion = { viewModel.simularVinculacionExitosa() }
                        )
                    } else {
                        // Dashboard de monitoreo principal con mapa y panel lateral dinámico
                        DashboardScreen(
                            state = state,
                            onSilenciar = { viewModel.silenciarAlarma() },
                            onCerrarSesion = { viewModel.cerrarSesion() },
                            onToggleColorPicker = { viewModel.toggleColorPicker() },
                            onGuardarColores = { u, v, a -> viewModel.guardarColores(u, v, a) }
                        )
                    }
                    
                    // Modal de emergencia de alta prioridad superpuesto sobre cualquier vista activa
                    if (state.showAlertModal && state.alertaActiva != null) {
                        AlertModalOverlay(
                            alerta = state.alertaActiva!!,
                            onDescartar = { viewModel.descartarModalAlerta() },
                            onSilenciar = { viewModel.silenciarAlarma() }
                        )
                    }
                }
            }
        }
    }
}

