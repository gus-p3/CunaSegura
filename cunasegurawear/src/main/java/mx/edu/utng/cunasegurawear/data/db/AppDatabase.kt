package mx.edu.utng.cunasegurawear.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(entities = [TouchConfig::class], version = 1, exportSchema = false)
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
                            TouchConfig(1, "MENSAJE_SMS", "Mensaje SMS"),
                            TouchConfig(2, "UBICACION_TIEMPO_REAL", "Ubicación T-Real"),
                            TouchConfig(3, "ALARMA_TV", "Alarma TV"),
                            TouchConfig(4, "LLAMAR_911", "Llamar al 911")
                        )
                    )
                }
            }
        }
    }
}
