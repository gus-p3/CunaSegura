package mx.edu.utng.cunasegura.domain.model

data class Network(
    val id: String = "",
    val nombre: String = "",
    val tipo: String = "Abierta", // "Abierta" (GPS) o "Cerrada" (QR)
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val radio: Double = 200.0, // Radio de cobertura por defecto (ej. 200 metros)
    val miembros: Map<String, Boolean> = emptyMap(),
    val tvId: String = ""
)
