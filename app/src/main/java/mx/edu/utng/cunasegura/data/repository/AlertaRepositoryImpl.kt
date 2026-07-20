package mx.edu.utng.cunasegura.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import mx.edu.utng.cunasegura.data.local.dao.AlertaDao
import mx.edu.utng.cunasegura.data.local.entity.AlertaEntity
import mx.edu.utng.cunasegura.domain.model.Alerta
import mx.edu.utng.cunasegura.domain.repository.IAlertaRepository
import android.util.Log

/**
 * Implementación de [IAlertaRepository] que interactúa con Room a través de [AlertaDao]
 * y sincroniza las alertas con Firebase Realtime Database.
 */
class AlertaRepositoryImpl(
    private val alertaDao: AlertaDao
) : IAlertaRepository {

    private val dbRef = FirebaseDatabase.getInstance().getReference("alertas")
    private val TAG = "AlertaRepository"

    override suspend fun crearAlerta(alerta: Alerta): Long {
        val id = alertaDao.insertarAlerta(alerta.toEntity())
        val alertaConId = alerta.copy(id = id.toInt())
        
        // Sync to Firebase
        val map = mapOf(
            "id" to alertaConId.id,
            "usuarioId" to alertaConId.usuarioId,
            "nombreUsuario" to alertaConId.nombreUsuario,
            "estado" to alertaConId.estado,
            "latitud" to alertaConId.latitud,
            "longitud" to alertaConId.longitud,
            "fueAtendida" to alertaConId.fueAtendida,
            "esFalsaAlarma" to alertaConId.esFalsaAlarma,
            "creadoEn" to alertaConId.creadoEn
        )
        dbRef.child(alertaConId.id.toString()).setValue(map).addOnFailureListener {
            Log.e(TAG, "Error sync Firebase: crearAlerta", it)
        }
        
        return id
    }

    override suspend fun cancelarAlerta(id: Int) {
        alertaDao.actualizarEstado(id = id, estado = "cancelada")
        
        // Sync to Firebase
        dbRef.child(id.toString()).child("estado").setValue("cancelada").addOnFailureListener {
            Log.e(TAG, "Error sync Firebase: cancelarAlerta", it)
        }
    }

    override suspend fun obtenerAlertaPorId(id: Int): Alerta? {
        return alertaDao.buscarPorId(id)?.toDomain()
    }

    override fun obtenerAlertaActiva(usuarioId: Int): Flow<Alerta?> {
        return alertaDao.obtenerAlertaActivaPorUsuario(usuarioId).map { entity ->
            entity?.toDomain()
        }
    }

    override fun obtenerAlertasVecinalesActivas(): Flow<List<Alerta>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alertas = mutableListOf<Alerta>()
                for (child in snapshot.children) {
                    try {
                        val estado = child.child("estado").getValue(String::class.java) ?: ""
                        if (estado == "activa") {
                            alertas.add(
                                Alerta(
                                    id = child.child("id").getValue(Int::class.java) ?: 0,
                                    usuarioId = child.child("usuarioId").getValue(Int::class.java) ?: 0,
                                    estado = estado,
                                    latitud = child.child("latitud").getValue(Double::class.java) ?: 0.0,
                                    longitud = child.child("longitud").getValue(Double::class.java) ?: 0.0,
                                    fueAtendida = child.child("fueAtendida").getValue(Boolean::class.java) ?: false,
                                    esFalsaAlarma = child.child("esFalsaAlarma").getValue(Boolean::class.java) ?: false,
                                    creadoEn = child.child("creadoEn").getValue(Long::class.java) ?: 0L
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing alerta", e)
                    }
                }
                trySend(alertas)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase listener cancelled", error.toException())
            }
        }

        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }

    // -------------------------------------------------------------------------
    // Mappers Entity <-> Domain
    // -------------------------------------------------------------------------

    private fun Alerta.toEntity(): AlertaEntity =
        AlertaEntity(
            id = this.id,
            usuarioId = this.usuarioId,
            nombreUsuario = this.nombreUsuario,
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
            nombreUsuario = this.nombreUsuario,
            estado = this.estado,
            latitud = this.latitud,
            longitud = this.longitud,
            fueAtendida = this.fueAtendida,
            esFalsaAlarma = this.esFalsaAlarma,
            creadoEn = this.creadoEn
        )
}
