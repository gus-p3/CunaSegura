package mx.edu.utng.cunasegura.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AzulCunaSegura = Color(0xFF1F4E79)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatsScreen() {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial y Estadísticas", color = Color.White, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF7F9FC))
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("📊", fontSize = 48.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Estadísticas en Desarrollo",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = AzulCunaSegura
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Próximamente podrás ver gráficas de alertas activas, falsas y atendidas, junto con un mapa de calor.",
                fontSize = 14.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
        }
    }
}
