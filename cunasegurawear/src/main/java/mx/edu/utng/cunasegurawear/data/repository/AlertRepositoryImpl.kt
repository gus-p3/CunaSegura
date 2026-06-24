package mx.edu.utng.cunasegurawear.data.repository

import mx.edu.utng.cunasegurawear.data.ble.BleWatchClient
import mx.edu.utng.cunasegurawear.domain.model.WatchContact
import mx.edu.utng.cunasegurawear.domain.repository.AlertRepository

class AlertRepositoryImpl(private val bleClient: BleWatchClient) : AlertRepository {
    override suspend fun sendAlert(address: String, actionName: String): Result<Int> =
        bleClient.sendAlert(address, actionName)
    override suspend fun cancelAlert(): Result<Unit> = bleClient.cancelAlert()
    override suspend fun getContacts(): List<WatchContact> = emptyList() // Configurable list in domain
}
