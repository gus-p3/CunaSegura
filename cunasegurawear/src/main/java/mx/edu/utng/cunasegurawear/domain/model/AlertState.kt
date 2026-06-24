package mx.edu.utng.cunasegurawear.domain.model

data class AlertState(
    val phase: AlertPhase = AlertPhase.IDLE,
    val countdownSeconds: Int = 5,
    val isGpsActive: Boolean = false,
    val contactsNotified: Int = 0,
    val gpsAddress: String = "",
    val configuredActions: List<SosAction> = emptyList(),
    val activeActionLabel: String = ""
)

enum class AlertPhase {
    IDLE,        // Pantalla 1: reloj en reposo, escudo verde visible
    COUNTDOWN,   // Pantalla 3: cuenta regresiva 5s corriendo
    ACTIVE,      // Pantalla 4-5: alerta enviada, GPS transmitiendo
    LIFE_CHECK,  // Pantalla 6: pregunta ¿Estás bien? a los 2 min
    CANCELLED    // Pantalla 1: regresa a reposo tras cancelar
}
