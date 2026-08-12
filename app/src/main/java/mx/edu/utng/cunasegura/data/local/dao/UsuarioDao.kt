package mx.edu.utng.cunasegura.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import mx.edu.utng.cunasegura.data.local.entity.UsuarioEntity

/**
 * Data Access Object (DAO) para operaciones de persistencia en la tabla `usuarios`.
 */
@Dao
interface UsuarioDao {

    /**
     * Inserta un usuario en la base de datos local. Si ya existe, reemplaza el registro.
     *
     * @param usuario Entidad del usuario a persistir.
     * @return El rowId asignado en la base de datos SQLite.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarUsuario(usuario: UsuarioEntity): Long

    /**
     * Busca un usuario por su número de teléfono.
     *
     * @param telefono Número de teléfono de 10 dígitos.
     * @return [UsuarioEntity] coincidente o `null` si no existe.
     */
    @Query("SELECT * FROM usuarios WHERE telefono = :telefono LIMIT 1")
    suspend fun buscarPorTelefono(telefono: String): UsuarioEntity?

    /**
     * Busca un usuario por su dirección de correo electrónico.
     *
     * @param correo Dirección de correo electrónico registrada.
     * @return [UsuarioEntity] coincidente o `null` si no se encuentra.
     */
    @Query("SELECT * FROM usuarios WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): UsuarioEntity?

    /**
     * Valida credenciales de autenticación para administradores con rol `admin`.
     *
     * @param correo Correo electrónico del administrador.
     * @param password Contraseña de acceso.
     * @return [UsuarioEntity] del administrador si las credenciales son válidas, `null` en caso contrario.
     */
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND password = :password AND rol = 'admin' LIMIT 1")
    suspend fun validarAdmin(correo: String, password: String): UsuarioEntity?

    /**
     * Valida credenciales de inicio de sesión genéricas para cualquier usuario.
     *
     * @param correo Correo electrónico del usuario.
     * @param password Contraseña de acceso.
     * @return [UsuarioEntity] si coincide, `null` si las credenciales son erróneas.
     */
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND password = :password LIMIT 1")
    suspend fun validarLogin(correo: String, password: String): UsuarioEntity?

    /**
     * Obtiene el usuario local actualmente guardado (sesión activa del dispositivo).
     *
     * @return [UsuarioEntity] del usuario principal local o `null` si no hay sesión iniciada.
     */
    @Query("SELECT * FROM usuarios LIMIT 1")
    suspend fun obtenerUsuarioActual(): UsuarioEntity?

    /**
     * Retorna la lista completa de usuarios registrados localmente, ordenada alfabéticamente.
     *
     * @return Lista de [UsuarioEntity].
     */
    @Query("SELECT * FROM usuarios ORDER BY nombre ASC")
    suspend fun obtenerTodosLosUsuarios(): List<UsuarioEntity>

    /**
     * Cuenta el total de registros de usuarios en la tabla local.
     *
     * @return Cantidad entera de usuarios.
     */
    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contarUsuarios(): Int

    /**
     * Elimina todos los registros de usuarios locales para cerrar sesión limpiamente.
     */
    @Query("DELETE FROM usuarios")
    suspend fun eliminarTodos()
}

