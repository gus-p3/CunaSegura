package mx.edu.utng.cunasegura.domain.repository

import mx.edu.utng.cunasegura.domain.model.Usuario

/**
 * Contrato de repositorio para la gestión y autenticación de usuarios.
 *
 * Define las operaciones abstractas sobre datos de usuario que la capa de dominio consume,
 * aislando los detalles de Firebase Auth, Firebase Realtime Database y SQLite Room.
 */
interface IUsuarioRepository {

    /**
     * Persiste o actualiza un [Usuario] en el origen de datos.
     *
     * @param usuario Modelo de dominio del usuario a guardar.
     */
    suspend fun guardarUsuario(usuario: Usuario)

    /**
     * Busca un usuario por número de teléfono.
     *
     * @param telefono Número de teléfono de 10 dígitos.
     * @return [Usuario] coincidente o `null` si no existe.
     */
    suspend fun buscarPorTelefono(telefono: String): Usuario?

    /**
     * Valida credenciales de acceso para la cuenta de administrador.
     *
     * @param correo Correo electrónico del administrador.
     * @param password Contraseña de acceso.
     * @return [Usuario] si las credenciales son correctas y cuenta con rol `admin`, `null` en caso contrario.
     */
    suspend fun validarAdmin(correo: String, password: String): Usuario?

    /**
     * Valida credenciales de autenticación para cualquier usuario registrado.
     *
     * @param correo Correo electrónico.
     * @param password Contraseña de acceso.
     * @return [Usuario] autenticado o `null` si falló la validación.
     */
    suspend fun validarLogin(correo: String, password: String): Usuario?

    /**
     * Retorna todos los usuarios registrados en el sistema para uso de paneles administrativos.
     *
     * @return Lista completa de [Usuario].
     */
    suspend fun obtenerTodosLosUsuarios(): List<Usuario>

    /**
     * Retorna el usuario de la sesión activa en el dispositivo local.
     *
     * @return [Usuario] actual o `null` si no hay sesión abierta.
     */
    suspend fun obtenerUsuarioActual(): Usuario?

    /**
     * Actualiza el perfil de datos personales y credenciales del usuario autenticado.
     *
     * @param nombre Nuevo nombre completo.
     * @param telefono Nuevo teléfono de contacto.
     * @param nuevaPassword Nueva contraseña opcional.
     * @return [Result] con el resultado de la operación.
     */
    suspend fun actualizarPerfilUsuario(nombre: String, telefono: String, nuevaPassword: String?): Result<Unit>

    /**
     * Pega o limpia la sesión local del usuario para garantizar sesiones limpias.
     */
    suspend fun limpiarSesionLocal()
}

