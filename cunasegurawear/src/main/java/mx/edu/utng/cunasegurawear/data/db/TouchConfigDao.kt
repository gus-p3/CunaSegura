package mx.edu.utng.cunasegurawear.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TouchConfigDao {
    @Query("SELECT * FROM touch_config ORDER BY tapNumber ASC")
    fun getAllConfigs(): Flow<List<TouchConfig>>

    @Query("SELECT * FROM touch_config WHERE tapNumber = :taps LIMIT 1")
    suspend fun getConfigForTaps(taps: Int): TouchConfig?

    @Query("SELECT * FROM touch_config WHERE actionName = :name LIMIT 1")
    suspend fun getConfigForAction(name: String): TouchConfig?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: TouchConfig)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigs(configs: List<TouchConfig>)

    @Query("DELETE FROM touch_config WHERE tapNumber = :taps")
    suspend fun deleteConfigForTaps(taps: Int)

    @Query("DELETE FROM touch_config")
    suspend fun deleteAllConfigs()
}
