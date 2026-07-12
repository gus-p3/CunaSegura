package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository

/**
 * Caso de uso: Valida las credenciales de un usuario.
 * Retorna el [Usuario] si las credenciales son correctas, null si no.
 */
class ValidarLoginUseCase(
    private val repository: IUsuarioRepository
) {
    suspend operator fun invoke(correo: String, password: String): Usuario? {
        return repository.validarLogin(correo, password)
    }
}
