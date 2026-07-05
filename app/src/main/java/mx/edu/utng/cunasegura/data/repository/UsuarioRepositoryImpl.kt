package mx.edu.utng.cunasegura.data.repository

import mx.edu.utng.cunasegura.data.local.dao.UsuarioDao
import mx.edu.utng.cunasegura.data.local.entity.UsuarioEntity
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository

/**
 * Implementación concreta de [IUsuarioRepository] que usa Room como origen de datos local.
 *
 * Contiene los mappers Entity ↔ Domain para mantener aislada la capa de dominio
 * de los detalles de persistencia.
 */
class UsuarioRepositoryImpl(
    private val usuarioDao: UsuarioDao
) : IUsuarioRepository {

    // -------------------------------------------------------------------------
    // IUsuarioRepository
    // -------------------------------------------------------------------------

    override suspend fun guardarUsuario(usuario: Usuario) {
        // Si ya existe un registro con ese teléfono, se preserva su id original
        // para no generar un duplicado (REPLACE por id).
        val existente = usuarioDao.buscarPorTelefono(usuario.telefono)
        val entity = usuario.toEntity(idExistente = existente?.id)
        usuarioDao.insertarUsuario(entity)
    }

    override suspend fun buscarPorTelefono(telefono: String): Usuario? {
        return usuarioDao.buscarPorTelefono(telefono)?.toDomain()
    }

    // -------------------------------------------------------------------------
    // Mappers
    // -------------------------------------------------------------------------

    /**
     * Convierte un [Usuario] de dominio a [UsuarioEntity] de Room.
     * Si [idExistente] no es null, se usa ese id para evitar duplicados.
     */
    private fun Usuario.toEntity(idExistente: Int? = null): UsuarioEntity =
        UsuarioEntity(
            id = idExistente ?: this.id,
            nombre = this.nombre,
            telefono = this.telefono,
            consentimientoGps = this.consentimientoGps,
            latActual = this.latActual,
            lonActual = this.lonActual,
            fcmToken = this.fcmToken,
            tvVinculada = this.tvVinculada,
            esAdminGlobal = this.esAdminGlobal,
            estado = this.estado
        )

    /**
     * Convierte un [UsuarioEntity] de Room al modelo de dominio [Usuario].
     */
    private fun UsuarioEntity.toDomain(): Usuario =
        Usuario(
            id = this.id,
            nombre = this.nombre,
            telefono = this.telefono,
            consentimientoGps = this.consentimientoGps,
            latActual = this.latActual,
            lonActual = this.lonActual,
            fcmToken = this.fcmToken,
            tvVinculada = this.tvVinculada,
            esAdminGlobal = this.esAdminGlobal,
            estado = this.estado
        )
}
