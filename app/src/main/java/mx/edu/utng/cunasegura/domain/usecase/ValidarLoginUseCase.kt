package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository

/**
 * Caso de uso responsable de autenticar las credenciales de un usuario.
 *
 * @property repository Repositorio de usuarios.
 */
class ValidarLoginUseCase(
    private val repository: IUsuarioRepository
) {
    /**
     * Valida las credenciales de inicio de sesión de un usuario.
     *
     * @param correo Correo electrónico.
     * @param password Contraseña de acceso.
     * @return [Usuario] si las credenciales son válidas, `null` si son incorrectas.
     */
    suspend operator fun invoke(correo: String, password: String): Usuario? {
        return repository.validarLogin(correo, password)
    }
}

