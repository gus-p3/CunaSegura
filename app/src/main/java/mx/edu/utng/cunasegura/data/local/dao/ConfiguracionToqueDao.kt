package mx.edu.utng.cunasegura.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mx.edu.utng.cunasegura.data.local.entity.ConfiguracionToqueEntity

/**
 * DAO para la configuración de toques de un vecino.
 */
@Dao
interface ConfiguracionToqueDao {

    /**
     * Inserta o reemplaza la configuración de toque.
     * Se usa REPLACE para evitar duplicados del mismo toque por usuario.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(config: ConfiguracionToqueEntity): Long

    /** Obtiene todas las configuraciones de toque de un usuario dado su [usuarioId]. */
    @Query("SELECT * FROM configuracion_toque WHERE usuarioId = :usuarioId")
    suspend fun obtenerPorUsuario(usuarioId: Int): List<ConfiguracionToqueEntity>

    /** Obtiene la configuración específica de un toque para un usuario. */
    @Query("SELECT * FROM configuracion_toque WHERE usuarioId = :usuarioId AND cantidadToques = :cantidadToques LIMIT 1")
    suspend fun obtenerPorUsuarioYToque(usuarioId: Int, cantidadToques: Int): ConfiguracionToqueEntity?
}
