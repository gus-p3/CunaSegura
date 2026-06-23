package mx.edu.utng.cunasegurawear.domain.usecase

import mx.edu.utng.cunasegurawear.domain.repository.AlertRepository

class TriggerSosUseCase(private val repo: AlertRepository) {
    suspend operator fun invoke(address: String): Result<Int> = repo.sendAlert(address)
}
