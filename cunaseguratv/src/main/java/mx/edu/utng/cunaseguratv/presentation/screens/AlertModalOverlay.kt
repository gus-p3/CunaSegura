package mx.edu.utng.cunaseguratv.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import mx.edu.utng.cunaseguratv.mqtt.AlertaMqttMessage

/**
 * Capa emergente superpuesta (Modal Overlay) desplegada a pantalla completa ante una alerta SOS crítica.
 *
 * Presenta una animación de parpadeo estroboscópico de color rojo de alto contraste para captar de inmediato
 * la atención de los residentes u operadores de la televisión en la habitación. Despliega el nombre del vecino,
 * su identificador, el nivel de toques físicos y sus coordenadas GPS exactas.
 *
 * Provee acciones navegables por D-Pad para silenciar la alarma auditiva y descartar el diálogo para navegar
 * directamente al mapa del incidente.
 *
 * @param alerta Datos del mensaje de alerta MQTT [AlertaMqttMessage] que detonó la emergencia.
 * @param onDescartar Lambda callback para cerrar el modal y enfocar el mapa.
 * @param onSilenciar Lambda callback para silenciar el tono de sirena auditivo.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
@Composable
fun AlertModalOverlay(
    alerta: AlertaMqttMessage,
    onDescartar: () -> Unit,
    onSilenciar: () -> Unit
) {
    // Animación de parpadeo rojo estroboscópico para máxima visibilidad en pantallas 1080p/4K
    val infiniteTransition = rememberInfiniteTransition()
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.error.copy(alpha = alphaAnim)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .background(MaterialTheme.colorScheme.background, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "🚨 ¡EMERGENCIA VECINAL! 🚨",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "El vecino ${alerta.nombreUsuario} (ID: ${alerta.usuarioId}) activó una alerta SOS.",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Nivel de Alerta: ${alerta.nivelAlerta} toques",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
            
            Text(
                text = "Ubicación GPS: ${alerta.latitud}, ${alerta.longitud}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Button(onClick = onDescartar) {
                    Text("VER EN MAPA")
                }
                
                Button(
                    onClick = {
                        onSilenciar()
                        onDescartar()
                    },
                    colors = ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("SILENCIAR Y VER MAPA")
                }
            }
        }
    }
}

