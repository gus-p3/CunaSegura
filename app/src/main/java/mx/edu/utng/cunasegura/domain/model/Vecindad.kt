package mx.edu.utng.cunasegura.domain.model

/**
 * Modelo de dominio puro que representa una Vecindad.
 * Usado para agrupar vecinos en Firebase Firestore.
 */
data class Vecindad(
    val id: String = "",
    val nombre: String = "",
    val codigoAcceso: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val radioMetros: Int = 1000
)
