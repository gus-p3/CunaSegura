package mx.edu.utng.cunasegurawear.domain

import mx.edu.utng.cunasegurawear.domain.model.WatchContact
import mx.edu.utng.cunasegurawear.domain.repository.AlertRepository

class FakeAlertRepository : AlertRepository {
    var sendResult: Result<Int> = Result.success(3)
    var cancelResult: Result<Unit> = Result.success(Unit)

    override suspend fun sendAlert(address: String): Result<Int> = sendResult
    override suspend fun cancelAlert(): Result<Unit> = cancelResult
    override suspend fun getContacts(): List<WatchContact> = listOf(WatchContact("Mamá", "461-000-0000"))
}
