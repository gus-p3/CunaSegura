package mx.edu.utng.cunasegura.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
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
 * Singleton: obtener la instancia a través de [AppDatabase.getInstance].
 * Incrementar [version] al realizar migraciones de esquema.
 */
@Database(
    entities = [
        UsuarioEntity::class,
        AlertaEntity::class,
        ContactoEmergenciaEntity::class,
        ConfiguracionToqueEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun alertaDao(): AlertaDao
    abstract fun contactoDao(): ContactoDao
    abstract fun configuracionToqueDao(): ConfiguracionToqueDao

    companion object {
        private const val DATABASE_NAME = "cuna_segura.db"

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
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
