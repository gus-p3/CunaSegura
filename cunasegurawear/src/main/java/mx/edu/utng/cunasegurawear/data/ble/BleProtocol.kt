package mx.edu.utng.cunasegurawear.data.ble

import java.util.UUID

object BleProtocol {
    /** UUID del servicio GATT principal de CunaSegura. */
    val SERVICE_UUID: UUID = UUID.fromString("0000c11a-0000-1000-8000-00805f9b34fb")

    /** Reloj → Teléfono: el reloj escribe el evento SOS (ACTION + ADDRESS). */
    val SOS_TRIGGER_UUID: UUID = UUID.fromString("00005050-0000-1000-8000-00805f9b34fb")

    /** Reloj → Teléfono: el reloj indica que el usuario canceló la alerta. */
    val SOS_CANCEL_UUID: UUID = UUID.fromString("0000ca1c-0000-1000-8000-00805f9b34fb")

    /**
     * Teléfono → Reloj: el teléfono expone la config actual (PROPERTY_READ | PROPERTY_NOTIFY).
     * - READ: el reloj la consulta al reconectar para sincronizar.
     * - NOTIFY: el teléfono empuja cambios en tiempo real cuando el usuario guarda config.
     * Formato del payload: "ACCION_TOQUE_1|ACCION_TOQUE_2|ACCION_TOQUE_3|ACCION_TOQUE_4"
     * donde cada parte es el nombre del enum SosAction (ej. "MENSAJE_SMS|UBICACION_TIEMPO_REAL|ALARMA_TV|LLAMAR_911").
     */
    val CONFIG_READ_UUID: UUID = UUID.fromString("0000c01f-0000-1000-8000-00805f9b34fb")

    /**
     * Reloj → Teléfono: el reloj escribe cambios de configuración al teléfono (PROPERTY_WRITE).
     * Mismo formato de payload que CONFIG_READ_UUID.
     * LIMITACIÓN CONOCIDA (sprint 1): si reloj y teléfono cambian config casi simultáneamente,
     * gana el último write que llegue al receptor — no hay resolución de conflictos.
     */
    val CONFIG_UPDATE_UUID: UUID = UUID.fromString("0000c0ff-0000-1000-8000-00805f9b34fb")
}
