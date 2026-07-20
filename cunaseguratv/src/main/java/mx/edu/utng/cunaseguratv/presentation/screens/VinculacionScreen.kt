package mx.edu.utng.cunaseguratv.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import mx.edu.utng.cunaseguratv.presentation.TvUiState

@Composable
fun VinculacionScreen(
    state: TvUiState,
    onSimularVinculacion: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "🛡️ Cuna Segura TV",
                style = MaterialTheme.typography.displayMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Escanea este código QR con la app de Cuna Segura en tu teléfono para vincular esta TV a tu red vecinal.",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (state.qrCode != null) {
                Box(
                    modifier = Modifier
                        .size(300.dp)
                        .background(MaterialTheme.colorScheme.onPrimary)
                        .padding(16.dp)
                ) {
                    Image(
                        bitmap = state.qrCode.asImageBitmap(),
                        contentDescription = "Código QR de Vinculación",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Text("Generando código QR...")
            }
            
            // Botón oculto/simulado para pruebas
            Button(onClick = onSimularVinculacion) {
                Text("Simular Vinculación Exitosa")
            }
        }
    }
}
