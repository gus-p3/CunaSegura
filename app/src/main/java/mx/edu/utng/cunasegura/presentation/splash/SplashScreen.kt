package mx.edu.utng.cunasegura.presentation.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.di.AppModule

private val AzulOscuro @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
private val AzulMedio @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary

/**
 * Pantalla inicial de bienvenida (Splash Screen) con animación de pulso y verificación asíncrona de sesión.
 *
 * Flujo de redirección:
 * 1. Si no hay sesión activa en Firebase Auth -> [onNavigateToLogin].
 * 2. Si la cuenta está en estado `bloqueado` o `suspendido` -> Cierra sesión y va a [onNavigateToLogin].
 * 3. Si el usuario cuenta con rol `admin` -> [onNavigateToAdmin].
 * 4. Si el usuario cuenta con rol `usuario` -> [onNavigateToHome].
 *
 * @param onNavigateToHome Navega al flujo principal del vecino.
 * @param onNavigateToLogin Navega a la pantalla de login.
 * @param onNavigateToAdmin Navega al panel maestro de administración.
 */
@Composable
fun SplashScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val context = LocalContext.current

    // Animación de escala del logo
    val infiniteTransition = rememberInfiniteTransition(label = "SplashPulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LogoScale"
    )

    LaunchedEffect(Unit) {
        delay(1500) // Splash de 1.5 segundos

        // Check Firebase Auth session first
        val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        
        if (firebaseUser == null) {
            onNavigateToLogin()
        } else {
            try {
                val snapshot = com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("usuarios").child(firebaseUser.uid).get().await()
                val estado = snapshot.child("estado").getValue(String::class.java) ?: "activo"
                if (estado == "bloqueado" || estado == "suspendido") {
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    onNavigateToLogin()
                    return@LaunchedEffect
                }
            } catch (e: Exception) {
                // Ignore
            }

            // Obtenemos el usuario de Room que representa la sesión local activa
            val obtenerUsuarioActual = AppModule.provideObtenerUsuarioActualUseCase(context)
            val usuarioActual = obtenerUsuarioActual()
            if (usuarioActual != null && usuarioActual.rol == "admin") {
                onNavigateToAdmin()
            } else {
                // Si no hay sesión local o no es admin, por defecto va a Home
                onNavigateToHome()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(colors = listOf(AzulOscuro, AzulMedio))
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Logo animado
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.onPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Logo Cuna Segura",
                        tint = AzulOscuro,
                        modifier = Modifier.size(64.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "CUNA SEGURA",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 4.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "ALERTA CIUDADANA · DOLORES HIDALGO",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                fontSize = 11.sp,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Indicador de carga
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(3) { index ->
                    val dotScale by infiniteTransition.animateFloat(
                        initialValue = 0.6f,
                        targetValue = 1f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(600, delayMillis = index * 200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "Dot$index"
                    )
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .scale(dotScale)
                            .clip(CircleShape)
                            .background(androidx.compose.material3.MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f))
                    )
                }
            }
        }
    }
}