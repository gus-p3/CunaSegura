package mx.edu.utng.cunasegura.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.edu.utng.cunasegura.data.local.dao.AlertaDao
import mx.edu.utng.cunasegura.data.local.entity.AlertaEntity
import mx.edu.utng.cunasegura.domain.model.Alerta
import mx.edu.utng.cunasegura.domain.repository.IAlertaRepository

/**
 * Implementación de [IAlertaRepository] que interactúa con Room a través de [AlertaDao].
 * No usa SQL raw — toda la persistencia pasa por el DAO.
 */
class AlertaRepositoryImpl(
    private val alertaDao: AlertaDao
) : IAlertaRepository {

    // -------------------------------------------------------------------------
    // IAlertaRepository
    // -------------------------------------------------------------------------

    override suspend fun crearAlerta(alerta: Alerta): Long {
        return alertaDao.insertarAlerta(alerta.toEntity())
    }

    /**
     * Cancela la alerta actualizando solo su campo [estado] a 'cancelada'.
     * Usa UPDATE en lugar de INSERT REPLACE para preservar el id original.
     */
    override suspend fun cancelarAlerta(id: Int) {
        alertaDao.actualizarEstado(id = id, estado = "cancelada")
    }

    override suspend fun obtenerAlertaPorId(id: Int): Alerta? {
        return alertaDao.buscarPorId(id)?.toDomain()
    }

    /**
     * Observa reactivamente la alerta activa más reciente del usuario dado.
     * La query filtra directamente en SQLite: estado = 'activa'.
     */
    override fun obtenerAlertaActiva(usuarioId: Int): Flow<Alerta?> {
        return alertaDao.obtenerAlertaActivaPorUsuario(usuarioId).map { entity ->
            entity?.toDomain()
        }
    }

    // -------------------------------------------------------------------------
    // Mappers Entity <-> Domain
    // -------------------------------------------------------------------------

    private fun Alerta.toEntity(): AlertaEntity =
        AlertaEntity(
            id = this.id,
            usuarioId = this.usuarioId,
            estado = this.estado,
            latitud = this.latitud,
            longitud = this.longitud,
            fueAtendida = this.fueAtendida,
            esFalsaAlarma = this.esFalsaAlarma,
            creadoEn = this.creadoEn
        )

    private fun AlertaEntity.toDomain(): Alerta =
        Alerta(
            id = this.id,
            usuarioId = this.usuarioId,
            estado = this.estado,
            latitud = this.latitud,
            longitud = this.longitud,
            fueAtendida = this.fueAtendida,
            esFalsaAlarma = this.esFalsaAlarma,
            creadoEn = this.creadoEn
        )
}
