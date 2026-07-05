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
     * Se usa REPLACE para que cada usuario tenga sólo una configuración vigente.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(config: ConfiguracionToqueEntity): Long

    /** Obtiene la configuración de toque de un usuario dado su [usuarioId]. */
    @Query("SELECT * FROM configuracion_toque WHERE usuarioId = :usuarioId LIMIT 1")
    suspend fun obtenerPorUsuario(usuarioId: Int): ConfiguracionToqueEntity?
}
