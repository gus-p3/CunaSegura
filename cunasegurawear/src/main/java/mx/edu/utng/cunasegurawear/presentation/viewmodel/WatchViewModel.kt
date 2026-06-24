package mx.edu.utng.cunasegurawear.presentation.viewmodel

import android.content.Context
import android.location.Geocoder
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
import mx.edu.utng.cunasegurawear.data.db.TouchConfigDao
import mx.edu.utng.cunasegurawear.data.location.WatchLocationTracker
import mx.edu.utng.cunasegurawear.domain.model.AlertPhase
import mx.edu.utng.cunasegurawear.domain.model.AlertState
import mx.edu.utng.cunasegurawear.domain.usecase.CancelAlertUseCase
import mx.edu.utng.cunasegurawear.domain.usecase.GetSosActionsUseCase
import mx.edu.utng.cunasegurawear.domain.usecase.TriggerSosUseCase

class WatchViewModel(
    private val triggerSos: TriggerSosUseCase,
    private val cancelAlert: CancelAlertUseCase,
    private val getSosActions: GetSosActionsUseCase,
    private val touchConfigDao: TouchConfigDao,
    private val locationTracker: WatchLocationTracker,
    private val context: Context
) : ViewModel() {
    private val _state = MutableStateFlow(AlertState())
    val state: StateFlow<AlertState> = _state.asStateFlow()
    private var countdownJob: Job? = null
    private var lifeCheckJob: Job? = null
    private var locationJob: Job? = null

    init {
        // Load initial actions from DataStore
        viewModelScope.launch {
            val actions = getSosActions()
            _state.update { it.copy(configuredActions = actions) }
        }
    }

    fun onSosPress() {
        countdownJob = viewModelScope.launch {
            _state.update { it.copy(phase = AlertPhase.COUNTDOWN, countdownSeconds = 5) }
            for (i in 4 downTo 0) {
                delay(1000L)
                _state.update { it.copy(countdownSeconds = i) }
            }
            if (_state.value.phase == AlertPhase.COUNTDOWN) {
                val result = triggerSos("Calle Morelos #48")
                result.onSuccess { n ->
                    _state.update { it.copy(phase = AlertPhase.ACTIVE, isGpsActive = true, contactsNotified = n) }
                    startLifeCheckTimer()
                    startLocationTracking()
                }
                result.onFailure {
                    _state.update { it.copy(phase = AlertPhase.IDLE) }
                }
            }
        }
    }

    fun onCancelCountdown() {
        countdownJob?.cancel()
        stopLocationTracking()
        _state.update { it.copy(phase = AlertPhase.IDLE, countdownSeconds = 5, activeActionLabel = "") }
    }

    fun onSimulateTaps(taps: Int) {
        countdownJob = viewModelScope.launch {
            val config = touchConfigDao.getConfigForTaps(taps)
            val actionLabel = config?.actionLabel ?: "SOS General"
            _state.update {
                it.copy(
                    phase = AlertPhase.COUNTDOWN,
                    countdownSeconds = 5,
                    activeActionLabel = actionLabel
                )
            }
            for (i in 4 downTo 0) {
                delay(1000L)
                _state.update { it.copy(countdownSeconds = i) }
            }
            if (_state.value.phase == AlertPhase.COUNTDOWN) {
                val result = triggerSos("Calle Morelos #48")
                result.onSuccess { n ->
                    _state.update { it.copy(phase = AlertPhase.ACTIVE, isGpsActive = true, contactsNotified = n) }
                    startLifeCheckTimer()
                    startLocationTracking()
                }
                result.onFailure {
                    _state.update { it.copy(phase = AlertPhase.IDLE) }
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
            _state.update { it.copy(phase = AlertPhase.IDLE) }
        }
    }

    fun onLifeCheckNo() {
        _state.update { it.copy(phase = AlertPhase.ACTIVE) }
        startLifeCheckTimer() // Restart 2 minutes timer
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
                            _state.update { it.copy(gpsAddress = finalAddress) }
                        } catch (e: Exception) {
                            val fallback = "Lat: ${String.format(Locale.US, "%.5f", loc.latitude)}, Lon: ${String.format(Locale.US, "%.5f", loc.longitude)}"
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
