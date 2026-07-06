package mx.edu.utng.cunasegura.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.edu.utng.cunasegura.data.local.dao.ContactoDao
import mx.edu.utng.cunasegura.data.local.entity.ContactoEmergenciaEntity
import mx.edu.utng.cunasegura.domain.model.ContactoEmergencia
import mx.edu.utng.cunasegura.domain.repository.IContactoRepository

class ContactoRepositoryImpl(
    private val contactoDao: ContactoDao
) : IContactoRepository {

    override suspend fun agregarContacto(contacto: ContactoEmergencia) {
        contactoDao.insertarContacto(contacto.toEntity())
    }

    override suspend fun eliminarContacto(id: Int) {
        // Room necesita la entidad completa para @Delete; buscamos primero.
        // Como no tenemos un buscarPorId, lo hacemos vía la lista del usuario.
        // Alternativa simple: agregamos abajo un helper.
        eliminarPorId(id)
    }

    override fun obtenerContactos(usuarioId: Int): Flow<List<ContactoEmergencia>> {
        return contactoDao.observarPorUsuario(usuarioId).map { lista ->
            lista.map { it.toDomain() }
        }
    }

    // -------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------

    private suspend fun eliminarPorId(id: Int) {
        contactoDao.eliminarContactoPorId(id)
    }

    private fun ContactoEmergencia.toEntity() = ContactoEmergenciaEntity(
        id = this.id,
        usuarioId = this.usuarioId,
        nombre = this.nombre,
        telefono = this.telefono,
        relacion = this.relacion,
        creadoEn = this.creadoEn
    )

    private fun ContactoEmergenciaEntity.toDomain() = ContactoEmergencia(
        id = this.id,
        usuarioId = this.usuarioId,
        nombre = this.nombre,
        telefono = this.telefono,
        relacion = this.relacion,
        creadoEn = this.creadoEn
    )
}