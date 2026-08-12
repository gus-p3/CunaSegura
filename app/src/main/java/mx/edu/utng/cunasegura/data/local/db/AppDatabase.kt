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
 * Base de datos principal de la aplicación móvil Cuna Segura implementada con Room SQLite.
 *
 * Entidades gestionadas:
 * - [UsuarioEntity]: Información de perfil, credenciales y geolocalización.
 * - [AlertaEntity]: Registro y estados de alertas de pánico ciudadanas.
 * - [ContactoEmergenciaEntity]: Directorio de contactos de auxilio.
 * - [ConfiguracionToqueEntity]: Configuración de acciones por gestos/toques de smartwatch.
 *
 * Implementa el patrón Singleton thread-safe mediante doble comprobación sincronizada y
 * realiza el sembrado inicial (seed) de la cuenta de administrador global mediante [AdminSeedCallback].
 */
@Database(
    entities = [
        UsuarioEntity::class,
        AlertaEntity::class,
        ContactoEmergenciaEntity::class,
        ConfiguracionToqueEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    /** Provee el DAO para operaciones sobre usuarios. */
    abstract fun usuarioDao(): UsuarioDao

    /** Provee el DAO para operaciones sobre alertas ciudadanas. */
    abstract fun alertaDao(): AlertaDao

    /** Provee el DAO para operaciones sobre contactos de emergencia. */
    abstract fun contactoDao(): ContactoDao

    /** Provee el DAO para operaciones sobre configuraciones de toques. */
    abstract fun configuracionToqueDao(): ConfiguracionToqueDao

    companion object {
        private const val DATABASE_NAME = "cuna_segura.db"

        // Credenciales por defecto del administrador global para entorno de pruebas y auditoría
        const val ADMIN_CORREO = "brandon@gmail.com"
        const val ADMIN_PASSWORD = "123456789"
        const val ADMIN_NOMBRE = "Brandon Admin"
        const val ADMIN_TELEFONO = "0000000000"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Retorna la instancia única (Singleton) de [AppDatabase].
         *
         * @param context Contexto de la aplicación Android.
         * @return Instancia única de la base de datos Room.
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
     * Callback de inicialización que inserta automáticamente el usuario administrador
     * en el hilo de E/S ([Dispatchers.IO]) la primera vez que se crea la base de datos.
     */
    private class AdminSeedCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
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

