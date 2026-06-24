package mx.edu.utng.cunasegurawear.data.ble

import java.util.UUID

object BleProtocol {
    val SERVICE_UUID: UUID     = UUID.fromString("0000c11a-0000-1000-8000-00805f9b34fb")
    val SOS_TRIGGER_UUID: UUID = UUID.fromString("00005050-0000-1000-8000-00805f9b34fb")
    val SOS_CANCEL_UUID: UUID  = UUID.fromString("0000ca1c-0000-1000-8000-00805f9b34fb")
    val CONFIG_READ_UUID: UUID = UUID.fromString("0000c01f-0000-1000-8000-00805f9b34fb")
}
