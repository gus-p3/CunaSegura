package mx.edu.utng.cunasegura.presentation.contacts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utng.cunasegura.domain.model.ContactoEmergencia

// Colors will use MaterialTheme
private val AzulCunaSegura @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary

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
        modifier = Modifier.imePadding(),
        containerColor = MaterialTheme.colorScheme.primary,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Contactos de Confianza",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulCunaSegura
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { if (puedeAgregar) viewModel.onShowAddSheet(true) },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text(if (puedeAgregar) "Añadir contacto" else "Máximo 5 contactos") },
                containerColor = if (puedeAgregar) MaterialTheme.colorScheme.primary else Color.Gray,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        if (contactos.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.People,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Aún no tienes contactos de confianza",
                        color = Color.Gray,
                        fontSize = 15.sp
                    )
                    Text(
                        "Agrega hasta 5 contactos que recibirán tus alertas",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                    .padding(padding)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(contactos, key = { it.id }) { contacto ->
                    ContactoItem(
                        contacto = contacto,
                        onEliminar = { viewModel.onEliminarContacto(contacto.id) }
                    )
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
    val context = LocalContext.current
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        onClick = { /* no-op */ }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iniciales(contacto.nombre),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    contacto.nombre,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    fontSize = 15.sp
                )
                Text(
                    contacto.relacion,
                    color = Color.Gray,
                    fontSize = 13.sp
                )
                Text(
                    contacto.telefono,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Botón llamar real
            IconButton(onClick = {
                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contacto.telefono}"))
                context.startActivity(intent)
            }) {
                Icon(
                    Icons.Default.Call,
                    contentDescription = "Llamar",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            IconButton(onClick = { mostrarConfirmacion = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = androidx.compose.material3.MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            title = { Text("Eliminar contacto", color = Color.DarkGray) },
            text = { Text("¿Seguro que quieres eliminar a ${contacto.nombre}?", color = Color.Gray) },
            confirmButton = {
                TextButton(onClick = {
                    onEliminar()
                    mostrarConfirmacion = false
                }) { Text("Eliminar", color = androidx.compose.material3.MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.primary)
                }
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
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        title = {
            Text(
                "Añadir contacto de confianza",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = form.nombre,
                    onValueChange = viewModel::onNombreChange,
                    label = { Text("Nombre completo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = form.telefono,
                    onValueChange = viewModel::onTelefonoChange,
                    label = { Text("Teléfono") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLabelColor = MaterialTheme.colorScheme.primary
                    )
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            focusedLabelColor = MaterialTheme.colorScheme.primary
                        )
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
            Button(
                onClick = { viewModel.onAgregarContacto() },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) { Text("Guardar", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = Color.Gray)
            }
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