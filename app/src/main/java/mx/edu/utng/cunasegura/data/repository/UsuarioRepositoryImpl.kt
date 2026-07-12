package mx.edu.utng.cunasegura.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository

/**
 * Implementación concreta de [IUsuarioRepository] que usa Firebase (Auth y Realtime Database)
 * como única fuente de verdad, eliminando la necesidad de persistir usuarios locales en SQLite/Room.
 */
class UsuarioRepositoryImpl : IUsuarioRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()

    override suspend fun guardarUsuario(usuario: Usuario) {
        val firebaseUser = auth.currentUser ?: return
        val map = mapOf(
            "nombre" to usuario.nombre,
            "telefono" to usuario.telefono,
            "correo" to usuario.correo,
            "rol" to usuario.rol,
            "estado" to usuario.estado,
            "tvVinculada" to usuario.tvVinculada
        )
        db.getReference("usuarios").child(firebaseUser.uid).updateChildren(map).await()
    }

    override suspend fun buscarPorTelefono(telefono: String): Usuario? {
        val snapshot = db.getReference("usuarios")
            .orderByChild("telefono")
            .equalTo(telefono)
            .limitToFirst(1)
            .get()
            .await()
        
        val child = snapshot.children.firstOrNull() ?: return null
        return Usuario(
            id = 0,
            nombre = child.child("nombre").getValue(String::class.java) ?: "",
            telefono = child.child("telefono").getValue(String::class.java) ?: "",
            correo = child.child("correo").getValue(String::class.java) ?: "",
            password = "",
            rol = child.child("rol").getValue(String::class.java) ?: "usuario",
            estado = child.child("estado").getValue(String::class.java) ?: "activo",
            tvVinculada = child.child("tvVinculada").getValue(Boolean::class.java) ?: false
        )
    }

    override suspend fun validarAdmin(correo: String, password: String): Usuario? {
        // Redundante con Firebase Auth
        return null
    }

    override suspend fun validarLogin(correo: String, password: String): Usuario? {
        // Redundante con Firebase Auth
        return null
    }

    override suspend fun obtenerTodosLosUsuarios(): List<Usuario> {
        val snapshot = db.getReference("usuarios").get().await()
        return snapshot.children.mapNotNull { child ->
            Usuario(
                id = 0,
                nombre = child.child("nombre").getValue(String::class.java) ?: "",
                telefono = child.child("telefono").getValue(String::class.java) ?: "",
                correo = child.child("correo").getValue(String::class.java) ?: "",
                password = "",
                rol = child.child("rol").getValue(String::class.java) ?: "usuario",
                estado = child.child("estado").getValue(String::class.java) ?: "activo",
                tvVinculada = child.child("tvVinculada").getValue(Boolean::class.java) ?: false
            )
        }
    }

    override suspend fun obtenerUsuarioActual(): Usuario? {
        val firebaseUser = auth.currentUser ?: return null
        val snapshot = db.getReference("usuarios").child(firebaseUser.uid).get().await()
        if (!snapshot.exists()) {
            val email = firebaseUser.email ?: ""
            return Usuario(
                id = 0,
                nombre = firebaseUser.displayName ?: email.substringBefore("@"),
                telefono = "",
                correo = email,
                password = "",
                rol = if (email == "admin@cunasegura.com") "admin" else "usuario",
                estado = "activo",
                tvVinculada = false
            )
        }
        return Usuario(
            id = 0,
            nombre = snapshot.child("nombre").getValue(String::class.java) ?: firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "",
            telefono = snapshot.child("telefono").getValue(String::class.java) ?: "",
            correo = snapshot.child("correo").getValue(String::class.java) ?: firebaseUser.email ?: "",
            password = "",
            rol = snapshot.child("rol").getValue(String::class.java) ?: "usuario",
            estado = snapshot.child("estado").getValue(String::class.java) ?: "activo",
            tvVinculada = snapshot.child("tvVinculada").getValue(Boolean::class.java) ?: false
        )
    }

    override suspend fun limpiarSesionLocal() {
        // No-op: La sesión se gestiona únicamente mediante Firebase Auth
    }
}
