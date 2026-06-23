package mx.edu.utng.cunasegurawear.domain.model

enum class SosAction(val label: String, val tapNumber: Int) {
    MENSAJE_SMS("Mensaje SMS", 1),       // 1 toque → envía SMS predefinido
    UBICACION_TIEMPO_REAL("Ubicación T-Real", 2),  // 2 toques → GPS activo
    ALARMA_TV("Alarma TV", 3),           // 3 toques → activa TV del vecino
    LLAMAR_911("Llamar al 911", 4)       // 4 toques → llama a emergencias
}
