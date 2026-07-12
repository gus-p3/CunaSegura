package mx.edu.utng.cunasegurawear.data.repository

import mx.edu.utng.cunasegurawear.data.wear.WearMessageClient
import mx.edu.utng.cunasegurawear.domain.model.WatchContact
import mx.edu.utng.cunasegurawear.domain.repository.AlertRepository

class AlertRepositoryImpl(private val wearClient: WearMessageClient) : AlertRepository {
    override suspend fun sendAlert(address: String, actionName: String): Result<Int> =
        wearClient.sendAlert(address, actionName)
    override suspend fun cancelAlert(): Result<Unit> = wearClient.cancelAlert()
    override suspend fun getContacts(): List<WatchContact> = emptyList() // Configurable list in domain
}
