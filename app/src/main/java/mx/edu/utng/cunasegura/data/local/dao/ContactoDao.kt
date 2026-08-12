package mx.edu.utng.cunasegura.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import mx.edu.utng.cunasegura.data.local.entity.ContactoEmergenciaEntity

/**
 * Data Access Object (DAO) para gestión CRUD de contactos de emergencia / confianza en SQLite.
 */
@Dao
interface ContactoDao {

    /**
     * Inserta un nuevo contacto de confianza.
     *
     * @param contacto Entidad del contacto.
     * @return Identificador generado en SQLite.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarContacto(contacto: ContactoEmergenciaEntity): Long

    /**
     * Consulta la lista de todos los contactos de emergencia asociados a un usuario.
     *
     * @param usuarioId Identificador del usuario.
     * @return Lista de [ContactoEmergenciaEntity].
     */
    @Query("SELECT * FROM contactos_emergencia WHERE usuarioId = :usuarioId ORDER BY creadoEn ASC")
    suspend fun obtenerPorUsuario(usuarioId: Int): List<ContactoEmergenciaEntity>

    /**
     * Actualiza la información de un contacto existente.
     *
     * @param contacto Entidad con datos actualizados.
     */
    @Update
    suspend fun actualizarContacto(contacto: ContactoEmergenciaEntity)

    /**
     * Elimina un contacto de la base de datos.
     *
     * @param contacto Entidad a eliminar.
     */
    @Delete
    suspend fun eliminarContacto(contacto: ContactoEmergenciaEntity)

    /**
     * Elimina todos los contactos asociados a un usuario específico.
     *
     * @param usuarioId Identificador del usuario.
     */
    @Query("DELETE FROM contactos_emergencia WHERE usuarioId = :usuarioId")
    suspend fun eliminarTodosPorUsuario(usuarioId: Int)

    /**
     * Emite un [Flow] reactivo con la lista actualizada de contactos de emergencia del usuario.
     *
     * @param usuarioId Identificador del usuario.
     * @return Flujo reactivo de lista de contactos.
     */
    @Query("SELECT * FROM contactos_emergencia WHERE usuarioId = :usuarioId ORDER BY creadoEn ASC")
    fun observarPorUsuario(usuarioId: Int): Flow<List<ContactoEmergenciaEntity>>

    /**
     * Elimina directamente un contacto por su identificador primario.
     *
     * @param id Identificador del contacto a remover.
     */
    @Query("DELETE FROM contactos_emergencia WHERE id = :id")
    suspend fun eliminarContactoPorId(id: Int)
}

