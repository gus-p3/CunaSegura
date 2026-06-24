package mx.edu.utng.cunasegurawear.data.ble

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BleWatchClient(private val context: Context) {
    private val _connectionState = MutableStateFlow(BleState.DISCONNECTED)
    val connectionState: StateFlow<BleState> = _connectionState.asStateFlow()

    suspend fun sendAlert(address: String): Result<Int> {
        // Simulates sending SOS signal via BLE GATT to the companion phone app
        return Result.success(3) // Returns 3 contacts notified successfully
    }

    suspend fun cancelAlert(): Result<Unit> {
        // Simulates sending SOS Cancel signal via BLE GATT to the companion phone app
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
