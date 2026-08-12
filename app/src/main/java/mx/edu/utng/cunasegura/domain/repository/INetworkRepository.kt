package mx.edu.utng.cunasegura.domain.repository

import mx.edu.utng.cunasegura.domain.model.Network
import mx.edu.utng.cunasegura.domain.model.Usuario

/**
 * Contrato de repositorio para la administración de redes vecinales y configuraciones de seguridad comunitaria.
 */
interface INetworkRepository {

    /** Crea una nueva red vecinal en Firebase Realtime Database. */
    suspend fun crearNetwork(network: Network)

    /** Obtiene la información de una red por su ID. */
    suspend fun obtenerNetworkPorId(id: String): Network?

    /** Consulta todas las redes comunitarias de modalidad abierta por GPS. */
    suspend fun obtenerRedesAbiertas(): List<Network>

    /** Une a un usuario a una red vecinal. */
    suspend fun unirseARed(usuarioId: String, networkId: String): Boolean

    /** Remueve al usuario de su red vecinal actual. */
    suspend fun salirDeRed(usuarioId: String, networkId: String): Boolean

    /** Obtiene la lista de usuarios miembros de una red. */
    suspend fun obtenerMiembrosDeRed(networkId: String): List<Usuario>

    /** Expulsa a un miembro de la red vecinal (acción exclusiva del administrador). */
    suspend fun expulsarMiembro(usuarioId: String, networkId: String): Boolean

    /** Actualiza el nombre visible de una red comunitaria. */
    suspend fun actualizarNombreRed(networkId: String, nuevoNombre: String): Boolean

    /** Consulta las alertas emitidas dentro de una red vecinal. */
    suspend fun obtenerAlertasDeRed(networkId: String): List<mx.edu.utng.cunasegura.domain.model.Alerta>

    /** Guarda las directivas y políticas globales de la red. */
    suspend fun guardarConfiguracionGlobal(tipo: String, radio: Double, tiempoAntiFalsa: Double, checkVida: Double, esperarDiasNuevos: Int, tiempoVidaAlerta: Double)

    /** Obtiene los parámetros de configuración global de la red. */
    suspend fun obtenerConfiguracionGlobal(): Map<String, Any>
}

