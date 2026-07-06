package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.model.Alerta
import mx.edu.utng.cunasegura.domain.repository.IAlertaRepository

/**
 * Caso de uso: Activa una nueva alerta de emergencia en el sistema.
 */
class ActivarAlertaUseCase(
    private val repository: IAlertaRepository
) {
    /**
     * Ejecuta el caso de uso.
     * Crea una Alerta con estado='activa', fueAtendida=false, esFalsaAlarma=false y la guarda.
     * @return El ID generado para la alerta.
     */
    suspend operator fun invoke(usuarioId: Int, latitud: Double, longitud: Double): Long {
        val alerta = Alerta(
            usuarioId = usuarioId,
            estado = "activa",
            latitud = latitud,
            longitud = longitud,
            fueAtendida = false,
            esFalsaAlarma = false,
            creadoEn = System.currentTimeMillis()
        )
        return repository.crearAlerta(alerta)
    }
}
