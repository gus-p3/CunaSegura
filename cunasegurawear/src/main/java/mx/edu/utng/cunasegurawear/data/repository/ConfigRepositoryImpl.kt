package mx.edu.utng.cunasegurawear.data.repository

import mx.edu.utng.cunasegurawear.data.datasource.WatchPreferencesDataSource
import mx.edu.utng.cunasegurawear.domain.model.SosAction
import mx.edu.utng.cunasegurawear.domain.repository.ConfigRepository

class ConfigRepositoryImpl(private val ds: WatchPreferencesDataSource) : ConfigRepository {
    override suspend fun getSosActions(): List<SosAction> = ds.getSosActions()
    override suspend fun saveSosActions(actions: List<SosAction>) = ds.saveSosActions(actions)
}
