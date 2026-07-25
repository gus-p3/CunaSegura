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
    suspend fun expulsarMiembro(usuarioId: String, networkId: String): Boolean
    suspend fun actualizarNombreRed(networkId: String, nuevoNombre: String): Boolean
    suspend fun obtenerAlertasDeRed(networkId: String): List<mx.edu.utng.cunasegura.domain.model.Alerta>
    suspend fun guardarConfiguracionGlobal(tipo: String, radio: Double, tiempoAntiFalsa: Double, checkVida: Double, esperarDiasNuevos: Int, tiempoVidaAlerta: Double)
    suspend fun obtenerConfiguracionGlobal(): Map<String, Any>
}
