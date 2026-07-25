package mx.edu.utng.cunasegurawear.domain.repository

import kotlinx.coroutines.flow.SharedFlow
import mx.edu.utng.cunasegurawear.domain.model.SosAction

interface ConfigRepository {
    /** Obtiene la lista de acciones SOS configuradas (fuente: DataStore local). */
    suspend fun getSosActions(): List<SosAction>

    /** Guarda la lista de acciones SOS localmente (DataStore). */
    suspend fun saveSosActions(actions: List<SosAction>)

    /**
     * Flow que emite cada payload de config recibido desde el teléfono vía BLE
     * (ya sea por NOTIFY en tiempo real o por READ al reconectar).
     * Formato del payload: "MENSAJE_SMS|UBICACION_TIEMPO_REAL|ALARMA_TV|LLAMAR_911"
     */
    fun observeConfigFromPhone(): SharedFlow<String>

    fun observeCheckVidaFromPhone(): SharedFlow<Long>

    /**
     * Envía la configuración actual de toques al teléfono vía BLE (camino reloj → teléfono).
     * @param configPayload Formato: "MENSAJE_SMS|UBICACION_TIEMPO_REAL|ALARMA_TV|LLAMAR_911"
     */
    suspend fun sendConfigToPhone(configPayload: String): Result<Unit>
    
    /** Pide al teléfono que envíe la configuración más reciente. */
    suspend fun requestConfigSync(): Result<Unit>
}
