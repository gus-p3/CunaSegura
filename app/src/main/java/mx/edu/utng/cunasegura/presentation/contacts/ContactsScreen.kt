package mx.edu.utng.cunasegura.presentation.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utng.cunasegura.domain.model.ContactoEmergencia

private val AzulCunaSegura = Color(0xFF1F4E79)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen() {
    val context = LocalContext.current
    val viewModel: ContactsViewModel = viewModel(
        factory = ContactsViewModelFactory(context)
    )

    val contactos by viewModel.contactos.collectAsState()
    val showSheet by viewModel.showAddSheet.collectAsState()
    val puedeAgregar = contactos.size < MAX_CONTACTOS

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Contactos de Confianza") })
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.onShowAddSheet(true) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(if (puedeAgregar) "Añadir contacto" else "Máximo 5 contactos") },
                containerColor = if (puedeAgregar) AzulCunaSegura else Color.Gray
            )
        }
    ) { padding ->
        if (contactos.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Aún no tienes contactos de confianza")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp)
            ) {
                items(contactos, key = { it.id }) { contacto ->
                    ContactoItem(
                        contacto = contacto,
                        onEliminar = { viewModel.onEliminarContacto(contacto.id) }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showSheet) {
        AddContactDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.onShowAddSheet(false) }
        )
    }
}

@Composable
private fun ContactoItem(
    contacto: ContactoEmergencia,
    onEliminar: () -> Unit
) {
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { /* no-op, solo decorativo */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AzulCunaSegura),
                contentAlignment = Alignment.Center
            ) {
                Text(text = iniciales(contacto.nombre), color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(contacto.nombre, fontWeight = FontWeight.Bold)
                Text("${contacto.relacion}", fontSize = androidx.compose.ui.unit.TextUnit.Unspecified)
            }

            IconButton(onClick = { /* Intent.ACTION_DIAL se agrega en un sprint con permisos de llamada */ }) {
                Icon(Icons.Default.Call, contentDescription = "Llamar")
            }

            IconButton(onClick = { mostrarConfirmacion = true }) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar")
            }
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Eliminar contacto") },
            text = { Text("¿Seguro que quieres eliminar a ${contacto.nombre} de tus contactos de confianza?") },
            confirmButton = {
                TextButton(onClick = {
                    onEliminar()
                    mostrarConfirmacion = false
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) { Text("Cancelar") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddContactDialog(
    viewModel: ContactsViewModel,
    onDismiss: () -> Unit
) {
    val form by viewModel.formState.collectAsState()
    var expandedRelacion by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir contacto") },
        text = {
            Column {
                OutlinedTextField(
                    value = form.nombre,
                    onValueChange = viewModel::onNombreChange,
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = form.telefono,
                    onValueChange = viewModel::onTelefonoChange,
                    label = { Text("Teléfono") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedRelacion,
                    onExpandedChange = { expandedRelacion = it }
                ) {
                    OutlinedTextField(
                        value = form.relacion,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Relación") },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRelacion,
                        onDismissRequest = { expandedRelacion = false }
                    ) {
                        RELACIONES.forEach { opcion ->
                            DropdownMenuItem(
                                text = { Text(opcion) },
                                onClick = {
                                    viewModel.onRelacionChange(opcion)
                                    expandedRelacion = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { viewModel.onAgregarContacto() }) { Text("Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private fun iniciales(nombre: String): String {
    return nombre.trim()
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
}