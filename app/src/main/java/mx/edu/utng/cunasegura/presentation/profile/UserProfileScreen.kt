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

private val AzulCunaSegura @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
private val RojoSOS @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val uc = AppModule.provideObtenerUsuarioActualUseCase(context)
        usuario = uc()
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

            Spacer(modifier = Modifier.height(24.dp))

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
                    ProfileRow(icon = Icons.Default.Shield, label = "Estado", value = "Activo")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Acceso directo a configuración de dispositivos
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Dispositivos Vinculados",
                        fontWeight = FontWeight.Bold,
                        color = AzulCunaSegura,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Watch, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("SmartWatch BLE", fontSize = 14.sp, color = Color.DarkGray)
                        Spacer(modifier = Modifier.weight(1f))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (mx.edu.utng.cunasegura.data.local.prefs.PreferencesManager(LocalContext.current).isWatchLinked())
                                        androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.tertiaryContainer
                                )
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (mx.edu.utng.cunasegura.data.local.prefs.PreferencesManager(LocalContext.current).isWatchLinked())
                                    "Vinculado" else "Sin vincular",
                                fontSize = 11.sp,
                                color = if (mx.edu.utng.cunasegura.data.local.prefs.PreferencesManager(LocalContext.current).isWatchLinked())
                                    androidx.compose.material3.MaterialTheme.colorScheme.secondary else androidx.compose.material3.MaterialTheme.colorScheme.tertiary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
