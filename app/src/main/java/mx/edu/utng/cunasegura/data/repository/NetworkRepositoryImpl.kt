package mx.edu.utng.cunasegura.data.repository

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.domain.model.Network
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.INetworkRepository

/**
 * Implementación de [INetworkRepository] conectada con Firebase Realtime Database.
 *
 * Administra los nodos `/networks/{networkId}`, `/configuracion_global` y sincroniza las membresías de los usuarios.
 */
class NetworkRepositoryImpl : INetworkRepository {
    private val db = FirebaseDatabase.getInstance()

    /**
     * Guarda una nueva red vecinal comunitaria en Firebase.
     *
     * @param network Modelo de la red.
     */
    override suspend fun crearNetwork(network: Network) {
        db.getReference("networks").child(network.id).setValue(network).await()
    }

    /**
     * Consulta una red vecinal por su identificador único.
     *
     * @param id Identificador de la red.
     * @return [Network] o `null` si no existe.
     */
    override suspend fun obtenerNetworkPorId(id: String): Network? {
        val snapshot = db.getReference("networks").child(id).get().await()
        if (!snapshot.exists()) return null
        return snapshot.getValue(Network::class.java)
    }

    /**
     * Obtiene el listado de redes comunitarias públicas/abiertas basadas en geolocalización.
     *
     * @return Lista de redes de tipo `Abierta`.
     */
    override suspend fun obtenerRedesAbiertas(): List<Network> {
        val snapshot = db.getReference("networks")
            .orderByChild("tipo")
            .equalTo("Abierta")
            .get()
            .await()
        return snapshot.children.mapNotNull { it.getValue(Network::class.java) }
    }

    /**
     * Vincula a un usuario como miembro de una red vecinal.
     *
     * @param usuarioId UID del usuario en Firebase.
     * @param networkId ID de la red vecinal.
     * @return `true` si la operación se completó exitosamente.
     */
    override suspend fun unirseARed(usuarioId: String, networkId: String): Boolean {
        return try {
            // 1. Agregar el usuario a la lista de miembros de la red
            db.getReference("networks")
                .child(networkId)
                .child("miembros")
                .child(usuarioId)
                .setValue(true)
                .await()

            // 2. Actualizar el networkId y fechaIngreso en el perfil del usuario
            val updates = mapOf(
                "networkId" to networkId,
                "fechaIngreso" to System.currentTimeMillis()
            )
            db.getReference("usuarios")
                .child(usuarioId)
                .updateChildren(updates)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Desvincula a un usuario de su red vecinal actual.
     *
     * @param usuarioId UID del usuario.
     * @param networkId ID de la red actual.
     * @return `true` si se desvinculó con éxito.
     */
    override suspend fun salirDeRed(usuarioId: String, networkId: String): Boolean {
        return try {
            // 1. Remover de miembros de la red
            db.getReference("networks")
                .child(networkId)
                .child("miembros")
                .child(usuarioId)
                .removeValue()
                .await()

            // 2. Restablecer el networkId del usuario a su propio UID (por defecto)
            db.getReference("usuarios")
                .child(usuarioId)
                .child("networkId")
                .setValue(usuarioId)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene el listado completo de vecinos que integran una red vecinal específica.
     *
     * @param networkId ID de la red comunitaria.
     * @return Lista de [Usuario] que pertenecen a la red.
     */
    override suspend fun obtenerMiembrosDeRed(networkId: String): List<Usuario> {
        val network = obtenerNetworkPorId(networkId) ?: return emptyList()
        val uids = network.miembros.keys
        val list = mutableListOf<Usuario>()
        for (uid in uids) {
            val userSnap = db.getReference("usuarios").child(uid).get().await()
            if (userSnap.exists()) {
                val user = Usuario(
                    id = 0,
                    nombre = userSnap.child("nombre").getValue(String::class.java) ?: "",
                    telefono = userSnap.child("telefono").getValue(String::class.java) ?: "",
                    correo = userSnap.child("correo").getValue(String::class.java) ?: "",
                    password = "",
                    rol = userSnap.child("rol").getValue(String::class.java) ?: "usuario",
                    estado = userSnap.child("estado").getValue(String::class.java) ?: "activo",
                    tvVinculada = userSnap.child("tvVinculada").getValue(Boolean::class.java) ?: false,
                    networkId = userSnap.child("networkId").getValue(String::class.java) ?: "",
                    fechaIngreso = userSnap.child("fechaIngreso").getValue(Long::class.java) ?: 0L,
                    uid = uid
                )
                list.add(user)
            }
        }
        return list
    }

    /**
     * Expulsa a un miembro de la red vecinal y reinicia sus credenciales comunitarias.
     *
     * @param usuarioId UID del miembro expulsado.
     * @param networkId ID de la red.
     * @return `true` si la expulsión se realizó correctamente.
     */
    override suspend fun expulsarMiembro(usuarioId: String, networkId: String): Boolean {
        return try {
            db.getReference("networks")
                .child(networkId)
                .child("miembros")
                .child(usuarioId)
                .removeValue()
                .await()

            val updates = mapOf<String, Any>(
                "networkId" to usuarioId,
                "rolEnRed" to ""
            )
            db.getReference("usuarios")
                .child(usuarioId)
                .updateChildren(updates)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Renombra una red comunitaria en Firebase.
     *
     * @param networkId ID de la red.
     * @param nuevoNombre Nuevo nombre asignado.
     * @return `true` en caso de éxito.
     */
    override suspend fun actualizarNombreRed(networkId: String, nuevoNombre: String): Boolean {
        return try {
            db.getReference("networks").child(networkId).child("nombre").setValue(nuevoNombre).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Consulta las alertas asociadas a una red comunitaria.
     *
     * @param networkId ID de la red.
     * @return Lista de alertas emitidas en la red.
     */
    override suspend fun obtenerAlertasDeRed(networkId: String): List<mx.edu.utng.cunasegura.domain.model.Alerta> {
        return try {
            val snapshot = db.getReference("alertas").get().await()
            val list = mutableListOf<mx.edu.utng.cunasegura.domain.model.Alerta>()
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
                    val alerta = mx.edu.utng.cunasegura.domain.model.Alerta(
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
            list.sortedByDescending { it.creadoEn }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Persiste los parámetros globales de la red vecinal en `/configuracion_global`.
     *
     * @param tipo Tipo de red (`Abierta` o `Cerrada`).
     * @param radio Radio en metros.
     * @param tiempoAntiFalsa Segundos de gracia antes de disparo definitivo.
     * @param checkVida Minutos de intervalo de verificación.
     * @param esperarDiasNuevos Días de restricción para nuevos ingresos.
     * @param tiempoVidaAlerta Minutos de vigencia de una alerta activa.
     */
    override suspend fun guardarConfiguracionGlobal(
        tipo: String,
        radio: Double,
        tiempoAntiFalsa: Double,
        checkVida: Double,
        esperarDiasNuevos: Int,
        tiempoVidaAlerta: Double
    ) {
        val map = mapOf(
            "tipo" to tipo,
            "radio" to radio,
            "tiempoAntiFalsa" to tiempoAntiFalsa,
            "checkVida" to checkVida,
            "esperarDiasNuevos" to esperarDiasNuevos,
            "tiempoVidaAlerta" to tiempoVidaAlerta
        )
        db.getReference("configuracion_global").setValue(map).await()
    }

    /**
     * Obtiene el mapa de configuraciones globales vigentes en Firebase Realtime Database.
     *
     * @return Mapa de clave y valor de configuración global.
     */
    override suspend fun obtenerConfiguracionGlobal(): Map<String, Any> {
        return try {
            val snapshot = db.getReference("configuracion_global").get().await()
            if (snapshot.exists()) {
                val map = mutableMapOf<String, Any>()
                snapshot.children.forEach { child ->
                    val k = child.key
                    val v = child.value
                    if (k != null && v != null) map[k] = v
                }
                map
            } else emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}

