package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.model.ContactoEmergencia
import mx.edu.utng.cunasegura.domain.repository.IContactoRepository

/**
 * Caso de uso responsable de registrar o actualizar un contacto de confianza en el directorio de emergencias.
 *
 * @property repository Repositorio de contactos de emergencia.
 */
class AgregarContactoUseCase(
    private val repository: IContactoRepository
) {
    /**
     * Ejecuta el guardado del contacto de emergencia.
     *
     * @param contacto Modelo del contacto de confianza a persistir.
     */
    suspend operator fun invoke(contacto: ContactoEmergencia) {
        repository.agregarContacto(contacto)
    }
}