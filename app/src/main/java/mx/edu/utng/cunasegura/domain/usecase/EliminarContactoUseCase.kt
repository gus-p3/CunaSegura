package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.repository.IContactoRepository

class EliminarContactoUseCase(
    private val repository: IContactoRepository
) {
    suspend operator fun invoke(id: Int) {
        repository.eliminarContacto(id)
    }
}