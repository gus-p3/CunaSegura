package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.model.Alerta
import mx.edu.utng.cunasegura.domain.repository.IAlertaRepository

/**
 * Caso de uso responsable de activar y despachar una nueva alerta de emergencia SOS ciudadana.
 *
 * Construye una entidad de dominio [Alerta] con estado inicial `activa` y la persiste a través de [IAlertaRepository].
 *
 * @property repository Repositorio de alertas.
 */
class ActivarAlertaUseCase(
    private val repository: IAlertaRepository
) {
    /**
     * Ejecuta la activación de la alerta SOS.
     *
     * @param usuarioId Identificador del usuario que detona la emergencia.
     * @param nombreUsuario Nombre visible del vecino.
     * @param latitud Coordenada GPS de latitud actual.
     * @param longitud Coordenada GPS de longitud actual.
     * @return El identificador numérico autogenerado para la alerta.
     */
    suspend operator fun invoke(usuarioId: Int, nombreUsuario: String, latitud: Double, longitud: Double): Long {
        val alerta = Alerta(
            usuarioId = usuarioId,
            nombreUsuario = nombreUsuario,
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

