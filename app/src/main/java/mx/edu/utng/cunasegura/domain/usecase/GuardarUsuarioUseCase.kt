package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository

/**
 * Caso de uso: guarda (o actualiza) un [Usuario] en el repositorio.
 *
 * Si el usuario ya existe con ese número de teléfono, el repositorio
 * actualiza el registro sin crear un duplicado.
 *
 * @param repository Fuente de datos abstracta (no acoplada a Room).
 */
class GuardarUsuarioUseCase(
    private val repository: IUsuarioRepository
) {
    /**
     * Ejecuta el caso de uso.
     * @param usuario El vecino a persistir.
     */
    suspend operator fun invoke(usuario: Usuario) {
        repository.guardarUsuario(usuario)
    }
}
