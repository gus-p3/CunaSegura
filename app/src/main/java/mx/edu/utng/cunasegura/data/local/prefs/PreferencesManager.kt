package mx.edu.utng.cunasegura.data.local.prefs

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("cuna_segura_prefs", Context.MODE_PRIVATE)

    fun setWatchLinked(isLinked: Boolean) {
        prefs.edit().putBoolean("KEY_WATCH_LINKED", isLinked).apply()
    }

    fun isWatchLinked(): Boolean {
        return prefs.getBoolean("KEY_WATCH_LINKED", false)
    }
}
