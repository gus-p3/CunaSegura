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

private val AzulCunaSegura = Color(0xFF1F4E79)
private val VerdeAdmin = Color(0xFF4CAF50)
private val RojoAdmin = Color(0xFFD32F2F)

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
                title = { Text("Gestión de Miembros", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FC))
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
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
                                colors = CardDefaults.cardColors(containerColor = Color.White),
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
                            MiembroCard(usuario = usuario)
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
                modifier = Modifier.size(44.dp).clip(CircleShape).background(AzulCunaSegura),
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
