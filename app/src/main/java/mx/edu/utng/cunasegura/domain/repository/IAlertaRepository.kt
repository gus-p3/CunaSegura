package mx.edu.utng.cunasegura.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.edu.utng.cunasegura.domain.model.Alerta

/**
 * Interfaz de repositorio para la gestión de alertas de emergencia.
 */
interface IAlertaRepository {
    /**
     * Crea una nueva alerta y la guarda.
     * @return El ID autogenerado de la alerta.
     */
    suspend fun crearAlerta(alerta: Alerta): Long

    /**
     * Cancela la alerta con el ID especificado (cambia su estado a 'cancelada').
     */
    suspend fun cancelarAlerta(id: Int)

    /**
     * Obtiene una alerta por su ID.
     */
    suspend fun obtenerAlertaPorId(id: Int): Alerta?

    /**
     * Observa la alerta activa actual de un usuario.
     */
    fun obtenerAlertaActiva(usuarioId: Int): Flow<Alerta?>

    /**
     * Observa todas las alertas activas de la comunidad en tiempo real.
     */
    fun obtenerAlertasVecinalesActivas(): Flow<List<Alerta>>
}
