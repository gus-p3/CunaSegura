package mx.edu.utng.cunasegura.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import mx.edu.utng.cunasegura.data.local.entity.ContactoEmergenciaEntity

/**
 * DAO con CRUD completo para la tabla de contactos de emergencia.
 */
@Dao
interface ContactoDao {

    /** Inserta un nuevo contacto de emergencia. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarContacto(contacto: ContactoEmergenciaEntity): Long

    /** Retorna todos los contactos de un usuario dado su [usuarioId]. */
    @Query("SELECT * FROM contactos_emergencia WHERE usuarioId = :usuarioId ORDER BY creadoEn ASC")
    suspend fun obtenerPorUsuario(usuarioId: Int): List<ContactoEmergenciaEntity>

    /** Actualiza los datos de un contacto existente. */
    @Update
    suspend fun actualizarContacto(contacto: ContactoEmergenciaEntity)

    /** Elimina un contacto de emergencia. */
    @Delete
    suspend fun eliminarContacto(contacto: ContactoEmergenciaEntity)

    /** Elimina todos los contactos de un usuario dado su [usuarioId]. */
    @Query("DELETE FROM contactos_emergencia WHERE usuarioId = :usuarioId")
    suspend fun eliminarTodosPorUsuario(usuarioId: Int)
}
