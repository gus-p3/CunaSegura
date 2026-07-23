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
import kotlinx.coroutines.tasks.await

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
        var id = System.currentTimeMillis() % 1000000
        try {
            // Verificar si el usuario ya tiene una alerta activa reciente (últimos 45s) para evitar duplicados por GPS o pulsaciones múltiples
            val alertaActiva = alertaDao.buscarAlertaActivaPorUsuario(alerta.usuarioId)
            val esReciente = alertaActiva != null && (Math.abs(System.currentTimeMillis() - alertaActiva.creadoEn) < 45000)
            
            val entityToInsert = if (esReciente && alertaActiva != null) {
                id = alertaActiva.id.toLong()
                alerta.copy(id = alertaActiva.id, creadoEn = alertaActiva.creadoEn).toEntity()
            } else {
                alerta.toEntity()
            }

            val insertedId = alertaDao.insertarAlerta(entityToInsert)
            if (insertedId > 0 && !esReciente) {
                id = insertedId
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error insertando alerta local en Room DB", e)
        }
        val alertaConId = alerta.copy(id = id.toInt())
        
        // Obtener nombreUsuario y networkId reales del usuario actual si vienen incompletos
        var nombreFinal = alertaConId.nombreUsuario
        var networkId = alertaConId.networkId
        try {
            val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                val userSnap = FirebaseDatabase.getInstance().getReference("usuarios")
                    .child(firebaseUser.uid).get().await()
                if (userSnap.exists()) {
                    if (nombreFinal.isBlank() || nombreFinal == "Vecino") {
                        nombreFinal = userSnap.child("nombre").getValue(String::class.java)
                            ?: firebaseUser.displayName
                            ?: firebaseUser.email?.substringBefore("@")
                            ?: "Vecino"
                    }
                    if (networkId.isEmpty()) {
                        networkId = userSnap.child("networkId").getValue(String::class.java) ?: firebaseUser.uid
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener datos del usuario para la alerta", e)
        }
        if (nombreFinal.isBlank()) nombreFinal = "Vecino"

        // Sync to Firebase Realtime Database
        val map = mapOf(
            "id" to alertaConId.id,
            "usuarioId" to alertaConId.usuarioId,
            "nombreUsuario" to nombreFinal,
            "estado" to alertaConId.estado,
            "latitud" to alertaConId.latitud,
            "longitud" to alertaConId.longitud,
            "fueAtendida" to alertaConId.fueAtendida,
            "esFalsaAlarma" to alertaConId.esFalsaAlarma,
            "creadoEn" to alertaConId.creadoEn,
            "networkId" to networkId
        )
        dbRef.child(alertaConId.id.toString()).setValue(map).addOnFailureListener {
            Log.e(TAG, "Error sync Firebase: crearAlerta", it)
        }

        // Publicar por MQTT para que las TVs de la red vecinal la reciban e inicien alarma
        mx.edu.utng.cunasegura.mqtt.MqttPublisher.publishAlertaTv(
            usuarioId = alertaConId.usuarioId,
            nombreUsuario = nombreFinal,
            lat = alertaConId.latitud,
            lon = alertaConId.longitud,
            networkId = networkId,
            estado = "activa"
        )
        
        return id
    }

    override suspend fun cancelarAlerta(id: Int) {
        try {
            alertaDao.actualizarEstado(id = id, estado = "cancelada")
            
            // Sync to Firebase
            dbRef.child(id.toString()).child("estado").setValue("cancelada").addOnFailureListener {
                Log.e(TAG, "Error sync Firebase: cancelarAlerta", it)
            }

            // Notificar cancelación por MQTT a las Smart TVs
            var networkId = ""
            var usuarioIdInt = 1
            var nombre = "Vecino"
            var lat = 0.0
            var lon = 0.0

            val alerta = alertaDao.buscarPorId(id)
            if (alerta != null) {
                usuarioIdInt = alerta.usuarioId
                nombre = alerta.nombreUsuario
                lat = alerta.latitud
                lon = alerta.longitud
            }

            try {
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    val netSnap = FirebaseDatabase.getInstance().getReference("usuarios")
                        .child(firebaseUser.uid).child("networkId").get().await()
                    networkId = netSnap.getValue(String::class.java) ?: firebaseUser.uid
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener networkId para cancelar MQTT", e)
            }

            mx.edu.utng.cunasegura.mqtt.MqttPublisher.publishAlertaTv(
                usuarioId = usuarioIdInt,
                nombreUsuario = nombre,
                lat = lat,
                lon = lon,
                networkId = networkId,
                estado = "cancelada"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error en cancelarAlerta", e)
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
                            val rawUsuarioId = child.child("usuarioId").value
                            val usuarioIdInt = when (rawUsuarioId) {
                                is Long -> rawUsuarioId.toInt()
                                is Int -> rawUsuarioId
                                is String -> rawUsuarioId.toIntOrNull() ?: 0
                                else -> 0
                            }
                            val netId = child.child("networkId").getValue(String::class.java) ?: ""

                            alertas.add(
                                Alerta(
                                    id = child.child("id").getValue(Int::class.java) ?: 0,
                                    usuarioId = usuarioIdInt,
                                    nombreUsuario = child.child("nombreUsuario").getValue(String::class.java) ?: "Vecino",
                                    estado = estado,
                                    latitud = child.child("latitud").getValue(Double::class.java) ?: 0.0,
                                    longitud = child.child("longitud").getValue(Double::class.java) ?: 0.0,
                                    fueAtendida = child.child("fueAtendida").getValue(Boolean::class.java) ?: false,
                                    esFalsaAlarma = child.child("esFalsaAlarma").getValue(Boolean::class.java) ?: false,
                                    creadoEn = child.child("creadoEn").getValue(Long::class.java) ?: 0L,
                                    networkId = netId
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

    override suspend fun obtenerTodasLasAlertas(): List<Alerta> {
        return try {
            val snapshot = dbRef.get().await()
            val list = mutableListOf<Alerta>()
            for (child in snapshot.children) {
                val alerta = Alerta(
                    id = child.child("id").getValue(Int::class.java) ?: 0,
                    usuarioId = child.child("usuarioId").getValue(Int::class.java) ?: 0,
                    nombreUsuario = child.child("nombreUsuario").getValue(String::class.java) ?: "Vecino",
                    estado = child.child("estado").getValue(String::class.java) ?: "",
                    latitud = child.child("latitud").getValue(Double::class.java) ?: 0.0,
                    longitud = child.child("longitud").getValue(Double::class.java) ?: 0.0,
                    fueAtendida = child.child("fueAtendida").getValue(Boolean::class.java) ?: false,
                    esFalsaAlarma = child.child("esFalsaAlarma").getValue(Boolean::class.java) ?: false,
                    creadoEn = child.child("creadoEn").getValue(Long::class.java) ?: 0L
                )
                list.add(alerta)
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all alerts: ${e.message}", e)
            emptyList()
        }
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
