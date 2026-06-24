package mx.edu.utng.cunasegurawear.presentation.screens

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.Text
import mx.edu.utng.cunasegurawear.domain.model.SosAction

@Composable
fun ConfigScreen(
    actions: List<SosAction>,
    onBack: () -> Unit
) {
    ScalingLazyColumn {
        item { Text("TOQUES PERSONALIZADOS", fontSize = 11.sp) }
        items(SosAction.values()) { action ->
            Chip(
                label = { Text("${action.tapNumber}. ${action.label}") },
                onClick = {},
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
