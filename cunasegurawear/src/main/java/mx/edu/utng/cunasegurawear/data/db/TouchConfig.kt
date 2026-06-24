package mx.edu.utng.cunasegurawear.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "touch_config")
data class TouchConfig(
    @PrimaryKey val tapNumber: Int,
    val actionName: String,
    val actionLabel: String
)
