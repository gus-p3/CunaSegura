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
 * Implementación híbrida de [IAlertaRepository] que persiste localmente en SQLite Room mediante [AlertaDao],
 * sincroniza en tiempo real con Firebase Realtime Database y dispara mensajes de emergencia MQTT a Smart TVs.
 *
 * @property alertaDao DAO de alertas para persistencia local offline-first.
 */
class AlertaRepositoryImpl(
    private val alertaDao: AlertaDao
) : IAlertaRepository {

    private val dbRef = FirebaseDatabase.getInstance().getReference("alertas")
    private val TAG = "AlertaRepository"

    /**
     * Inserta una alerta SOS en la base de datos local y la publica tanto en Firebase como en el broker MQTT.
     *
     * @param alerta Datos de la alerta emitida.
     * @return Identificador asignado a la alerta.
     */
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
                    val realName = userSnap.child("nombre").getValue(String::class.java)
                        ?: firebaseUser.displayName
                        ?: firebaseUser.email?.substringBefore("@")
                    if (!realName.isNullOrBlank()) {
                        nombreFinal = realName
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

    /**
     * Cancela la alerta tanto en Room como en Firebase y notifica el cese de alarma a las Smart TVs vía MQTT.
     *
     * @param id Identificador de la alerta a cancelar.
     */
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

    /**
     * Busca una alerta en la base de datos local por su ID.
     *
     * @param id Identificador de la alerta.
     * @return [Alerta] si existe o `null`.
     */
    override suspend fun obtenerAlertaPorId(id: Int): Alerta? {
        return alertaDao.buscarPorId(id)?.toDomain()
    }

    /**
     * Observa de forma reactiva la alerta activa del usuario especificado.
     *
     * @param usuarioId ID del usuario.
     * @return [Flow] con la entidad [Alerta] o `null`.
     */
    override fun obtenerAlertaActiva(usuarioId: Int): Flow<Alerta?> {
        return alertaDao.obtenerAlertaActivaPorUsuario(usuarioId).map { entity ->
            entity?.toDomain()
        }
    }

    /**
     * Escucha en tiempo real todas las alertas activas en Firebase y aplica el filtro temporal `tiempoVidaAlerta`.
     *
     * @return [Flow] reactivo con la lista de alertas vigentes.
     */
    override fun obtenerAlertasVecinalesActivas(): Flow<List<Alerta>> = callbackFlow {
        // Fetch config once when flow starts
        var tiempoVidaMs = 720L * 60 * 1000 // 720 minutes default (12h)
        FirebaseDatabase.getInstance().getReference("configuracion_global").child("tiempoVidaAlerta").get().addOnSuccessListener { snap ->
            val minutos = snap.getValue(Double::class.java) ?: 720.0
            tiempoVidaMs = (minutos * 60 * 1000).toLong()
        }

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
                // Filtrar por tiempoVidaAlerta
                val ahora = System.currentTimeMillis()
                val activas = alertas.filter { alerta ->
                    (ahora - alerta.creadoEn) <= tiempoVidaMs
                }
                trySend(activas)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase listener cancelled", error.toException())
            }
        }

        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }

    /**
     * Consulta el catálogo total de alertas históricas en Firebase.
     *
     * @return Lista completa de [Alerta].
     */
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

    /**
     * Consulta las alertas asociadas a una red vecinal comunitaria filtradas por su vigencia.
     *
     * @param networkId Identificador de la red vecinal.
     * @return Lista de alertas de la comunidad ordenadas cronológicamente.
     */
    override suspend fun obtenerAlertasPorNetworkId(networkId: String): List<Alerta> {
        return try {
            val snapshot = dbRef.get().await()
            val list = mutableListOf<Alerta>()
            for (child in snapshot.children) {
                val netId = child.child("networkId").getValue(String::class.java) ?: ""
                if (netId == networkId || networkId.isBlank()) {
                    val rawUsuarioId = child.child("usuarioId").value
                    val usuarioIdInt = when (rawUsuarioId) {
                        is Long -> rawUsuarioId.toInt()
                        is Int -> rawUsuarioId
                        is String -> rawUsuarioId.toIntOrNull() ?: 0
                        else -> 0
                    }
                    val alerta = Alerta(
                        id = child.child("id").getValue(Int::class.java) ?: 0,
                        usuarioId = usuarioIdInt,
                        nombreUsuario = child.child("nombreUsuario").getValue(String::class.java) ?: "Vecino",
                        estado = child.child("estado").getValue(String::class.java) ?: "",
                        latitud = child.child("latitud").getValue(Double::class.java) ?: 0.0,
                        longitud = child.child("longitud").getValue(Double::class.java) ?: 0.0,
                        fueAtendida = child.child("fueAtendida").getValue(Boolean::class.java) ?: false,
                        esFalsaAlarma = child.child("esFalsaAlarma").getValue(Boolean::class.java) ?: false,
                        creadoEn = child.child("creadoEn").getValue(Long::class.java) ?: 0L,
                        networkId = netId
                    )
                    list.add(alerta)
                }
            }
            
            // Obtener config
            val configSnap = FirebaseDatabase.getInstance().getReference("configuracion_global").child("tiempoVidaAlerta").get().await()
            val minutos = configSnap.getValue(Double::class.java) ?: 720.0
            val tiempoVidaMs = (minutos * 60 * 1000).toLong()
            val ahora = System.currentTimeMillis()
            
            list.filter { (ahora - it.creadoEn) <= tiempoVidaMs }.sortedByDescending { it.creadoEn }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting network alerts: ${e.message}", e)
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

