package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository

/**
 * Caso de uso responsable de persistir o actualizar el perfil de un usuario/vecino en el repositorio.
 *
 * Si el usuario ya existe con ese número de teléfono, actualiza su registro sin crear un duplicado.
 *
 * @property repository Fuente de datos abstracta de usuarios.
 */
class GuardarUsuarioUseCase(
    private val repository: IUsuarioRepository
) {
    /**
     * Ejecuta el guardado del usuario en la capa de datos.
     *
     * @param usuario Modelo de dominio del usuario a persistir.
     */
    suspend operator fun invoke(usuario: Usuario) {
        repository.guardarUsuario(usuario)
    }
}

