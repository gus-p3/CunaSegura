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

/**
 * Estado inmutable del formulario modal para agregar o editar un contacto de auxilio.
 *
 * @property id Identificador del contacto (0 si es nuevo).
 * @property nombre Nombre o alias.
 * @property telefono Teléfono de 10 dígitos.
 * @property relacion Parentesco o vínculo seleccionado.
 */
data class ContactsFormState(
    val id: Int = 0,
    val nombre: String = "",
    val telefono: String = "",
    val relacion: String = RELACIONES.first()
)

/**
 * ViewModel encargado del directorio y gestión de contactos de emergencia.
 *
 * Limita el registro a un máximo de 5 contactos ([MAX_CONTACTOS]) y coordina operaciones CRUD en la nube.
 *
 * @property agregarContactoUseCase Caso de uso para agregar/actualizar contacto.
 * @property eliminarContactoUseCase Caso de uso para eliminar contacto.
 * @property obtenerContactosUseCase Caso de uso para observar el flujo de contactos.
 * @property obtenerUsuarioActualUseCase Caso de uso para asociar el ID de usuario correspondiente.
 */
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

    /**
     * Muestra la hoja modal precargando los datos del contacto o en blanco si es nuevo.
     *
     * @param contacto Contacto a editar o `null` si es una nueva inserción.
     */
    fun onShowSheet(contacto: ContactoEmergencia?) {
        if (contacto != null) {
            _formState.value = ContactsFormState(contacto.id, contacto.nombre, contacto.telefono, contacto.relacion)
        } else {
            _formState.value = ContactsFormState()
        }
        _showAddSheet.value = true
    }

    /**
     * Oculta la hoja modal del formulario.
     */
    fun onHideSheet() {
        _showAddSheet.value = false
    }

    /**
     * Actualiza el nombre en el formulario.
     */
    fun onNombreChange(value: String) {
        _formState.value = _formState.value.copy(nombre = value)
    }

    /**
     * Actualiza y filtra el teléfono a 10 dígitos numéricos.
     */
    fun onTelefonoChange(value: String) {
        _formState.value = _formState.value.copy(telefono = value.filter { it.isDigit() }.take(10))
    }

    /**
     * Actualiza el parentesco seleccionado.
     */
    fun onRelacionChange(value: String) {
        _formState.value = _formState.value.copy(relacion = value)
    }

    /**
     * Valida si el usuario aún puede añadir más contactos respetando el límite máximo permitido.
     */
    fun puedeAgregar(): Boolean = _contactos.value.size < MAX_CONTACTOS

    /**
     * Valida y persiste el contacto de emergencia en Firebase y localmente.
     */
    fun onGuardarContacto() {
        val form = _formState.value
        if (form.nombre.isBlank() || form.telefono.length < 10) return
        if (form.id == 0 && !puedeAgregar()) return

        viewModelScope.launch {
            agregarContactoUseCase(
                ContactoEmergencia(
                    id = form.id,
                    usuarioId = usuarioId,
                    nombre = form.nombre,
                    telefono = form.telefono,
                    relacion = form.relacion
                )
            )
            _showAddSheet.value = false
        }
    }

    /**
     * Elimina el contacto especificado por ID.
     *
     * @param id Identificador del contacto.
     */
    fun onEliminarContacto(id: Int) {
        viewModelScope.launch {
            eliminarContactoUseCase(id)
        }
    }
}

/**
 * Fábrica para instanciar [ContactsViewModel] inyectando casos de uso desde [AppModule].
 */
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