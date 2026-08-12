package mx.edu.utng.cunasegura.domain.usecase

import kotlinx.coroutines.flow.Flow
import mx.edu.utng.cunasegura.domain.model.ContactoEmergencia
import mx.edu.utng.cunasegura.domain.repository.IContactoRepository

/**
 * Caso de uso responsable de obtener y observar de forma reactiva la lista de contactos de emergencia.
 *
 * @property repository Repositorio de contactos.
 */
class ObtenerContactosUseCase(
    private val repository: IContactoRepository
) {
    /**
     * Retorna el flujo reactivo con los contactos de emergencia del usuario.
     *
     * @param usuarioId Identificador del usuario.
     * @return [Flow] reactivo con la lista de [ContactoEmergencia].
     */
    operator fun invoke(usuarioId: Int): Flow<List<ContactoEmergencia>> {
        return repository.obtenerContactos(usuarioId)
    }
}