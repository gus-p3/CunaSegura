package mx.edu.utng.cunasegura.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import mx.edu.utng.cunasegura.data.local.entity.AlertaEntity

/**
 * DAO para operaciones sobre la tabla de alertas.
 */
@Dao
interface AlertaDao {

    /**
     * Inserta una alerta. Si ya existe (mismo id), la reemplaza.
     * @return el rowId de la alerta insertada.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarAlerta(alerta: AlertaEntity): Long

    /**
     * Retorna un Flow reactivo con las alertas que aún están activas
     * (no atendidas y no marcadas como falsa alarma).
     */
    @Query("SELECT * FROM alertas WHERE fueAtendida = 0 AND esFalsaAlarma = 0 ORDER BY creadoEn DESC")
    fun obtenerAlertasActivas(): Flow<List<AlertaEntity>>

    /**
     * Busca una alerta por su ID.
     * @return [AlertaEntity] si existe, null si no.
     */
    @Query("SELECT * FROM alertas WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Int): AlertaEntity?

    /**
     * Actualiza solo el campo [estado] de una alerta existente.
     * Más eficiente que hacer INSERT REPLACE ya que no toca otros campos.
     */
    @Query("UPDATE alertas SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: String)

    /**
     * Observa la alerta activa más reciente de un usuario.
     */
    @Query("SELECT * FROM alertas WHERE usuarioId = :usuarioId AND estado = 'activa' ORDER BY creadoEn DESC LIMIT 1")
    fun obtenerAlertaActivaPorUsuario(usuarioId: Int): Flow<AlertaEntity?>

    /**
     * Busca síncronamente la alerta activa más reciente de un usuario.
     */
    @Query("SELECT * FROM alertas WHERE usuarioId = :usuarioId AND estado = 'activa' ORDER BY creadoEn DESC LIMIT 1")
    suspend fun buscarAlertaActivaPorUsuario(usuarioId: Int): AlertaEntity?
}
