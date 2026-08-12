package mx.edu.utng.cunasegura.domain.model

/**
 * Modelo de dominio que define una Red Vecinal Comunitaria y sus políticas de seguridad.
 *
 * @property id Identificador único de la red en Firebase Realtime Database.
 * @property nombre Nombre descriptivo de la colonia o sector barrial.
 * @property tipo Modalidad de adhesión (`Abierta` por proximidad GPS o `Cerrada` por código QR).
 * @property latitud Coordenada central de latitud geográfica de la red.
 * @property longitud Coordenada central de longitud geográfica de la red.
 * @property radio Radio de cobertura perimetral en metros (ej. 200m).
 * @property miembros Mapa de identificadores de miembros pertenecientes a la red.
 * @property tvId Identificador de la Smart TV asignada como central comunitaria.
 * @property tiempoAntiFalsa Segundos de gracia para cancelar alertas accidentales.
 * @property checkVida Intervalo en minutos para verificación de conectividad periódica.
 * @property esperarDiasNuevos Días de antigüedad requeridos a nuevos miembros para emitir alertas masivas.
 */
data class Network(
    val id: String = "",
    val nombre: String = "",
    val tipo: String = "Abierta", // "Abierta" (GPS) o "Cerrada" (QR)
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val radio: Double = 200.0, // Radio de cobertura por defecto (ej. 200 metros)
    val miembros: Map<String, Boolean> = emptyMap(),
    val tvId: String = "",
    val tiempoAntiFalsa: Double = 5.0,
    val checkVida: Double = 2.0,
    val esperarDiasNuevos: Int = 0
)

