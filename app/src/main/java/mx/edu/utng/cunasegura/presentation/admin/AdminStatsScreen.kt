package mx.edu.utng.cunasegura.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import mx.edu.utng.cunasegura.domain.model.Alerta
import java.text.SimpleDateFormat
import java.util.*

private val AzulCunaSegura @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
private val VerdeAdmin @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.secondary
private val RojoAdmin @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.error

data class DayStat(
    val dayName: String,
    val dateLabel: String,
    val realAlarms: Float,
    val falseAlarms: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatsScreen() {
    val context = LocalContext.current
    val viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(context))

    val alertas by viewModel.alertas.collectAsState()

    // 1. Calculate stats for the last 7 days
    val dayStats = remember(alertas) {
        val calendar = Calendar.getInstance()
        (0..6).map { offset ->
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -offset)
            }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfDay = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val endOfDay = cal.timeInMillis

            val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
            val dateLabelFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
            val dayName = dayNameFormat.format(cal.time)
            val dateLabel = dateLabelFormat.format(cal.time)

            // Filter alerts in this day range
            val dayAlerts = alertas.filter { it.creadoEn in startOfDay..endOfDay }
            val real = dayAlerts.count { !it.esFalsaAlarma }.toFloat()
            val falseAlarmsCount = dayAlerts.count { it.esFalsaAlarma }.toFloat()

            DayStat(dayName, dateLabel, real, falseAlarmsCount)
        }.reversed()
    }

    // 2. Prep entries for Vico
    val chartEntryModel = remember(dayStats) {
        val entriesReal = dayStats.mapIndexed { index, stat -> entryOf(index, stat.realAlarms) }
        val entriesFalse = dayStats.mapIndexed { index, stat -> entryOf(index, stat.falseAlarms) }
        if (entriesReal.isNotEmpty() || entriesFalse.isNotEmpty()) {
            entryModelOf(entriesReal, entriesFalse)
        } else {
            entryModelOf(emptyList<entryOf>())
        }
    }

    // 3. Aggregate metrics for the summary card
    val totalAlertas = alertas.size
    val alertasReales = alertas.count { !it.esFalsaAlarma }
    val alertasFalsas = alertas.count { it.esFalsaAlarma }
    val porcentajeFalsas = if (totalAlertas > 0) (alertasFalsas * 100) / totalAlertas else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial y Estadísticas", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Métricas de Alerta Semanales", fontWeight = FontWeight.Bold, color = AzulCunaSegura, fontSize = 15.sp)

            // Tarjeta de Resumen con Gradiente Premium
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    AzulCunaSegura,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Alertas Totales", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                            Text(porcentajeFalsas.toString() + "% Falsas", color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text(totalAlertas.toString(), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reales", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                Text(alertasReales.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Falsas", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                Text(alertasFalsas.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Gráfico Vico
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Distribución por Día", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
                    Text("■ Reales   ■ Falsas", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                    
                    if (totalAlertas > 0) {
                        Chart(
                            chart = columnChart(),
                            model = chartEntryModel,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(
                                valueFormatter = { value, _ ->
                                    val idx = value.toInt()
                                    if (idx in dayStats.indices) {
                                        dayStats[idx].dayName
                                    } else {
                                        ""
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(top = 16.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sin datos de alertas para graficar esta semana", color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // Historial de auditoría
            Text("Registro de Auditoría Reciente", fontWeight = FontWeight.Bold, color = AzulCunaSegura, fontSize = 15.sp)
            if (alertas.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No hay historial de alertas registrado", color = Color.Gray)
                    }
                }
            } else {
                alertas.sortedByDescending { it.creadoEn }.take(10).forEach { alerta ->
                    AlertaHistoryItem(alerta = alerta)
                }
            }
        }
    }
}

@Composable
fun AlertaHistoryItem(alerta: Alerta) {
    val date = Date(alerta.creadoEn)
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateStr = format.format(date)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (alerta.esFalsaAlarma) RojoAdmin.copy(alpha = 0.1f) else VerdeAdmin.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (alerta.esFalsaAlarma) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (alerta.esFalsaAlarma) RojoAdmin else VerdeAdmin
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(alerta.nombreUsuario, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(dateStr, fontSize = 11.sp, color = Color.Gray)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (alerta.esFalsaAlarma) RojoAdmin.copy(alpha = 0.15f) else VerdeAdmin.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (alerta.esFalsaAlarma) "Falsa Alarma" else "Real/Atendida",
                    fontSize = 11.sp,
                    color = if (alerta.esFalsaAlarma) RojoAdmin else VerdeAdmin,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
