package mx.edu.utng.cunasegura.domain.repository

import kotlinx.coroutines.flow.Flow
import mx.edu.utng.cunasegura.domain.model.ContactoEmergencia

interface IContactoRepository {

    /** Inserta un nuevo contacto de emergencia. */
    suspend fun agregarContacto(contacto: ContactoEmergencia)

    /** Elimina un contacto por su id. */
    suspend fun eliminarContacto(id: Int)

    /** Observa en tiempo real los contactos de un usuario (se actualiza sola con Room + Flow). */
    fun obtenerContactos(usuarioId: Int): Flow<List<ContactoEmergencia>>
}