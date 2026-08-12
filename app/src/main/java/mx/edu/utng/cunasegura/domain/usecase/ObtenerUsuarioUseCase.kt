package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository

/**
 * Caso de uso responsable de buscar un [Usuario] por su número de teléfono celular.
 *
 * @property repository Fuente de datos abstracta de usuarios.
 */
class ObtenerUsuarioUseCase(
    private val repository: IUsuarioRepository
) {
    /**
     * Ejecuta la búsqueda de usuario.
     *
     * @param telefono Número telefónico de 10 dígitos.
     * @return [Usuario] si existe en el repositorio o `null`.
     */
    suspend operator fun invoke(telefono: String): Usuario? {
        return repository.buscarPorTelefono(telefono)
    }
}

