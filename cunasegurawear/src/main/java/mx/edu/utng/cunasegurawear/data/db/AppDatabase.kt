package mx.edu.utng.cunasegurawear.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TouchConfig::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun touchConfigDao(): TouchConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cuna_segura_db"
                )
                .fallbackToDestructiveMigration()
                .addCallback(AppDatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch(Dispatchers.IO) {
                    val dao = database.touchConfigDao()
                    dao.insertConfigs(
                        listOf(
                            TouchConfig(1, "MENSAJE_SMS", "SMS de Ayuda"),
                            TouchConfig(2, "UBICACION_TIEMPO_REAL", "Compartir GPS"),
                            TouchConfig(3, "ALARMA_TV", "Bocina de Vecino"),
                            TouchConfig(4, "LLAMAR_911", "Llamada 911")
                        )
                    )
                }
            }
        }
    }
}
