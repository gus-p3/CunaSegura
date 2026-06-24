package mx.edu.utng.cunasegurawear.data.ble

import java.util.UUID

object BleProtocol {
    val SERVICE_UUID: UUID     = UUID.fromString("0000cuna-0000-1000-8000-00805f9b34fb")
    val SOS_TRIGGER_UUID: UUID = UUID.fromString("0000sos0-0000-1000-8000-00805f9b34fb")
    val SOS_CANCEL_UUID: UUID  = UUID.fromString("0000canc-0000-1000-8000-00805f9b34fb")
    val CONFIG_READ_UUID: UUID = UUID.fromString("0000conf-0000-1000-8000-00805f9b34fb")
}
