package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository

/**
 * Caso de uso: busca un [Usuario] por su número de teléfono.
 *
 * @param repository Fuente de datos abstracta (no acoplada a Room).
 */
class ObtenerUsuarioUseCase(
    private val repository: IUsuarioRepository
) {
    /**
     * Ejecuta el caso de uso.
     * @param telefono Número de 10 dígitos a buscar.
     * @return [Usuario] si existe en el repositorio, `null` si no hay registro.
     */
    suspend operator fun invoke(telefono: String): Usuario? {
        return repository.buscarPorTelefono(telefono)
    }
}
