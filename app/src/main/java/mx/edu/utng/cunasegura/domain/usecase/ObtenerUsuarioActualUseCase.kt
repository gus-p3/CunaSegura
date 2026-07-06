package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository

/**
 * Caso de uso: obtiene el usuario de la sesión activa del dispositivo (si existe).
 * Usado por el SplashScreen para decidir si ir a Login o a Home.
 */
class ObtenerUsuarioActualUseCase(
    private val repository: IUsuarioRepository
) {
    suspend operator fun invoke(): Usuario? = repository.obtenerUsuarioActual()
}