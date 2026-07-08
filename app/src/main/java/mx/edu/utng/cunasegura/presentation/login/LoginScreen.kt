package mx.edu.utng.cunasegura.presentation.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val AzulOscuro = Color(0xFF1F4E79)
private val AzulMedio = Color(0xFF2E6DA4)
private val AzulClaro = Color(0xFF4A90D9)

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onAdminSuccess: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    var passwordVisible by remember { mutableStateOf(false) }

    // Navegación al completar login
    LaunchedEffect(uiState.navigateToHome) {
        if (uiState.navigateToHome) onLoginSuccess()
    }
    LaunchedEffect(uiState.navigateToAdmin) {
        if (uiState.navigateToAdmin) onAdminSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AzulOscuro, AzulMedio, AzulClaro)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // ── Logo / Escudo ──────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Logo Cuna Segura",
                        tint = AzulOscuro,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "CUNA SEGURA",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp
            )
            Text(
                text = "ALERTA CIUDADANA",
                color = Color.White.copy(alpha = 0.85f),
                fontSize = 13.sp,
                letterSpacing = 2.sp
            )
            Text(
                text = "DOLORES HIDALGO",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 12.sp,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ── Card de Login ──────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // ── Toggle modo admin / vecino ────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (uiState.esAdmin) "Acceso Administrador" else "Acceso Vecino",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = AzulOscuro
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.onToggleAdminMode() }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Modo admin",
                                tint = if (uiState.esAdmin) AzulOscuro else Color.Gray,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Admin",
                                fontSize = 12.sp,
                                color = if (uiState.esAdmin) AzulOscuro else Color.Gray,
                                fontWeight = if (uiState.esAdmin) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // ── Campos según modo ─────────────────────────────────
                    AnimatedVisibility(visible = !uiState.esAdmin, enter = fadeIn(), exit = fadeOut()) {
                        Column {
                            // Campo teléfono (modo vecino)
                            OutlinedTextField(
                                value = uiState.phoneNumber,
                                onValueChange = { viewModel.onPhoneNumberChange(it) },
                                label = { Text("Número de teléfono") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = AzulMedio)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                isError = uiState.errorMessage != null && !uiState.esAdmin,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    AnimatedVisibility(visible = uiState.esAdmin, enter = fadeIn(), exit = fadeOut()) {
                        Column {
                            // Campo correo (modo admin)
                            OutlinedTextField(
                                value = uiState.correo,
                                onValueChange = { viewModel.onCorreoChange(it) },
                                label = { Text("Correo electrónico") },
                                leadingIcon = {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = AzulOscuro)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                singleLine = true,
                                isError = uiState.errorMessage != null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            // Campo contraseña
                            OutlinedTextField(
                                value = uiState.password,
                                onValueChange = { viewModel.onPasswordChange(it) },
                                label = { Text("Contraseña") },
                                leadingIcon = {
                                    Icon(Icons.Default.Lock, contentDescription = null, tint = AzulOscuro)
                                },
                                trailingIcon = {
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(
                                            if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                            contentDescription = if (passwordVisible) "Ocultar" else "Mostrar"
                                        )
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None
                                                      else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                singleLine = true,
                                isError = uiState.errorMessage != null,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    // ── Mensaje de error ──────────────────────────────────
                    if (uiState.errorMessage != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.errorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── Botón de ingreso ──────────────────────────────────
                    Button(
                        onClick = { viewModel.onLoginClick() },
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (uiState.esAdmin) AzulOscuro else AzulMedio
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (uiState.esAdmin) "Ingresar como Administrador"
                                       else "Ingresar con número de Teléfono",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // ── Tip de admin (solo modo admin) ────────────────────
                    if (uiState.esAdmin) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "🛡️ Acceso exclusivo para el administrador global de la plataforma",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}