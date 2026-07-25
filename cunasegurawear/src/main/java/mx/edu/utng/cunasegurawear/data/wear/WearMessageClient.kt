package mx.edu.utng.cunasegurawear.data.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

class WearMessageClient(private val context: Context) : MessageClient.OnMessageReceivedListener {

    private val TAG = "WearMessageClient"
    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)

    private val _incomingConfig = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val incomingConfig: SharedFlow<String> = _incomingConfig.asSharedFlow()

    private val _incomingCheckVida = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val incomingCheckVida: SharedFlow<Long> = _incomingCheckVida.asSharedFlow()

    init {
        messageClient.addListener(this)
        Log.d(TAG, "WearMessageClient inicializado y oyendo mensajes")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "Mensaje recibido del teléfono: ${messageEvent.path}")
        if (messageEvent.path == "/cunasegura/config/update" || messageEvent.path == "/cunasegura/config/sync") {
            val payload = String(messageEvent.data)
            _incomingConfig.tryEmit(payload)
            Log.d(TAG, "Configuración extraída: $payload")
        } else if (messageEvent.path == "/cunasegura/config/checkVida") {
            val payload = String(messageEvent.data)
            val ms = payload.toLongOrNull() ?: 120_000L
            _incomingCheckVida.tryEmit(ms)
            Log.d(TAG, "CheckVida dinámico recibido: $ms ms")
        }
    }

    suspend fun sendAlert(address: String, actionName: String): Result<Int> = withContext(Dispatchers.IO) {
        val payload = "ACTION=$actionName|ADDRESS=$address"
        Log.d(TAG, "📡 [SOS-TRIGGER] Enviando → $payload")
        
        try {
            val nodes = Tasks.await(nodeClient.connectedNodes)
            for (node in nodes) {
                messageClient.sendMessage(node.id, "/cunasegura/sos/trigger", payload.toByteArray())
            }
            if (nodes.isNotEmpty()) Result.success(3) else Result.failure(Exception("No hay teléfono conectado"))
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando alerta SOS", e)
            Result.failure(e)
        }
    }

    suspend fun cancelAlert(): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d(TAG, "📡 [SOS-CANCEL] Enviando cancelación")
        try {
            val nodes = Tasks.await(nodeClient.connectedNodes)
            for (node in nodes) {
                messageClient.sendMessage(node.id, "/cunasegura/sos/cancel", "CANCEL".toByteArray())
            }
            if (nodes.isNotEmpty()) Result.success(Unit) else Result.failure(Exception("No hay teléfono conectado"))
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando cancelación", e)
            Result.failure(e)
        }
    }

    suspend fun sendConfigUpdate(configPayload: String): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d(TAG, "📡 [CONFIG-UPDATE] Reloj → Teléfono: $configPayload")
        try {
            val nodes = Tasks.await(nodeClient.connectedNodes)
            for (node in nodes) {
                messageClient.sendMessage(node.id, "/cunasegura/config/update", configPayload.toByteArray())
            }
            if (nodes.isNotEmpty()) Result.success(Unit) else Result.failure(Exception("No hay teléfono conectado"))
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando config", e)
            Result.failure(e)
        }
    }

    suspend fun sendSyncRequest(): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d(TAG, "📡 [CONFIG-SYNC] Solicitando config al Teléfono...")
        try {
            val nodes = Tasks.await(nodeClient.connectedNodes)
            for (node in nodes) {
                messageClient.sendMessage(node.id, "/cunasegura/config/sync_request", "SYNC".toByteArray())
            }
            if (nodes.isNotEmpty()) Result.success(Unit) else Result.failure(Exception("No hay teléfono conectado"))
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando sync_request", e)
            Result.failure(e)
        }
    }
}
