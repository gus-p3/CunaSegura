package mx.edu.utng.cunasegura.domain.model

/**
 * Modelo de dominio que representa un grupo vecinal comunitario administrado en Firestore.
 *
 * @property id Identificador del documento.
 * @property nombre Nombre visible de la comunidad.
 * @property codigoAcceso PIN o código alfanumérico para validación de entrada.
 * @property latitud Latitud central.
 * @property longitud Longitud central.
 * @property radioMetros Radio de cobertura perimetral en metros.
 */
data class Vecindad(
    val id: String = "",
    val nombre: String = "",
    val codigoAcceso: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val radioMetros: Int = 1000
)

