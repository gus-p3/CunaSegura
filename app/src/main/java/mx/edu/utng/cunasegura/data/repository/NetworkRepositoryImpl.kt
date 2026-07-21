package mx.edu.utng.cunasegura.data.repository

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.domain.model.Network
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.INetworkRepository

class NetworkRepositoryImpl : INetworkRepository {
    private val db = FirebaseDatabase.getInstance()

    override suspend fun crearNetwork(network: Network) {
        db.getReference("networks").child(network.id).setValue(network).await()
    }

    override suspend fun obtenerNetworkPorId(id: String): Network? {
        val snapshot = db.getReference("networks").child(id).get().await()
        if (!snapshot.exists()) return null
        return snapshot.getValue(Network::class.java)
    }

    override suspend fun obtenerRedesAbiertas(): List<Network> {
        val snapshot = db.getReference("networks")
            .orderByChild("tipo")
            .equalTo("Abierta")
            .get()
            .await()
        return snapshot.children.mapNotNull { it.getValue(Network::class.java) }
    }

    override suspend fun unirseARed(usuarioId: String, networkId: String): Boolean {
        return try {
            // 1. Agregar el usuario a la lista de miembros de la red
            db.getReference("networks")
                .child(networkId)
                .child("miembros")
                .child(usuarioId)
                .setValue(true)
                .await()

            // 2. Actualizar el networkId en el perfil del usuario
            db.getReference("usuarios")
                .child(usuarioId)
                .child("networkId")
                .setValue(networkId)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

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
                    tvVinculada = userSnap.child("tvVinculada").getValue(Boolean::class.java) ?: false
                )
                list.add(user)
            }
        }
        return list
    }
}
