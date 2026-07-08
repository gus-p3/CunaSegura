package mx.edu.utng.cunasegura.domain.repository

import mx.edu.utng.cunasegura.domain.model.Usuario

/**
 * Contrato de repositorio para la gestión de usuarios.
 * Las implementaciones viven en la capa de datos; el dominio sólo conoce esta interfaz.
 */
interface IUsuarioRepository {

    /**
     * Persiste o actualiza un [Usuario] en el origen de datos.
     * Si el usuario ya existe (mismo teléfono), actualiza su registro sin duplicar.
     */
    suspend fun guardarUsuario(usuario: Usuario)

    /**
     * Busca un usuario por número de teléfono.
     * @return [Usuario] si existe, `null` si no hay registro con ese número.
     */
    suspend fun buscarPorTelefono(telefono: String): Usuario?

    /**
     * Valida credenciales de administrador.
     * @return [Usuario] con [Usuario.esAdminGlobal] = true si las credenciales son correctas, null si no.
     */
    suspend fun validarAdmin(correo: String, password: String): Usuario?

    /**
     * Retorna todos los usuarios registrados. Solo para uso del administrador.
     */
    suspend fun obtenerTodosLosUsuarios(): List<Usuario>

    /** Retorna el usuario de la sesión activa en este dispositivo, o null si no hay ninguno. */
    suspend fun obtenerUsuarioActual(): Usuario?
}
