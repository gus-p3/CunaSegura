package mx.edu.utng.cunaseguratv.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class FirebaseAlertListener {

    private val dbRef = FirebaseDatabase.getInstance().getReference("alertas")
    private val TAG = "FirebaseAlertListener"

    fun escucharAlertasActivas(targetNetworkId: String = ""): Flow<List<AlertaTV>> = callbackFlow {
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
                trySend(alertas.sortedByDescending { it.creadoEn })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase listener cancelled", error.toException())
            }
        }

        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }
}
