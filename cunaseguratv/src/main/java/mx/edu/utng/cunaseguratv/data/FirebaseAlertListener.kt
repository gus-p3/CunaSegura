package mx.edu.utng.cunaseguratv.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Servicio encargado de escuchar y sincronizar en tiempo real las alertas de seguridad
 * almacenadas en Firebase Realtime Database para el módulo Smart TV.
 *
 * Transforma los eventos de [ValueEventListener] de Firebase en un [Flow] asíncrono y reactivo,
 * aplicando filtros de expiración temporal basados en `tiempoVidaAlerta`, eliminando duplicados
 * y ordenando las alertas cronológicamente.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
class FirebaseAlertListener {

    private val dbRef = FirebaseDatabase.getInstance().getReference("alertas")
    private val TAG = "FirebaseAlertListener"

    /**
     * Inicia la escucha reactiva de alertas activas desde Firebase Realtime Database.
     *
     * Realiza las siguientes operaciones:
     * 1. Consulta la configuración global (`configuracion_global/tiempoVidaAlerta`) para determinar
     *    la ventana de validez temporal de las alertas (por defecto 720 minutos / 12 horas).
     * 2. Suscribe un [ValueEventListener] en `/alertas`.
     * 3. Filtra alertas con estado `"activa"` pertenecientes a [targetNetworkId] o globales.
     * 4. Descarta alertas expiradas respecto a la marca temporal actual.
     * 5. Agrupa por vecino para conservar únicamente la alerta más reciente por usuario.
     * 6. Emite la lista resultante ordenada en forma descendente por fecha de creación.
     *
     * @param targetNetworkId Identificador opcional de la red vecinal vinculada. Si está vacío, procesa todas las alertas.
     * @return [Flow] reactivo con la lista de alertas activas [AlertaTV] actualizadas en tiempo real.
     */
    fun escucharAlertasActivas(targetNetworkId: String = ""): Flow<List<AlertaTV>> = callbackFlow {
        var tiempoVidaMs = 720L * 60 * 1000 // 720 minutos por defecto (12h)
        FirebaseDatabase.getInstance().getReference("configuracion_global").child("tiempoVidaAlerta").get().addOnSuccessListener { snap ->
            val minutos = snap.getValue(Double::class.java) ?: 720.0
            tiempoVidaMs = (minutos * 60 * 1000).toLong()
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alertas = mutableListOf<AlertaTV>()
                for (child in snapshot.children) {
                    try {
                        val estado = child.child("estado").getValue(String::class.java) ?: ""
                        val netId = child.child("networkId").getValue(String::class.java) ?: ""

                        // Solo procesar si está activa y (si hay targetNetworkId) coincide con la red de la TV
                        if (estado == "activa" && (targetNetworkId.isEmpty() || netId == targetNetworkId || netId.isEmpty())) {
                            val rawUsuarioId = child.child("usuarioId").value
                            val usuarioIdInt = when (rawUsuarioId) {
                                is Long -> rawUsuarioId.toInt()
                                is Int -> rawUsuarioId
                                is String -> rawUsuarioId.toIntOrNull() ?: 0
                                else -> 0
                            }

                            alertas.add(
                                AlertaTV(
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
                        Log.e(TAG, "Error parsing alerta en TV", e)
                    }
                }
                
                val ahora = System.currentTimeMillis()
                val activas = alertas.filter { (ahora - it.creadoEn) <= tiempoVidaMs }
                
                // Agrupar por usuario y quedarnos solo con la alerta más reciente por persona
                val activasPorUsuario = activas.groupBy { it.nombreUsuario }.map { entry ->
                    entry.value.maxByOrNull { it.creadoEn }!!
                }
                
                trySend(activasPorUsuario.sortedByDescending { it.creadoEn })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase listener cancelled", error.toException())
            }
        }

        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }
}

