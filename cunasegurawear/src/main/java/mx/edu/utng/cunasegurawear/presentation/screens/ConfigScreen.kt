package mx.edu.utng.cunasegurawear.presentation.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import mx.edu.utng.cunasegurawear.data.db.TouchConfig
import mx.edu.utng.cunasegurawear.domain.model.SosAction

@Composable
fun ConfigScreen(
    configs: List<TouchConfig>,
    onUpdateConfig: (tapNumber: Int, action: SosAction) -> Unit,
    onBack: () -> Unit
) {
    var editingTapNumber by remember { mutableStateOf<Int?>(null) }

    if (editingTapNumber != null) {
        val tapNumber = editingTapNumber!!
        val currentConfig = configs.find { it.tapNumber == tapNumber }
        
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "SELECCIONAR ACCIÓN\nPARA $tapNumber TOQUE(S)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(SosAction.values()) { action ->
                val isSelected = currentConfig?.actionName == action.name
                Chip(
                    label = { Text(action.label, fontSize = 10.sp) },
                    onClick = {
                        onUpdateConfig(tapNumber, action)
                        editingTapNumber = null
                    },
                    colors = if (isSelected) {
                        ChipDefaults.primaryChipColors()
                    } else {
                        ChipDefaults.secondaryChipColors()
                    },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                )
            }
            item {
                Chip(
                    label = { Text("Cancelar", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    onClick = { editingTapNumber = null },
                    colors = ChipDefaults.childChipColors(),
                    modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                )
            }
        }
    } else {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Text(
                    text = "CONFIGURAR TOQUES",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            items(configs) { config ->
                Chip(
                    label = { Text("${config.tapNumber} Toque(s)", fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                    secondaryLabel = { Text(config.actionLabel, fontSize = 9.sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)) },
                    onClick = { editingTapNumber = config.tapNumber },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                    colors = ChipDefaults.secondaryChipColors()
                )
            }
            item {
                Chip(
                    label = { Text("Regresar", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    onClick = onBack,
                    colors = ChipDefaults.childChipColors(),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}
