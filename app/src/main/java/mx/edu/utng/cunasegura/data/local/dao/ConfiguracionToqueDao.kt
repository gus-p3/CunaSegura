package mx.edu.utng.cunasegura.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mx.edu.utng.cunasegura.data.local.entity.ConfiguracionToqueEntity

/**
 * Data Access Object (DAO) para la persistencia de configuraciones de toques y gestos SOS en SQLite.
 */
@Dao
interface ConfiguracionToqueDao {

    /**
     * Inserta o actualiza la acción asignada a una cantidad de toques para un usuario.
     *
     * @param config Entidad de configuración.
     * @return Identificador del registro insertado o reemplazado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarOActualizar(config: ConfiguracionToqueEntity): Long

    /**
     * Obtiene todas las configuraciones de toques asignadas a un usuario.
     *
     * @param usuarioId Identificador del usuario.
     * @return Lista de [ConfiguracionToqueEntity].
     */
    @Query("SELECT * FROM configuracion_toque WHERE usuarioId = :usuarioId")
    suspend fun obtenerPorUsuario(usuarioId: Int): List<ConfiguracionToqueEntity>

    /**
     * Obtiene la configuración específica para una cantidad de toques determinada de un usuario.
     *
     * @param usuarioId Identificador del usuario.
     * @param cantidadToques Cantidad de toques (1, 2, 3 o 4).
     * @return [ConfiguracionToqueEntity] correspondiente o `null` si no está configurado.
     */
    @Query("SELECT * FROM configuracion_toque WHERE usuarioId = :usuarioId AND cantidadToques = :cantidadToques LIMIT 1")
    suspend fun obtenerPorUsuarioYToque(usuarioId: Int, cantidadToques: Int): ConfiguracionToqueEntity?
}

