package mx.edu.utng.cunaseguratv.mqtt

import kotlinx.serialization.Serializable

@Serializable
data class AlertaMqttMessage(
    val usuarioId: Int,
    val nombreUsuario: String = "Vecino Desconocido",
    val latitud: Double,
    val longitud: Double,
    val nivelAlerta: Int = 3, // Cantidad de toques
    val estado: String = "activa",
    val timestamp: Long = System.currentTimeMillis(),
    val networkId: String = ""
)

@Serializable
data class TvStatusMessage(
    val tvId: String,
    val networkId: String,
    val isOnline: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
