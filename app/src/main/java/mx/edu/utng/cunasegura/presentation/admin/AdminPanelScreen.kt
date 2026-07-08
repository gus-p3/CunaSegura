package mx.edu.utng.cunasegura.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utng.cunasegura.domain.model.Usuario

private val AzulAdmin = Color(0xFF0D2137)
private val AzulSecundario = Color(0xFF1F4E79)
private val DoradoAdmin = Color(0xFFFFC107)
private val VerdeAdmin = Color(0xFF4CAF50)
private val RojoAdmin = Color(0xFFD32F2F)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(context))

    val usuarios by viewModel.usuarios.collectAsState()
    val totalUsuarios by viewModel.totalUsuarios.collectAsState()
    val adminActual by viewModel.adminActual.collectAsState()

    val vecinos = usuarios.filter { !it.esAdminGlobal }
    val vectinosActivos = vecinos.filter { it.estado == "activo" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = DoradoAdmin, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Panel de Administración", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Cuna Segura — Admin Global", color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.recargar() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar", tint = Color.White)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulAdmin)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF0F4F8))
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Bienvenida Admin ─────────────────────────────────────────
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(colors = listOf(AzulAdmin, AzulSecundario))
                            )
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(DoradoAdmin),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("BA", color = AzulAdmin, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Bienvenido,", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
                                Text(adminActual?.nombre ?: "Administrador", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DoradoAdmin))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Administrador Global", color = DoradoAdmin, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            // ── Estadísticas rápidas ─────────────────────────────────────
            item {
                Text("Estadísticas de la Plataforma", fontWeight = FontWeight.Bold, color = AzulAdmin, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        titulo = "Vecinos",
                        valor = vecinos.size.toString(),
                        icon = Icons.Default.People,
                        color = AzulSecundario
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        titulo = "Activos",
                        valor = vectinosActivos.size.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = VerdeAdmin
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        titulo = "Total",
                        valor = totalUsuarios.toString(),
                        icon = Icons.Default.Group,
                        color = DoradoAdmin
                    )
                }
            }

            // ── Config global ────────────────────────────────────────────
            item {
                Text("Configuración Global de la Red", fontWeight = FontWeight.Bold, color = AzulAdmin, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminConfigRow(icon = Icons.Default.LocationOn, label = "Radio de detección automática", valor = "200 metros")
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                        AdminConfigRow(icon = Icons.Default.Security, label = "Tipo de red", valor = "GPS Abierta + QR")
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                        AdminConfigRow(icon = Icons.Default.Timer, label = "Tiempo anti-falsa alarma", valor = "5 segundos")
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                        AdminConfigRow(icon = Icons.Default.Notifications, label = "Check de vida cada", valor = "2 minutos")
                    }
                }
            }

            // ── Lista de Miembros ────────────────────────────────────────
            item {
                Text("Gestión de Miembros (${vecinos.size})", fontWeight = FontWeight.Bold, color = AzulAdmin, fontSize = 15.sp)
            }

            if (vecinos.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("👥", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Sin vecinos registrados", color = Color.Gray, textAlign = TextAlign.Center)
                                Text("Los vecinos aparecerán aquí cuando inicien sesión.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            } else {
                items(vecinos) { usuario ->
                    MiembroCard(usuario = usuario)
                }
            }

            // ── Módulos futuros ──────────────────────────────────────────
            item {
                Text("Módulos en Desarrollo", fontWeight = FontWeight.Bold, color = AzulAdmin, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModuloRow("🔥 Firebase Realtime DB", "Alertas en tiempo real entre vecinos", pendiente = true)
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                        ModuloRow("📲 FCM Push", "Notificaciones push a celulares y TV", pendiente = true)
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                        ModuloRow("📺 Smart TV App", "App de monitoreo en Android TV", pendiente = true)
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                        ModuloRow("⌚ BLE Smartwatch", "Comunicación Bluetooth con watch", pendiente = true)
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                        ModuloRow("🗺️ Mapa Comunitario", "Marcadores de vecinos en mapa", completado = true)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, titulo: String, valor: String, icon: ImageVector, color: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier.shadow(2.dp, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = titulo, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(valor, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(titulo, fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun AdminConfigRow(icon: ImageVector, label: String, valor: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = label, tint = AzulSecundario, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 13.sp, color = Color.DarkGray)
        }
        Text(valor, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AzulSecundario)
    }
}

@Composable
private fun MiembroCard(usuario: Usuario) {
    val iniciales = usuario.nombre.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(AzulSecundario),
                contentAlignment = Alignment.Center
            ) {
                Text(iniciales, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("📞 ${usuario.telefono}", fontSize = 12.sp, color = Color.Gray)
                if (usuario.correo.isNotBlank()) Text("✉️ ${usuario.correo}", fontSize = 11.sp, color = Color.Gray)
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(
                    if (usuario.estado == "activo") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                ).padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = usuario.estado.replaceFirstChar { it.uppercase() },
                    fontSize = 11.sp,
                    color = if (usuario.estado == "activo") VerdeAdmin else RojoAdmin,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ModuloRow(nombre: String, descripcion: String, pendiente: Boolean = false, completado: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(nombre, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(descripcion, fontSize = 11.sp, color = Color.Gray)
        }
        Text(
            text = if (completado) "✅ Listo" else "🔧 Próx.",
            fontSize = 11.sp,
            color = if (completado) VerdeAdmin else Color(0xFFFF9800),
            fontWeight = FontWeight.Bold
        )
    }
}
