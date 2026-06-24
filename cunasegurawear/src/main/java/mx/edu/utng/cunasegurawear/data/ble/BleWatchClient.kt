package mx.edu.utng.cunasegurawear.data.ble

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BleWatchClient(private val context: Context) {
    private val _connectionState = MutableStateFlow(BleState.DISCONNECTED)
    val connectionState: StateFlow<BleState> = _connectionState.asStateFlow()

    suspend fun sendAlert(address: String, actionName: String): Result<Int> {
        // Empaqueta la acción y la dirección en un payload BLE para enviar al celular.
        // El celular recibe este paquete y ejecuta la acción correspondiente.
        //
        // Formato del payload enviado por GATT:
        //   "ACTION=ALARMA_TV|ADDRESS=Calle Morelos #48"
        //
        // El celular parsea el ACTION y ejecuta:
        //   MENSAJE_SMS           → Envía SMS de auxilio a los contactos
        //   UBICACION_TIEMPO_REAL → Comparte link de mapa en tiempo real
        //   ALARMA_TV             → Enciende la sirena de vecino
        //   LLAMAR_911            → Marca al número de emergencias
        val blePayload = "ACTION=$actionName|ADDRESS=$address"
        android.util.Log.d("BleWatchClient", "📡 Enviando SOS por BLE → $blePayload")

        // TODO: Aquí tu compañera conecta el GATT real y escribe blePayload
        // en la característica UUID del servicio SOS del celular:
        //
        //   gatt.writeCharacteristic(sosCharacteristic, blePayload.toByteArray())
        //
        return Result.success(3) // Simula que 3 contactos fueron notificados
    }

    suspend fun cancelAlert(): Result<Unit> {
        android.util.Log.d("BleWatchClient", "📡 Enviando CANCELAR SOS por BLE")
        return Result.success(Unit)
    }

    fun connect(deviceAddress: String) {
        _connectionState.value = BleState.CONNECTED
    }

    fun disconnect() {
        _connectionState.value = BleState.DISCONNECTED
    }
}

enum class BleState { DISCONNECTED, CONNECTING, CONNECTED }
