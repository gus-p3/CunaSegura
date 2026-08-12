package mx.edu.utng.cunasegura.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.edu.utng.cunasegura.domain.model.Alerta

/**
 * Contrato de repositorio para el ciclo de vida, persistencia y despacho de alertas SOS.
 */
interface IAlertaRepository {

    /**
     * Registra y emite una nueva alerta SOS ciudadana.
     *
     * @param alerta Modelo de la alerta a persistir.
     * @return Identificador autogenerado asignado a la alerta.
     */
    suspend fun crearAlerta(alerta: Alerta): Long

    /**
     * Cancela una alerta activa marcándola como cancelada o falsa alarma.
     *
     * @param id Identificador de la alerta.
     */
    suspend fun cancelarAlerta(id: Int)

    /**
     * Consulta una alerta por su identificador único.
     *
     * @param id Identificador de la alerta.
     * @return [Alerta] si existe o `null`.
     */
    suspend fun obtenerAlertaPorId(id: Int): Alerta?

    /**
     * Observa de forma reactiva la alerta activa del usuario especificado.
     *
     * @param usuarioId Identificador del usuario.
     * @return [Flow] reactivo que emite la alerta o `null`.
     */
    fun obtenerAlertaActiva(usuarioId: Int): Flow<Alerta?>

    /**
     * Observa en tiempo real el flujo de todas las alertas activas en la red comunitaria.
     *
     * @return [Flow] reactivo con la lista de alertas activas.
     */
    fun obtenerAlertasVecinalesActivas(): Flow<List<Alerta>>

    /**
     * Consulta el historial histórico completo de alertas para fines estadísticos.
     *
     * @return Lista de [Alerta].
     */
    suspend fun obtenerTodasLasAlertas(): List<Alerta>

    /**
     * Obtiene el listado de alertas asociadas a una red vecinal específica.
     *
     * @param networkId Identificador de la red.
     * @return Lista de alertas de la red.
     */
    suspend fun obtenerAlertasPorNetworkId(networkId: String): List<Alerta>
}

