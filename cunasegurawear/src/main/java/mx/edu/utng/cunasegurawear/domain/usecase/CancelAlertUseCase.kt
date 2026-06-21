package mx.edu.utng.cunasegurawear.domain.usecase

import mx.edu.utng.cunasegurawear.domain.repository.AlertRepository

class CancelAlertUseCase(private val repo: AlertRepository) {
    suspend operator fun invoke(): Result<Unit> = repo.cancelAlert()
}
