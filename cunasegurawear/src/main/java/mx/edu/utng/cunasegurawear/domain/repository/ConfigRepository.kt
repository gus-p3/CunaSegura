package mx.edu.utng.cunasegurawear.domain.repository

import mx.edu.utng.cunasegurawear.domain.model.SosAction

interface ConfigRepository {
    suspend fun getSosActions(): List<SosAction>
    suspend fun saveSosActions(actions: List<SosAction>)
}
