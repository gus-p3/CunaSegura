package mx.edu.utng.cunasegurawear.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mx.edu.utng.cunasegurawear.data.db.TouchConfigDao
import mx.edu.utng.cunasegurawear.domain.model.AlertPhase
import mx.edu.utng.cunasegurawear.domain.model.AlertState
import mx.edu.utng.cunasegurawear.domain.usecase.CancelAlertUseCase
import mx.edu.utng.cunasegurawear.domain.usecase.GetSosActionsUseCase
import mx.edu.utng.cunasegurawear.domain.usecase.TriggerSosUseCase

class WatchViewModel(
    private val triggerSos: TriggerSosUseCase,
    private val cancelAlert: CancelAlertUseCase,
    private val getSosActions: GetSosActionsUseCase,
    private val touchConfigDao: TouchConfigDao
) : ViewModel() {
    private val _state = MutableStateFlow(AlertState())
    val state: StateFlow<AlertState> = _state.asStateFlow()
    private var countdownJob: Job? = null
    private var lifeCheckJob: Job? = null

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
                }
                result.onFailure {
                    _state.update { it.copy(phase = AlertPhase.IDLE) }
                }
            }
        }
    }

    fun onCancelCountdown() {
        countdownJob?.cancel()
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
}
