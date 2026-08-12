package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository

/**
 * Caso de uso responsable de validar las credenciales del administrador global del sistema.
 *
 * @property repository Repositorio de usuarios.
 */
class ValidarAdminUseCase(
    private val repository: IUsuarioRepository
) {
    /**
     * Valida si las credenciales corresponden a una cuenta con privilegios administrativos.
     *
     * @param correo Correo electrónico del administrador.
     * @param password Contraseña de acceso.
     * @return [Usuario] admin si es válido, `null` si falla.
     */
    suspend operator fun invoke(correo: String, password: String): Usuario? {
        return repository.validarAdmin(correo, password)
    }
}

