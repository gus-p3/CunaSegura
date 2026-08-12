package mx.edu.utng.cunasegura.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.edu.utng.cunasegura.data.local.entity.AlertaEntity

/**
 * Data Access Object (DAO) para gestión y consulta de alertas de emergencia ciudadana en SQLite.
 */
@Dao
interface AlertaDao {

    /**
     * Inserta una nueva alerta en la base de datos.
     *
     * @param alerta Entidad de la alerta a persistir.
     * @return El identificador autonumérico generado para la alerta.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAlerta(alerta: AlertaEntity): Long

    /**
     * Emite un [Flow] reactivo con la lista de alertas que se encuentran activas
     * (no atendidas y no marcadas como falsa alarma), ordenadas de la más reciente a la más antigua.
     *
     * @return Flujo reactivo de lista de [AlertaEntity].
     */
    @Query("SELECT * FROM alertas WHERE fueAtendida = 0 AND esFalsaAlarma = 0 ORDER BY creadoEn DESC")
    fun obtenerAlertasActivas(): Flow<List<AlertaEntity>>

    /**
     * Busca una alerta específica por su ID primario.
     *
     * @param id Identificador de la alerta.
     * @return [AlertaEntity] si se encuentra, `null` en caso contrario.
     */
    @Query("SELECT * FROM alertas WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): AlertaEntity?

    /**
     * Actualiza el campo de estado de una alerta sin modificar sus demás propiedades.
     *
     * @param id Identificador de la alerta.
     * @param estado Nuevo estado (`activa`, `cancelada`, `atendida`).
     */
    @Query("UPDATE alertas SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: String)

    /**
     * Observa de forma reactiva la alerta activa más reciente emitida por un usuario.
     *
     * @param usuarioId Identificador del usuario.
     * @return [Flow] que emite la alerta activa o `null`.
     */
    @Query("SELECT * FROM alertas WHERE usuarioId = :usuarioId AND estado = 'activa' ORDER BY creadoEn DESC LIMIT 1")
    fun obtenerAlertaActivaPorUsuario(usuarioId: Int): Flow<AlertaEntity?>

    /**
     * Consulta de manera síncrona/suspendida la alerta activa más reciente de un usuario.
     *
     * @param usuarioId Identificador del usuario.
     * @return [AlertaEntity] activa más reciente o `null`.
     */
    @Query("SELECT * FROM alertas WHERE usuarioId = :usuarioId AND estado = 'activa' ORDER BY creadoEn DESC LIMIT 1")
    suspend fun buscarAlertaActivaPorUsuario(usuarioId: Int): AlertaEntity?
}

