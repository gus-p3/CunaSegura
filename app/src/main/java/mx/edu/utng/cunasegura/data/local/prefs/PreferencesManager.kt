package mx.edu.utng.cunasegura.data.local.prefs

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestor de almacenamiento local ligero basado en [SharedPreferences].
 *
 * Utilizado para persistir banderas de estado y configuraciones rápidas, tales como
 * el estado de vinculación simulada o real de dispositivos wearable (SmartWatch).
 *
 * @param context Contexto de la aplicación Android.
 */
class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("cuna_segura_prefs", Context.MODE_PRIVATE)

    /**
     * Guarda el estado de vinculación del SmartWatch.
     *
     * @param isLinked `true` si el reloj está vinculado, `false` en caso contrario.
     */
    fun setWatchLinked(isLinked: Boolean) {
        prefs.edit().putBoolean("KEY_WATCH_LINKED", isLinked).apply()
    }

    /**
     * Consulta si existe un SmartWatch actualmente vinculado.
     *
     * @return `true` si está vinculado, `false` por defecto.
     */
    fun isWatchLinked(): Boolean {
        return prefs.getBoolean("KEY_WATCH_LINKED", false)
    }
}

