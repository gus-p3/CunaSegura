package mx.edu.utng.cunasegura.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.edu.utng.cunasegura.domain.model.ContactoEmergencia

/**
 * Contrato de repositorio para la gestión reactiva de contactos de emergencia.
 */
interface IContactoRepository {

    /**
     * Inserta un nuevo contacto de confianza.
     *
     * @param contacto Modelo de contacto a registrar.
     */
    suspend fun agregarContacto(contacto: ContactoEmergencia)

    /**
     * Elimina un contacto por su identificador primario.
     *
     * @param id Identificador del contacto.
     */
    suspend fun eliminarContacto(id: Int)

    /**
     * Observa en tiempo real la lista de contactos de confianza de un usuario.
     *
     * @param usuarioId Identificador del usuario.
     * @return [Flow] reactivo con la lista de contactos.
     */
    fun obtenerContactos(usuarioId: Int): Flow<List<ContactoEmergencia>>
}