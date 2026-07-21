package mx.edu.utng.cunasegura.domain.repository

import mx.edu.utng.cunasegura.domain.model.Network
import mx.edu.utng.cunasegura.domain.model.Usuario

interface INetworkRepository {
    suspend fun crearNetwork(network: Network)
    suspend fun obtenerNetworkPorId(id: String): Network?
    suspend fun obtenerRedesAbiertas(): List<Network>
    suspend fun unirseARed(usuarioId: String, networkId: String): Boolean
    suspend fun salirDeRed(usuarioId: String, networkId: String): Boolean
    suspend fun obtenerMiembrosDeRed(networkId: String): List<Usuario>
}
