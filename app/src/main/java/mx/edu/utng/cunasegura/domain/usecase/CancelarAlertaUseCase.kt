package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.repository.IAlertaRepository

/**
 * Caso de uso responsable de desactivar o cancelar una alerta SOS activa.
 *
 * Actualiza el estado de la alerta en el repositorio local y en la red remota.
 *
 * @property repository Repositorio de alertas.
 */
class CancelarAlertaUseCase(
    private val repository: IAlertaRepository
) {
    /**
     * Ejecuta la cancelación de la alerta.
     *
     * @param alertaId Identificador numérico de la alerta a cancelar.
     */
    suspend operator fun invoke(alertaId: Int) {
        repository.cancelarAlerta(alertaId)
    }
}

