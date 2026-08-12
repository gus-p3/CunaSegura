package mx.edu.utng.cunasegura.presentation.emergency

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.cunasegura.data.local.dao.ContactoDao
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Alerta
import mx.edu.utng.cunasegura.domain.repository.IAlertaRepository
import mx.edu.utng.cunasegura.domain.usecase.CancelarAlertaUseCase

/**
 * ViewModel que controla la pantalla de emergencia activa.
 *
 * Muestra el estado del incidente, número de contactos notificados y ejecuta una cuenta regresiva visual
 * mientras permite cancelar la alerta si se trató de una falsa alarma.
 *
 * @property alertaId Identificador de la alerta activa en curso.
 * @property alertaRepository Repositorio de alertas.
 * @property contactoDao DAO para contar contactos de confianza.
 * @property cancelarAlertaUseCase Caso de uso para cancelación de alerta.
 */
class EmergencyViewModel(
    private val alertaId: Int,
    private val alertaRepository: IAlertaRepository,
    private val contactoDao: ContactoDao,
    private val cancelarAlertaUseCase: CancelarAlertaUseCase
) : ViewModel() {

    private val _alerta = MutableStateFlow<Alerta?>(null)
    val alerta: StateFlow<Alerta?> = _alerta.asStateFlow()

    private val _contactCount = MutableStateFlow(0)
    val contactCount: StateFlow<Int> = _contactCount.asStateFlow()

    private val _secondsLeft = MutableStateFlow(8)
    val secondsLeft: StateFlow<Int> = _secondsLeft.asStateFlow()

    private val _alertaCancelada = MutableStateFlow(false)
    val alertaCancelada: StateFlow<Boolean> = _alertaCancelada.asStateFlow()

    init {
        cargarAlertaYContactos()
        iniciarCuentaRegresiva()
    }

    /**
     * Recupera el detalle de la alerta y cuenta los contactos de auxilio asociados.
     */
    private fun cargarAlertaYContactos() {
        viewModelScope.launch {
            val alert = alertaRepository.obtenerAlertaPorId(alertaId)
            _alerta.value = alert
            if (alert != null) {
                // Cuenta los contactos de Room para el usuario de la alerta
                val contactos = contactoDao.obtenerPorUsuario(alert.usuarioId)
                _contactCount.value = contactos.size
            }
        }
    }

    /**
     * Inicia una cuenta regresiva de 8 segundos en corrutina para el estado de notificación.
     */
    private fun iniciarCuentaRegresiva() {
        viewModelScope.launch {
            for (sec in (0..7).reversed()) {
                _secondsLeft.value = sec + 1
                delay(1000)
            }
            _secondsLeft.value = 0
        }
    }

    /**
     * Ejecuta la cancelación de la alerta activa y actualiza el estado de la UI.
     */
    fun cancelarAlerta() {
        viewModelScope.launch {
            cancelarAlertaUseCase(alertaId)
            _alertaCancelada.value = true
        }
    }
}

/**
 * Fábrica para instanciar [EmergencyViewModel] con el ID de la alerta correspondiente.
 */
class EmergencyViewModelFactory(
    private val context: Context,
    private val alertaId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmergencyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmergencyViewModel(
                alertaId = alertaId,
                alertaRepository = AppModule.provideAlertaRepository(context),
                contactoDao = AppModule.provideDatabase(context).contactoDao(),
                cancelarAlertaUseCase = AppModule.provideCancelarAlertaUseCase(context)
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}

