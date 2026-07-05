package mx.edu.utng.cunasegura.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mx.edu.utng.cunasegura.data.local.entity.UsuarioEntity

/**
 * DAO para operaciones sobre la tabla de usuarios.
 */
@Dao
interface UsuarioDao {

    /**
     * Inserta un usuario. Si ya existe (mismo id), lo reemplaza.
     * @return el rowId del usuario insertado.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: UsuarioEntity): Long

    /**
     * Busca un usuario por número de teléfono.
     * @return UsuarioEntity si existe, null si no.
     */
    @Query("SELECT * FROM usuarios WHERE telefono = :telefono LIMIT 1")
    suspend fun buscarPorTelefono(telefono: String): UsuarioEntity?
}
