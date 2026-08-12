package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository

/**
 * Caso de uso responsable de purgar la sesión y registros de usuario local en SQLite Room
 * garantizando un estado limpio al cerrar sesión o cambiar de cuenta.
 *
 * @property repository Repositorio de usuarios.
 */
class LimpiarSesionLocalUseCase(
    private val repository: IUsuarioRepository
) {
    /**
     * Ejecuta la limpieza de sesión local.
     */
    suspend operator fun invoke() {
        repository.limpiarSesionLocal()
    }
}

