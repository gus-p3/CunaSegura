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
     * @return [UsuarioEntity] si existe, null si no.
     */
    @Query("SELECT * FROM usuarios WHERE telefono = :telefono LIMIT 1")
    suspend fun buscarPorTelefono(telefono: String): UsuarioEntity?

    /**
     * Busca un usuario por correo electrónico.
     * Usado para el login de administrador.
     */
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    /**
     * Valida credenciales de administrador por correo y contraseña.
     * @return [UsuarioEntity] si las credenciales son correctas, null si no.
     */
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND password = :password AND rol = 'admin' LIMIT 1")
    suspend fun validarAdmin(correo: String, password: String): UsuarioEntity?

    /**
     * Valida credenciales de login genérico por correo y contraseña.
     * @return [UsuarioEntity] si las credenciales son correctas, null si no.
     */
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND password = :password LIMIT 1")
    suspend fun validarLogin(correo: String, password: String): UsuarioEntity?

    /**
     * Retorna el primer usuario registrado en el dispositivo (sesión activa).
     * Como esta app maneja un solo usuario por teléfono, sirve para el Splash.
     */
    @Query("SELECT * FROM usuarios LIMIT 1")
    suspend fun obtenerUsuarioActual(): UsuarioEntity?

    /**
     * Retorna todos los usuarios registrados. Solo accesible para el admin.
     */
    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    suspend fun obtenerTodosLosUsuarios(): List<UsuarioEntity>

    /**
     * Cuenta el total de usuarios registrados.
     */
    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int

    /**
     * Elimina todos los usuarios locales.
     * Útil para limpiar la sesión antes de un nuevo login.
     */
    @Query("DELETE FROM usuarios")
    suspend fun eliminarTodos()
}
