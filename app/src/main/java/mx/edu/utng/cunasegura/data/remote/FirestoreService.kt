package mx.edu.utng.cunasegura.data.remote

import mx.edu.utng.cunasegura.domain.model.Vecindad

/**
 * Servicio para interactuar con Firebase Firestore (Placeholders)
 * para preparar la lógica de vecindades (Problemas 7 y 8).
 */
class FirestoreService {

    // TODO: Implementar la conexión real a Firestore cuando se integre Firebase

    /**
     * Obtiene la lista de vecindades disponibles.
     */
    suspend fun getVecindades(): List<Vecindad> {
        // Placeholder: Retorna lista vacía temporalmente
        return emptyList()
    }

    /**
     * Une a un usuario a una vecindad específica mediante un código de acceso.
     * @return true si el código es válido y se unió con éxito.
     */
    suspend fun unirseVecindad(usuarioId: String, codigoAcceso: String): Boolean {
        // Placeholder: Simula éxito
        return true
    }

    /**
     * Envía una alerta a Firestore que será escuchada por la app de Smart TV.
     */
    suspend fun enviarAlertaTv(alertaData: Map<String, Any>): Boolean {
        // Placeholder: Simula envío exitoso
        return true
    }
}
