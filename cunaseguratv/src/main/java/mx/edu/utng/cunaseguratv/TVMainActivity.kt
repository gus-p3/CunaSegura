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

class TVMainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            CunaSeguraTvTheme {
                val viewModel: TvMonitorViewModel = viewModel()
                val state by viewModel.state.collectAsState()

                Box(modifier = Modifier.fillMaxSize()) {
                    if (!state.isVinculada) {
                        VinculacionScreen(
                            state = state,
                            onSimularVinculacion = { viewModel.simularVinculacionExitosa() }
                        )
                    } else {
                        DashboardScreen(
                            state = state,
                            onSilenciar = { viewModel.silenciarAlarma() },
                            onCerrarSesion = { viewModel.cerrarSesion() },
                            onToggleColorPicker = { viewModel.toggleColorPicker() },
                            onGuardarColores = { u, v, a -> viewModel.guardarColores(u, v, a) }
                        )
                    }
                    
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
