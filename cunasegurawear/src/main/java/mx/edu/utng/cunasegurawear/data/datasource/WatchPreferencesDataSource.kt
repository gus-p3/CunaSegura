package mx.edu.utng.cunasegurawear.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import mx.edu.utng.cunasegurawear.domain.model.SosAction

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "watch_preferences")

class WatchPreferencesDataSource(private val context: Context) {
    private val KEY_ACTIONS = stringPreferencesKey("sos_actions")

    suspend fun getSosActions(): List<SosAction> {
        val prefs = context.dataStore.data.first()
        val actionsStr = prefs[KEY_ACTIONS] ?: return SosAction.values().toList()
        return try {
            actionsStr.split(",").map { SosAction.valueOf(it) }
        } catch (e: Exception) {
            SosAction.values().toList()
        }
    }

    suspend fun saveSosActions(actions: List<SosAction>) {
        context.dataStore.edit { prefs ->
            prefs[KEY_ACTIONS] = actions.joinToString(",") { it.name }
        }
    }
}
