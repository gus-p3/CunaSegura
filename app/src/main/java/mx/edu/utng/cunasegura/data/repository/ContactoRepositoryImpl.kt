package mx.edu.utng.cunasegura.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.domain.model.ContactoEmergencia
import mx.edu.utng.cunasegura.domain.repository.IContactoRepository
import kotlin.random.Random

/**
 * Implementación de [IContactoRepository] utilizando Firebase Realtime Database como origen de datos.
 * Esto asegura que los contactos de emergencia se guarden en la NUBE y estén asociados al usuario activo.
 */
class ContactoRepositoryImpl : IContactoRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()

    override suspend fun agregarContacto(contacto: ContactoEmergencia) {
        val firebaseUser = auth.currentUser ?: return
        val ref = db.getReference("usuarios").child(firebaseUser.uid).child("contactos")
        
        // Generamos un id numérico aleatorio único si viene en cero (como en el formulario de Compose)
        val id = if (contacto.id == 0) Random.nextInt(1, Int.MAX_VALUE) else contacto.id
        
        val map = mapOf(
            "id" to id,
            "nombre" to contacto.nombre,
            "telefono" to contacto.telefono,
            "relacion" to contacto.relacion,
            "creadoEn" to contacto.creadoEn
        )
        ref.child(id.toString()).setValue(map).await()
    }

    override suspend fun eliminarContacto(id: Int) {
        val firebaseUser = auth.currentUser ?: return
        db.getReference("usuarios")
            .child(firebaseUser.uid)
            .child("contactos")
            .child(id.toString())
            .removeValue()
            .await()
    }

    override fun obtenerContactos(usuarioId: Int): Flow<List<ContactoEmergencia>> = callbackFlow {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val ref = db.getReference("usuarios").child(firebaseUser.uid).child("contactos")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { child ->
                    val id = child.child("id").getValue(Int::class.java) ?: 0
                    val nombre = child.child("nombre").getValue(String::class.java) ?: ""
                    val telefono = child.child("telefono").getValue(String::class.java) ?: ""
                    val relacion = child.child("relacion").getValue(String::class.java) ?: ""
                    val creadoEn = child.child("creadoEn").getValue(Long::class.java) ?: System.currentTimeMillis()
                    
                    ContactoEmergencia(
                        id = id,
                        usuarioId = usuarioId,
                        nombre = nombre,
                        telefono = telefono,
                        relacion = relacion,
                        creadoEn = creadoEn
                    )
                }
                trySend(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}