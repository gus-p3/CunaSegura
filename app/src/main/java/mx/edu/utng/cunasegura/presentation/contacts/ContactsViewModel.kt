package mx.edu.utng.cunasegura.presentation.contacts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.ContactoEmergencia
import mx.edu.utng.cunasegura.domain.usecase.AgregarContactoUseCase
import mx.edu.utng.cunasegura.domain.usecase.EliminarContactoUseCase
import mx.edu.utng.cunasegura.domain.usecase.ObtenerContactosUseCase
import mx.edu.utng.cunasegura.domain.usecase.ObtenerUsuarioActualUseCase

const val MAX_CONTACTOS = 5
val RELACIONES = listOf("Mamá", "Papá", "Pareja", "Hermano/a", "Otro")

data class ContactsFormState(
    val nombre: String = "",
    val telefono: String = "",
    val relacion: String = RELACIONES.first()
)

class ContactsViewModel(
    private val agregarContactoUseCase: AgregarContactoUseCase,
    private val eliminarContactoUseCase: EliminarContactoUseCase,
    private val obtenerContactosUseCase: ObtenerContactosUseCase,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase
) : ViewModel() {

    private var usuarioId: Int = 0

    private val _contactos = MutableStateFlow<List<ContactoEmergencia>>(emptyList())
    val contactos: StateFlow<List<ContactoEmergencia>> = _contactos.asStateFlow()

    private val _showAddSheet = MutableStateFlow(false)
    val showAddSheet: StateFlow<Boolean> = _showAddSheet.asStateFlow()

    private val _formState = MutableStateFlow(ContactsFormState())
    val formState: StateFlow<ContactsFormState> = _formState.asStateFlow()

    init {
        viewModelScope.launch {
            val usuario = obtenerUsuarioActualUseCase()
            usuarioId = usuario?.id ?: 0
            obtenerContactosUseCase(usuarioId).collect { lista ->
                _contactos.value = lista
            }
        }
    }

    fun onShowAddSheet(show: Boolean) {
        if (show) _formState.value = ContactsFormState() // limpia el formulario al abrir
        _showAddSheet.value = show
    }

    fun onNombreChange(value: String) {
        _formState.value = _formState.value.copy(nombre = value)
    }

    fun onTelefonoChange(value: String) {
        _formState.value = _formState.value.copy(telefono = value.filter { it.isDigit() }.take(10))
    }

    fun onRelacionChange(value: String) {
        _formState.value = _formState.value.copy(relacion = value)
    }

    fun puedeAgregar(): Boolean = _contactos.value.size < MAX_CONTACTOS

    fun onAgregarContacto() {
        val form = _formState.value
        if (form.nombre.isBlank() || form.telefono.length < 10) return
        if (!puedeAgregar()) return

        viewModelScope.launch {
            agregarContactoUseCase(
                ContactoEmergencia(
                    usuarioId = usuarioId,
                    nombre = form.nombre,
                    telefono = form.telefono,
                    relacion = form.relacion
                )
            )
            _showAddSheet.value = false
        }
    }

    fun onEliminarContacto(id: Int) {
        viewModelScope.launch {
            eliminarContactoUseCase(id)
        }
    }
}

class ContactsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactsViewModel(
                AppModule.provideAgregarContactoUseCase(context),
                AppModule.provideEliminarContactoUseCase(context),
                AppModule.provideObtenerContactosUseCase(context),
                AppModule.provideObtenerUsuarioActualUseCase(context)
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}