package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.repository.IContactoRepository

/**
 * Caso de uso responsable de dar de baja a un contacto de confianza por su identificador.
 *
 * @property repository Repositorio de contactos de emergencia.
 */
class EliminarContactoUseCase(
    private val repository: IContactoRepository
) {
    /**
     * Ejecuta la eliminación del contacto.
     *
     * @param id Identificador único del contacto a eliminar.
     */
    suspend operator fun invoke(id: Int) {
        repository.eliminarContacto(id)
    }
}