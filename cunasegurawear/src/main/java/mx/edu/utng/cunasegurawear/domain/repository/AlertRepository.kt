package mx.edu.utng.cunasegurawear.domain.repository

import mx.edu.utng.cunasegurawear.domain.model.WatchContact

interface AlertRepository {
    suspend fun sendAlert(address: String): Result<Int>  // retorna nº contactos notificados
    suspend fun cancelAlert(): Result<Unit>
    suspend fun getContacts(): List<WatchContact>
}
