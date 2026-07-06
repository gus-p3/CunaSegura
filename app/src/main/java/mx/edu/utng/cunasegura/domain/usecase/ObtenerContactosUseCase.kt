package mx.edu.utng.cunasegura.domain.usecase

import kotlinx.coroutines.flow.Flow
import mx.edu.utng.cunasegura.domain.model.ContactoEmergencia
import mx.edu.utng.cunasegura.domain.repository.IContactoRepository

class ObtenerContactosUseCase(
    private val repository: IContactoRepository
) {
    operator fun invoke(usuarioId: Int): Flow<List<ContactoEmergencia>> {
        return repository.obtenerContactos(usuarioId)
    }
}