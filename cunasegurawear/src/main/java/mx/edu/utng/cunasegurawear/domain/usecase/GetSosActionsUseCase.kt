package mx.edu.utng.cunasegurawear.domain.usecase

import mx.edu.utng.cunasegurawear.domain.model.SosAction
import mx.edu.utng.cunasegurawear.domain.repository.ConfigRepository

class GetSosActionsUseCase(private val repo: ConfigRepository) {
    suspend operator fun invoke(): List<SosAction> = repo.getSosActions()
}
