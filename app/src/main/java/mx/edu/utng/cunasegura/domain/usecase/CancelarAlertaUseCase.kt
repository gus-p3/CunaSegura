package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.repository.IAlertaRepository

/**
 * Caso de uso: Cancela una alerta existente.
 */
class CancelarAlertaUseCase(
    private val repository: IAlertaRepository
) {
    /**
     * Ejecuta el caso de uso.
     * Actualiza el estado de la alerta especificada a 'cancelada'.
     */
    suspend operator fun invoke(alertaId: Int) {
        repository.cancelarAlerta(alertaId)
    }
}
