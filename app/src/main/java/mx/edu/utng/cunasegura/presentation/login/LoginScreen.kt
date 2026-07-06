package mx.edu.utng.cunasegura.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.draw.clip

private val AzulCunaSegura = Color(0xFF1F4E79)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    // Navega en cuanto el ViewModel confirme el login
    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) {
            onLoginSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo placeholder
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(AzulCunaSegura),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CS",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "ALERTA CIUDADANA",
            color = AzulCunaSegura,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "DOLORES HIDALGO",
            color = AzulCunaSegura,
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = uiState.phoneNumber,
            onValueChange = { viewModel.onPhoneNumberChange(it) },
            label = { Text("Número de teléfono") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            isError = uiState.errorMessage != null,
            modifier = Modifier.fillMaxWidth()
        )

        if (uiState.errorMessage != null) {
            Text(
                text = uiState.errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                fontSize = 12.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { viewModel.onLoginClick() },
            enabled = !uiState.isLoading,
            colors = ButtonDefaults.buttonColors(containerColor = AzulCunaSegura),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Text("Ingresar con número de Teléfono")
            }
        }
    }
}