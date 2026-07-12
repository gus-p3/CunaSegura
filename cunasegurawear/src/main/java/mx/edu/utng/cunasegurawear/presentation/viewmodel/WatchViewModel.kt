package mx.edu.utng.cunasegurawear.presentation.viewmodel

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import mx.edu.utng.cunasegurawear.data.db.TouchConfig
import mx.edu.utng.cunasegurawear.data.db.TouchConfigDao
import mx.edu.utng.cunasegurawear.data.location.WatchLocationTracker
import mx.edu.utng.cunasegurawear.domain.model.AlertPhase
import mx.edu.utng.cunasegurawear.domain.model.AlertState
import mx.edu.utng.cunasegurawear.domain.model.SosAction
import mx.edu.utng.cunasegurawear.domain.repository.ConfigRepository
import mx.edu.utng.cunasegurawear.domain.usecase.CancelAlertUseCase
import mx.edu.utng.cunasegurawear.domain.usecase.GetSosActionsUseCase
import mx.edu.utng.cunasegurawear.domain.usecase.TriggerSosUseCase

class WatchViewModel(
    private val triggerSos: TriggerSosUseCase,
    private val cancelAlert: CancelAlertUseCase,
    private val getSosActions: GetSosActionsUseCase,
    private val configRepo: ConfigRepository,
    private val touchConfigDao: TouchConfigDao,
    private val locationTracker: WatchLocationTracker,
    private val context: Context
) : ViewModel() {
    private val TAG = "WatchViewModel"
    private val _state = MutableStateFlow(AlertState())
    val state: StateFlow<AlertState> = _state.asStateFlow()
    private var countdownJob: Job? = null
    private var lifeCheckJob: Job? = null
    private var locationJob: Job? = null

    init {
        // Cargar acciones iniciales desde DataStore
        viewModelScope.launch {
            val actions = getSosActions()
            _state.update { it.copy(configuredActions = actions) }
        }

        // Observar Room con self-healing: si faltan slots, los crea con valores por defecto
        viewModelScope.launch {
            touchConfigDao.getAllConfigs().collect { configs ->
                val needsSelfHealing = configs.size < 4 || configs.any { it.actionLabel.isBlank() || it.actionName.isBlank() }
                if (needsSelfHealing) {
                    viewModelScope.launch(Dispatchers.IO) {
                        touchConfigDao.insertConfigs(
                            listOf(
                                TouchConfig(1, "MENSAJE_SMS", "SMS de Ayuda"),
                                TouchConfig(2, "UBICACION_TIEMPO_REAL", "Compartir GPS"),
                                TouchConfig(3, "ALARMA_TV", "Bocina de Vecino"),
                                TouchConfig(4, "LLAMAR_911", "Llamada 911")
                            )
                        )
                    }
                } else {
                    _state.update { it.copy(touchConfigs = configs) }
                }
            }
        }

        // ── SYNC BIDIRECCIONAL: escuchar config entrante del teléfono ────────
        // Este Flow emite tanto en tiempo real (NOTIFY) como al reconectar (READ sync).
        viewModelScope.launch {
            configRepo.observeConfigFromPhone().collect { payload ->
                Log.i(TAG, "📥 [CONFIG-SYNC] Payload recibido del teléfono: $payload")
                applyIncomingConfig(payload)
            }
        }
    }

    /**
     * Parsea el payload de config recibido por BLE y actualiza Room + estado de la UI.
     * Formato esperado: "MENSAJE_SMS|UBICACION_TIEMPO_REAL|ALARMA_TV|LLAMAR_911"
     */
    private fun applyIncomingConfig(payload: String) {
        if (payload.isBlank() || payload == "NO_CONFIG") {
            Log.w(TAG, "⚠️ Config recibida vacía o sin configurar — ignorando")
            return
        }
        val parts = payload.split("|")
        if (parts.size < 4) {
            Log.w(TAG, "⚠️ Payload de config malformado (esperados 4 partes): $payload")
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val newConfigs = parts.mapIndexedNotNull { index, actionName ->
                val tapNumber = index + 1
                // Validar que el nombre corresponda a un SosAction conocido
                val action = SosAction.values().firstOrNull { it.name == actionName }
                if (action == null) {
                    Log.w(TAG, "⚠️ Acción desconocida en slot $tapNumber: '$actionName' — ignorando slot")
                    null
                } else {
                    TouchConfig(tapNumber = tapNumber, actionName = action.name, actionLabel = action.label)
                }
            }
            if (newConfigs.size == 4) {
                touchConfigDao.insertConfigs(newConfigs)
                Log.i(TAG, "✅ Room actualizado con config del teléfono: ${newConfigs.joinToString { "${it.tapNumber}→${it.actionName}" }}")
                kotlinx.coroutines.withContext(Dispatchers.Main) {
                    android.widget.Toast.makeText(context, "Sincronizado con teléfono", android.widget.Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.w(TAG, "⚠️ Config parcialmente inválida — solo ${newConfigs.size}/4 slots actualizados")
            }
        }
    }

    fun updateTouchConfig(tapNumber: Int, action: SosAction) {
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Leer el slot destino
            val targetConfig = touchConfigDao.getConfigForTaps(tapNumber)
            val oldActionName = targetConfig?.actionName ?: ""
            val oldActionLabel = targetConfig?.actionLabel ?: ""

            // 2. Detectar si la acción nueva ya existe en otro slot (swap)
            val sourceConfig = touchConfigDao.getConfigForAction(action.name)

            if (sourceConfig != null && sourceConfig.tapNumber != tapNumber) {
                touchConfigDao.insertConfig(TouchConfig(tapNumber, action.name, action.label))
                if (oldActionName.isNotEmpty()) {
                    touchConfigDao.insertConfig(TouchConfig(sourceConfig.tapNumber, oldActionName, oldActionLabel))
                }
            } else {
                touchConfigDao.insertConfig(TouchConfig(tapNumber, action.name, action.label))
            }

            // 3. Propagar el cambio al teléfono vía BLE (reloj → teléfono)
            val allConfigs = touchConfigDao.getAllConfigsNow()
            val payload = (1..4).joinToString("|") { slot ->
                allConfigs.firstOrNull { it.tapNumber == slot }?.actionName
                    ?: SosAction.values().getOrNull(slot - 1)?.name
                    ?: "MENSAJE_SMS"
            }
            Log.d(TAG, "📡 [CONFIG-UPDATE] Enviando config actualizada al teléfono: $payload")
            val result = configRepo.sendConfigToPhone(payload)
            result.onFailure { e ->
                Log.w(TAG, "⚠️ No se pudo enviar config al teléfono (¿desconectado?): ${e.message}")
                // Falla silenciosa: el teléfono la recibirá en la próxima reconexión
            }
        }
    }

    fun onSosPress() {
        countdownJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    phase = AlertPhase.COUNTDOWN,
                    countdownSeconds = 5,
                    activeActionLabel = "SOS General",
                    activeActionName = "SOS_GENERAL"
                )
            }
            for (i in 4 downTo 0) {
                delay(1000L)
                _state.update { it.copy(countdownSeconds = i) }
            }
            if (_state.value.phase == AlertPhase.COUNTDOWN) {
                // SOS manual: no viene de la BD, usa acción genérica
                val address = _state.value.gpsAddress.ifBlank { "Ubicación actual" }
                val result = triggerSos(address, "SOS_GENERAL")
                result.onSuccess { n ->
                    _state.update { it.copy(phase = AlertPhase.ACTIVE, isGpsActive = true, contactsNotified = n) }
                    startLifeCheckTimer()
                    startLocationTracking()
                }
                result.onFailure {
                    _state.update { it.copy(phase = AlertPhase.IDLE, activeActionLabel = "", activeActionName = "") }
                }
            }
        }
    }

    fun onCancelCountdown() {
        countdownJob?.cancel()
        stopLocationTracking()
        _state.update { it.copy(phase = AlertPhase.IDLE, countdownSeconds = 5, activeActionLabel = "", activeActionName = "") }
    }

    fun onSimulateTaps(taps: Int) {
        countdownJob = viewModelScope.launch {
            // PASO 1: Lee la configuración directamente del estado en memoria (ya cargado desde Room via Flow).
            // Esto garantiza que siempre se use la acción que el usuario configuró en ConfigScreen,
            // sin race conditions entre la query a BD y el arranque de la coroutine.
            val config = _state.value.touchConfigs.find { it.tapNumber == taps }

            // Fallback: si los configs aún no llegaron al estado (primer arranque muy rápido),
            // consulta la BD directamente en el hilo de IO.
            val resolvedConfig = config ?: kotlinx.coroutines.withContext(Dispatchers.IO) {
                touchConfigDao.getConfigForTaps(taps)
            }

            // Fallback final: si ni en estado ni en BD hay config, usa los valores por defecto del enum
            val actionLabel = resolvedConfig?.actionLabel ?: when (taps) {
                1 -> SosAction.MENSAJE_SMS.label
                2 -> SosAction.UBICACION_TIEMPO_REAL.label
                3 -> SosAction.ALARMA_TV.label
                4 -> SosAction.LLAMAR_911.label
                else -> "SOS General"
            }
            val actionName = resolvedConfig?.actionName ?: when (taps) {
                1 -> SosAction.MENSAJE_SMS.name
                2 -> SosAction.UBICACION_TIEMPO_REAL.name
                3 -> SosAction.ALARMA_TV.name
                4 -> SosAction.LLAMAR_911.name
                else -> "SOS_GENERAL"
            }

            android.util.Log.d("WatchViewModel", "Taps=$taps → acción=$actionName ($actionLabel) [fuente: ${if (config != null) "estado" else "BD/fallback"}]")
            kotlinx.coroutines.withContext(Dispatchers.Main) {
                android.widget.Toast.makeText(context, "Toque $taps: $actionName", android.widget.Toast.LENGTH_SHORT).show()
            }

            // PASO 2: Actualiza la pantalla con la cuenta regresiva y el nombre de la acción
            _state.update {
                it.copy(
                    phase = AlertPhase.COUNTDOWN,
                    countdownSeconds = 5,
                    activeActionLabel = actionLabel,
                    activeActionName = actionName
                )
            }
            for (i in 4 downTo 0) {
                delay(1000L)
                _state.update { it.copy(countdownSeconds = i) }
            }
            if (_state.value.phase == AlertPhase.COUNTDOWN) {
                // PASO 3: Envía la alerta por BLE con la dirección Y el código de acción configurado
                val address = _state.value.gpsAddress.ifBlank { "Ubicación actual" }
                val result = triggerSos(address, actionName)
                result.onSuccess { n ->
                    _state.update { it.copy(phase = AlertPhase.ACTIVE, isGpsActive = true, contactsNotified = n) }
                    startLifeCheckTimer()
                    startLocationTracking()
                }
                result.onFailure {
                    _state.update { it.copy(phase = AlertPhase.IDLE, activeActionLabel = "", activeActionName = "") }
                }
            }
        }
    }

    private fun startLifeCheckTimer() {
        lifeCheckJob = viewModelScope.launch {
            delay(120_000L) // 2 minutes
            if (_state.value.phase == AlertPhase.ACTIVE) {
                _state.update { it.copy(phase = AlertPhase.LIFE_CHECK) }
            }
        }
    }

    fun onLifeCheckYes() {
        lifeCheckJob?.cancel()
        stopLocationTracking()
        viewModelScope.launch {
            cancelAlert()
            _state.update { it.copy(phase = AlertPhase.CANCELLED, isGpsActive = false) }
            delay(2000L)
            _state.update { it.copy(phase = AlertPhase.IDLE, activeActionLabel = "", activeActionName = "") }
        }
    }

    fun onLifeCheckNo() {
        _state.update { it.copy(phase = AlertPhase.ACTIVE) }
        startLifeCheckTimer() // Restart 2 minutes timer
    }

    fun onSwipeBackToCountdown() {
        countdownJob?.cancel()
        stopLocationTracking()

        val actionName = _state.value.activeActionName
        val actionLabel = _state.value.activeActionLabel

        countdownJob = viewModelScope.launch {
            _state.update {
                it.copy(
                    phase = AlertPhase.COUNTDOWN,
                    countdownSeconds = 5,
                    activeActionLabel = actionLabel,
                    activeActionName = actionName
                )
            }
            for (i in 4 downTo 0) {
                delay(1000L)
                _state.update { it.copy(countdownSeconds = i) }
            }
            if (_state.value.phase == AlertPhase.COUNTDOWN) {
                val address = _state.value.gpsAddress.ifBlank { "Ubicación actual" }
                val result = triggerSos(address, actionName)
                result.onSuccess { n ->
                    _state.update { it.copy(phase = AlertPhase.ACTIVE, isGpsActive = true, contactsNotified = n) }
                    startLifeCheckTimer()
                    startLocationTracking()
                }
                result.onFailure {
                    _state.update { it.copy(phase = AlertPhase.IDLE, activeActionLabel = "", activeActionName = "") }
                }
            }
        }
    }

    private fun startLocationTracking() {
        locationTracker.startTracking()
        locationJob = viewModelScope.launch {
            locationTracker.locationFlow.collect { location ->
                location?.let { loc ->
                    _state.update { it.copy(latitude = loc.latitude, longitude = loc.longitude) }

                    // Geocoding asynchronously on IO Thread
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            @Suppress("DEPRECATION")
                            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                            val addressLine = addresses?.firstOrNull()?.getAddressLine(0)
                            val finalAddress = addressLine ?: "Lat: ${String.format(Locale.US, "%.5f", loc.latitude)}, Lon: ${String.format(Locale.US, "%.5f", loc.longitude)}"
                            
                            // Enviamos la ubicación actualizada al teléfono por BLE
                            triggerSos(finalAddress, _state.value.activeActionName)
                            
                            _state.update { it.copy(gpsAddress = finalAddress) }
                        } catch (e: Exception) {
                            val fallback = "Lat: ${String.format(Locale.US, "%.5f", loc.latitude)}, Lon: ${String.format(Locale.US, "%.5f", loc.longitude)}"
                            
                            // Enviamos el fallback de coordenadas si falla el geocoder
                            triggerSos(fallback, _state.value.activeActionName)

                            _state.update { it.copy(gpsAddress = fallback) }
                        }
                    }
                }
            }
        }
    }

    private fun stopLocationTracking() {
        locationTracker.stopTracking()
        locationJob?.cancel()
        _state.update { it.copy(gpsAddress = "", latitude = 0.0, longitude = 0.0) }
    }
}