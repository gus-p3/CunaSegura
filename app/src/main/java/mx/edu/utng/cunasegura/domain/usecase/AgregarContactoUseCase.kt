package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.model.ContactoEmergencia
import mx.edu.utng.cunasegura.domain.repository.IContactoRepository

class AgregarContactoUseCase(
    private val repository: IContactoRepository
) {
    suspend operator fun invoke(contacto: ContactoEmergencia) {
        repository.agregarContacto(contacto)
    }
}