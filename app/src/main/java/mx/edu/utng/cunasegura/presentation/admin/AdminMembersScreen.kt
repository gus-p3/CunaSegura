package mx.edu.utng.cunasegura.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utng.cunasegura.domain.model.Usuario

private val AzulCunaSegura @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
private val VerdeAdmin @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.secondary
private val RojoAdmin @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMembersScreen() {
    val context = LocalContext.current
    val viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(context))

    val usuarios by viewModel.usuarios.collectAsState()
    val vecinos = usuarios.filter { it.rol != "admin" }
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Miembros", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                contentColor = AzulCunaSegura
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Activos (${vecinos.size})") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Solicitudes (0)") }
                )
            }

            if (selectedTabIndex == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (vecinos.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("👥", fontSize = 32.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Sin vecinos registrados", color = Color.Gray, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    } else {
                        items(vecinos) { usuario ->
                            MiembroCard(
                                usuario = usuario,
                                onToggleEstado = { nuevoEstado ->
                                    viewModel.cambiarEstadoUsuario(usuario.uid, nuevoEstado)
                                }
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay solicitudes pendientes", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun MiembroCard(
    usuario: Usuario,
    onToggleEstado: (String) -> Unit
) {
    val iniciales = usuario.nombre.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    val esActivo = usuario.estado == "activo"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(if (esActivo) AzulCunaSegura else Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(iniciales, color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("📞 ${usuario.telefono}", fontSize = 12.sp, color = Color.Gray)
                if (usuario.correo.isNotBlank()) Text("✉️ ${usuario.correo}", fontSize = 11.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(
                        if (esActivo) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.errorContainer
                    ).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = usuario.estado.replaceFirstChar { it.uppercase() },
                        fontSize = 11.sp,
                        color = if (esActivo) VerdeAdmin else RojoAdmin,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { onToggleEstado(if (esActivo) "bloqueado" else "activo") },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = if (esActivo) "Bloquear" else "Activar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (esActivo) RojoAdmin else VerdeAdmin
                    )
                }
            }
        }
    }
}
