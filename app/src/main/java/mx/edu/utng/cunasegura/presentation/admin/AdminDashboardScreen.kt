package mx.edu.utng.cunasegura.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val AzulCunaSegura @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
private val DoradoAdmin @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.tertiary
private val VerdeAdmin @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.secondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen() {
    val context = LocalContext.current
    val viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(context))

    val usuarios by viewModel.usuarios.collectAsState()
    val totalUsuarios by viewModel.totalUsuarios.collectAsState()
    val adminActual by viewModel.adminActual.collectAsState()

    val vecinos = usuarios.filter { it.rol != "admin" }
    val vectinosActivos = vecinos.filter { it.estado == "activo" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen de Estado", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(AzulCunaSegura.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = adminActual?.nombre?.split(" ")?.let {
                                if (it.size >= 2) "${it[0].first()}${it[1].first()}".uppercase()
                                else adminActual?.nombre?.take(2)?.uppercase()
                            } ?: "AD"
                            Text(initials, color = AzulCunaSegura, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Bienvenido,", color = Color.Gray, fontSize = 13.sp)
                            Text(adminActual?.nombre ?: "Administrador", color = AzulCunaSegura, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DoradoAdmin))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Administrador Global", color = DoradoAdmin, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            item {
                Text("Estadísticas de la Red", fontWeight = FontWeight.Bold, color = AzulCunaSegura, fontSize = 15.sp)
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
                        color = AzulCunaSegura
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

            // Módulos en Desarrollo (placeholder from previous screen)
            item {
                Text("Módulos en Desarrollo", fontWeight = FontWeight.Bold, color = AzulCunaSegura, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModuloRow("🔥 Firebase Realtime DB", "Alertas en tiempo real entre vecinos", pendiente = true)
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        ModuloRow("📲 FCM Push", "Notificaciones push a celulares y TV", pendiente = true)
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        ModuloRow("📺 Smart TV App", "App de monitoreo en Android TV", pendiente = true)
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        ModuloRow("⌚ BLE Smartwatch", "Comunicación Bluetooth con watch", pendiente = true)
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        ModuloRow("🗺️ Mapa Comunitario", "Marcadores de vecinos en mapa", completado = true)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, titulo: String, valor: String, icon: ImageVector, color: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
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
            color = if (completado) androidx.compose.material3.MaterialTheme.colorScheme.secondary else androidx.compose.material3.MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold
        )
    }
}
