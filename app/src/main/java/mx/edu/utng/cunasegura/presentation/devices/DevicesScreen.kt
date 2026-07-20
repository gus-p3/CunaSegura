package mx.edu.utng.cunasegura.presentation.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
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
private val ColorVinculado @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.secondary
private val ColorNoVinculado @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.outline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevicesScreen(
    onNavigateToWatchConfig: () -> Unit,
    onNavigateToTvConfig: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: DevicesViewModel = viewModel(
        factory = DevicesViewModelFactory(context)
    )

    val usuario by viewModel.usuario.collectAsState()
    val contactCount by viewModel.contactCount.collectAsState()

    // Helper para generar iniciales
    val nombreUsuario = usuario?.nombre ?: "Vecino"
    val iniciales = getInitials(nombreUsuario)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Dispositivos Vinculados", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Avatar del usuario con iniciales
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .shadow(4.dp, CircleShape)
                    .clip(CircleShape)
                    .background(AzulCunaSegura),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = iniciales,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Texto "Tus Dispositivos / [Nombre]"
            Text(
                text = "Tus Dispositivos / $nombreUsuario",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AzulCunaSegura
            )

            Spacer(modifier = Modifier.height(32.dp))

            // LazyVerticalGrid de 2x2 para acomodar los dispositivos y la tarjeta de contactos
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Tarjeta SmartWatch
                item {
                    DeviceCard(
                        name = "SmartWatch",
                        icon = Icons.Default.Info, // Reemplazo de Reloj en core
                        isLinked = false,
                        onClick = onNavigateToWatchConfig
                    )
                }

                // Tarjeta Smart TV
                item {
                    DeviceCard(
                        name = "Smart TV",
                        icon = Icons.Default.PlayArrow, // Reemplazo de TV en core
                        isLinked = usuario?.tvVinculada ?: false,
                        onClick = onNavigateToTvConfig
                    )
                }

                // Tercera tarjeta: Contactos Configurados (Ancho completo)
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(AzulCunaSegura.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "Contactos",
                                    tint = AzulCunaSegura,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Contactos Configurados",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "$contactCount de confianza en Room",
                                    fontSize = 13.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceCard(
    name: String,
    icon: ImageVector,
    isLinked: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = AzulCunaSegura,
                modifier = Modifier.size(36.dp)
            )

            Text(
                text = name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                // Indicador circular verde o gris
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isLinked) ColorVinculado else ColorNoVinculado)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isLinked) "Vinculado" else "No vinculado",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isLinked) ColorVinculado else ColorNoVinculado
                )
            }
        }
    }
}

private fun getInitials(name: String): String {
    if (name.isBlank()) return "V"
    val parts = name.trim().split("\\s+".toRegex())
    val initials = parts.take(2).map { it.firstOrNull()?.uppercaseChar() ?: "" }.joinToString("")
    return if (initials.isEmpty()) "V" else initials
}
