package mx.edu.utng.cunasegura.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Usuario

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import android.widget.Toast
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.Tv

private val AzulCunaSegura @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
private val RojoSOS @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onLogout: () -> Unit,
    onNavigateToNetworks: () -> Unit,
    onNavigateToWatchConfig: () -> Unit = {},
    onNavigateToTvConfig: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    var editNombre by remember { mutableStateOf("") }
    var editTelefono by remember { mutableStateOf("") }
    var editPassword by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val cargarUsuario = {
        coroutineScope.launch {
            val uc = AppModule.provideObtenerUsuarioActualUseCase(context)
            usuario = uc()
        }
    }

    LaunchedEffect(Unit) {
        cargarUsuario()
    }

    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi Perfil",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        editNombre = usuario?.nombre ?: ""
                        editTelefono = usuario?.telefono ?: ""
                        editPassword = ""
                        showEditDialog = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Perfil", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(AzulCunaSegura),
                contentAlignment = Alignment.Center
            ) {
                val iniciales = usuario?.nombre
                    ?.trim()
                    ?.split(" ")
                    ?.filter { it.isNotBlank() }
                    ?.take(2)
                    ?.joinToString("") { it.first().uppercase() }
                    ?: "?"
                Text(
                    text = iniciales,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = usuario?.nombre ?: "Cargando...",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AzulCunaSegura
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(AzulCunaSegura.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Vecino Verificado",
                    color = AzulCunaSegura,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Editar Perfil principal
            OutlinedButton(
                onClick = {
                    editNombre = usuario?.nombre ?: ""
                    editTelefono = usuario?.telefono ?: ""
                    editPassword = ""
                    showEditDialog = true
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = AzulCunaSegura)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Modificar Datos / Contraseña", fontWeight = FontWeight.Bold, color = AzulCunaSegura)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!usuario?.correo.isNullOrBlank()) {
                        ProfileRow(icon = Icons.Default.Email, label = "Correo", value = usuario?.correo ?: "")
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                    }
                    if (!usuario?.telefono.isNullOrBlank()) {
                        ProfileRow(icon = Icons.Default.Call, label = "Teléfono", value = usuario?.telefono ?: "")
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                    }
                    ProfileRow(icon = Icons.Default.Shield, label = "Estado", value = usuario?.estado?.replaceFirstChar { it.uppercase() } ?: "Activo")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Configuración de Dispositivos completa
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Dispositivos Vinculados y Toques",
                        fontWeight = FontWeight.Bold,
                        color = AzulCunaSegura,
                        fontSize = 14.sp
                    )
                    
                    // Item SmartWatch BLE y Toques
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Watch, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SmartWatch BLE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Text("Configurar gestos y toques SOS", fontSize = 12.sp, color = Color.Gray)
                        }
                        TextButton(onClick = onNavigateToWatchConfig) {
                            Text("Configurar", fontWeight = FontWeight.Bold, color = AzulCunaSegura)
                        }
                    }

                    HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)

                    // Item Smart TV Vecinal
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Tv, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Smart TV Vecinal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Text(if (usuario?.tvVinculada == true) "TV Vinculada" else "Vincular TV de la Red", fontSize = 12.sp, color = Color.Gray)
                        }
                        TextButton(onClick = onNavigateToTvConfig) {
                            Text(if (usuario?.tvVinculada == true) "Ajustes" else "Vincular", fontWeight = FontWeight.Bold, color = AzulCunaSegura)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                onClick = onNavigateToNetworks,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Red Vecinal", fontWeight = FontWeight.Bold, color = AzulCunaSegura, fontSize = 14.sp)
                        Text("Configurar, buscar o escanear QR", fontSize = 12.sp, color = Color.Gray)
                    }
                    Text("Configurar", fontSize = 12.sp, color = AzulCunaSegura, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RojoSOS)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSaving) showEditDialog = false },
            title = { Text("Editar Perfil", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editNombre,
                        onValueChange = { editNombre = it },
                        label = { Text("Nombre Completo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editTelefono,
                        onValueChange = { editTelefono = it },
                        label = { Text("Teléfono de Contacto") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPassword,
                        onValueChange = { editPassword = it },
                        label = { Text("Nueva Contraseña (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editNombre.isBlank()) {
                            Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        coroutineScope.launch {
                            val repo = AppModule.provideUsuarioRepository(context)
                            val res = repo.actualizarPerfilUsuario(
                                nombre = editNombre,
                                telefono = editTelefono,
                                nuevaPassword = editPassword.ifBlank { null }
                            )
                            isSaving = false
                            if (res.isSuccess) {
                                Toast.makeText(context, "¡Perfil actualizado con éxito!", Toast.LENGTH_SHORT).show()
                                showEditDialog = false
                                cargarUsuario()
                            } else {
                                Toast.makeText(context, "Error: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = AzulCunaSegura)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Guardar Cambios")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }, enabled = !isSaving) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    // Clear local session
                    val prefs = mx.edu.utng.cunasegura.data.local.prefs.PreferencesManager(context)
                    // Firebase sign out
                    try {
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    } catch (e: Exception) {
                        // If Firebase not initialized yet, ignore
                    }
                    onLogout()
                }) {
                    Text("Cerrar sesión", color = RojoSOS)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = AzulCunaSegura)
                }
            }
        )
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = label, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
        }
    }
}
