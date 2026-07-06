package mx.edu.utng.cunasegura.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import mx.edu.utng.cunasegura.data.local.dao.AlertaDao
import mx.edu.utng.cunasegura.data.local.db.AppDatabase
import mx.edu.utng.cunasegura.data.local.entity.AlertaEntity
import mx.edu.utng.cunasegura.domain.model.Alerta
import mx.edu.utng.cunasegura.domain.repository.IAlertaRepository

/**
 * Implementación de [IAlertaRepository] que interactúa con Room.
 */
class AlertaRepositoryImpl(
    private val database: AppDatabase,
    private val alertaDao: AlertaDao
) : IAlertaRepository {

    override suspend fun crearAlerta(alerta: Alerta): Long {
        val entity = alerta.toEntity()
        return alertaDao.insertarAlerta(entity)
    }

    override suspend fun cancelarAlerta(id: Int) {
        val alertaExistente = obtenerAlertaPorId(id)
        if (alertaExistente != null) {
            val alertaCancelada = alertaExistente.copy(estado = "cancelada")
            alertaDao.insertarAlerta(alertaCancelada.toEntity())
        }
    }

    override suspend fun obtenerAlertaPorId(id: Int): Alerta? {
        val db = database.openHelper.readableDatabase
        val cursor = db.query("SELECT * FROM alertas WHERE id = ?", arrayOf(id))
        var alerta: Alerta? = null
        try {
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex("id")
                val usuarioIdIndex = cursor.getColumnIndex("usuarioId")
                val estadoIndex = cursor.getColumnIndex("estado")
                val latitudIndex = cursor.getColumnIndex("latitud")
                val longitudIndex = cursor.getColumnIndex("longitud")
                val fueAtendidaIndex = cursor.getColumnIndex("fueAtendida")
                val esFalsaAlarmaIndex = cursor.getColumnIndex("esFalsaAlarma")
                val creadoEnIndex = cursor.getColumnIndex("creadoEn")

                if (idIndex >= 0 && usuarioIdIndex >= 0 && estadoIndex >= 0 &&
                    latitudIndex >= 0 && longitudIndex >= 0 && fueAtendidaIndex >= 0 &&
                    esFalsaAlarmaIndex >= 0 && creadoEnIndex >= 0
                ) {
                    alerta = Alerta(
                        id = cursor.getInt(idIndex),
                        usuarioId = cursor.getInt(usuarioIdIndex),
                        estado = cursor.getString(estadoIndex) ?: "activa",
                        latitud = cursor.getDouble(latitudIndex),
                        longitud = cursor.getDouble(longitudIndex),
                        fueAtendida = cursor.getInt(fueAtendidaIndex) == 1,
                        esFalsaAlarma = cursor.getInt(esFalsaAlarmaIndex) == 1,
                        creadoEn = cursor.getLong(creadoEnIndex)
                    )
                }
            }
        } finally {
            cursor.close()
        }
        return alerta
    }

    override fun obtenerAlertaActiva(usuarioId: Int): Flow<Alerta?> {
        return alertaDao.obtenerAlertasActivas().map { list ->
            list.firstOrNull { it.usuarioId == usuarioId && it.estado == "activa" }?.toDomain()
        }
    }

    // -------------------------------------------------------------------------
    // Mappers
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
