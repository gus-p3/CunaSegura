package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository

/**
 * Caso de uso responsable de recuperar el usuario de la sesión activa en el dispositivo.
 *
 * Utilizado por el flujo de inicio (SplashScreen / MainActivity) para determinar la ruta inicial.
 *
 * @property repository Repositorio de usuarios.
 */
class ObtenerUsuarioActualUseCase(
    private val repository: IUsuarioRepository
) {
    /**
     * Retorna el [Usuario] activo actualmente o `null` si no hay sesión iniciada.
     */
    suspend operator fun invoke(): Usuario? = repository.obtenerUsuarioActual()
}