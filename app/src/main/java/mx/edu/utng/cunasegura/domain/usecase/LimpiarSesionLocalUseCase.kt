package mx.edu.utng.cunasegura.domain.usecase

import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository

class LimpiarSesionLocalUseCase(
    private val repository: IUsuarioRepository
) {
    suspend operator fun invoke() {
        repository.limpiarSesionLocal()
    }
}
