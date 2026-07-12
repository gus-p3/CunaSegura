package mx.edu.utng.cunasegurawear.data.repository

import kotlinx.coroutines.flow.SharedFlow
import mx.edu.utng.cunasegurawear.data.wear.WearMessageClient
import mx.edu.utng.cunasegurawear.data.datasource.WatchPreferencesDataSource
import mx.edu.utng.cunasegurawear.domain.model.SosAction
import mx.edu.utng.cunasegurawear.domain.repository.ConfigRepository

class ConfigRepositoryImpl(
    private val ds: WatchPreferencesDataSource,
    private val wearClient: WearMessageClient
) : ConfigRepository {

    override suspend fun getSosActions(): List<SosAction> = ds.getSosActions()

    override suspend fun saveSosActions(actions: List<SosAction>) = ds.saveSosActions(actions)

    override fun observeConfigFromPhone(): SharedFlow<String> = wearClient.incomingConfig

    override suspend fun sendConfigToPhone(configPayload: String): Result<Unit> =
        wearClient.sendConfigUpdate(configPayload)
}
