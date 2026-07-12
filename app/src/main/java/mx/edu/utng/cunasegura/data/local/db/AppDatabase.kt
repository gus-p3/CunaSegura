package mx.edu.utng.cunasegura.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.edu.utng.cunasegura.data.local.dao.AlertaDao
import mx.edu.utng.cunasegura.data.local.dao.ConfiguracionToqueDao
import mx.edu.utng.cunasegura.data.local.dao.ContactoDao
import mx.edu.utng.cunasegura.data.local.dao.UsuarioDao
import mx.edu.utng.cunasegura.data.local.entity.AlertaEntity
import mx.edu.utng.cunasegura.data.local.entity.ConfiguracionToqueEntity
import mx.edu.utng.cunasegura.data.local.entity.ContactoEmergenciaEntity
import mx.edu.utng.cunasegura.data.local.entity.UsuarioEntity

/**
 * Base de datos local de CunaSegura usando Room.
 *
 * Versión 3: cambia tipoAccion de texto libre a nombres del enum (MENSAJE_SMS, etc.).
 * Nota (Sprint 1): Se usa fallbackToDestructiveMigration(), lo cual borrará
 * los datos existentes (esto es aceptable por ahora ya que no hay usuarios en prod).
 * Singleton: obtener la instancia a través de [AppDatabase.getInstance].
 */
@Database(
    entities = [
        UsuarioEntity::class,
        AlertaEntity::class,
        ContactoEmergenciaEntity::class,
        ConfiguracionToqueEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun alertaDao(): AlertaDao
    abstract fun contactoDao(): ContactoDao
    abstract fun configuracionToqueDao(): ConfiguracionToqueDao

    companion object {
        private const val DATABASE_NAME = "cuna_segura.db"

        // Credenciales del administrador global
        const val ADMIN_CORREO = "brandon@gmail.com"
        const val ADMIN_PASSWORD = "123456789"
        const val ADMIN_NOMBRE = "Brandon Admin"
        const val ADMIN_TELEFONO = "0000000000"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Retorna la instancia singleton de [AppDatabase].
         * Hilo-seguro gracias a doble comprobación con bloque `synchronized`.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .addCallback(AdminSeedCallback())
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

    /**
     * Callback que inserta el usuario administrador la primera vez que
     * se crea la base de datos (instalación limpia).
     */
    private class AdminSeedCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            // Insertar admin en un hilo de IO para no bloquear el main thread
            CoroutineScope(Dispatchers.IO).launch {
                INSTANCE?.usuarioDao()?.insertarUsuario(
                    UsuarioEntity(
                        nombre = ADMIN_NOMBRE,
                        telefono = ADMIN_TELEFONO,
                        correo = ADMIN_CORREO,
                        password = ADMIN_PASSWORD,
                        rol = "admin",
                        estado = "activo"
                    )
                )
            }
        }
    }
}
