package mx.edu.utng.cunaseguratv.data

data class AlertaTV(
    val id: Int = 0,
    val usuarioId: Int = 0,
    val nombreUsuario: String = "Vecino",
    val estado: String = "activa",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fueAtendida: Boolean = false,
    val esFalsaAlarma: Boolean = false,
    val creadoEn: Long = System.currentTimeMillis(),
    val networkId: String = ""
)
