package mx.edu.utng.cunasegura.presentation.networks

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.google.zxing.BarcodeFormat
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import com.journeyapps.barcodescanner.BarcodeEncoder

private val AzulCunaSegura @Composable get() = MaterialTheme.colorScheme.primary
private val RojoSOS @Composable get() = MaterialTheme.colorScheme.error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworksScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: NetworksViewModel = viewModel(factory = NetworksViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()
    
    val scrollState = rememberScrollState()
    var nombreNuevaRed by remember { mutableStateOf("") }
    var tipoNuevaRed by remember { mutableStateOf("Abierta") }
    var radioNuevaRed by remember { mutableStateOf("200") }
    var mostrarDialogoCrear by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    
    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                viewModel.unirsePorQr(result.contents)
            }
        }
    )

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                    if (loc != null) {
                        viewModel.buscarRedesAbiertasCercanas(loc.latitude, loc.longitude)
                    }
                }
            } catch (e: SecurityException) {
                // Ignore
            }
        } else {
            Toast.makeText(context, "Permiso de ubicación denegado", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(uiState.mensaje) {
        if (uiState.mensaje != null) {
            Toast.makeText(context, uiState.mensaje, Toast.LENGTH_SHORT).show()
            viewModel.limpiarMensaje()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Redes Vecinales", color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally), color = AzulCunaSegura)
            }

            val red = uiState.redActual
            val hasRed = red != null && red.id != uiState.usuarioActual?.correo && red.id.isNotEmpty()
            if (hasRed && red != null) {
                // Pertenecer a una red existente
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(red.nombre, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = AzulCunaSegura)
                        }
                        
                        Text("Tipo de red: ${red.tipo}", fontSize = 14.sp, color = Color.DarkGray)
                        Text("Cobertura: ${red.radio.toInt()} metros", fontSize = 14.sp, color = Color.DarkGray)
                        if (red.tvId.isNotBlank()) {
                            Text("Smart TV Enlazada: ${red.tvId}", fontSize = 14.sp, color = Color.DarkGray)
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        
                        // Generar código QR para compartir
                        Text("Código QR para invitar vecinos:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            val qrBitmap = generarQrBitmap(red.id)
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "Código QR de la red",
                                    modifier = Modifier.size(200.dp)
                                )
                            } else {
                                Text("No se pudo generar el código QR", color = Color.Red)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Miembros de la red
                        Text("Miembros Conectados (${uiState.miembrosRed.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AzulCunaSegura)
                        uiState.miembrosRed.forEach { miembro ->
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(miembro.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(miembro.telefono, fontSize = 12.sp, color = Color.Gray)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (miembro.rol == "administrador") AzulCunaSegura.copy(alpha = 0.2f) else Color.LightGray)
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = miembro.rol.uppercase(),
                                        fontSize = 10.sp,
                                        color = if (miembro.rol == "administrador") AzulCunaSegura else Color.DarkGray,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { viewModel.salirDeRedActual() },
                            colors = ButtonDefaults.buttonColors(containerColor = RojoSOS),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().height(48.dp)
                        ) {
                            Text("Salir de la Red Vecinal", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                // No pertenece a ninguna red
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(Icons.Default.Radar, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(48.dp))
                        Text(
                            "Aún no estás en una red vecinal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Únete a una red cercana por GPS o escanea el código QR de un administrador para mantenerte a salvo.",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Botón de Escáner QR
                        Button(
                            onClick = {
                                val opts = ScanOptions()
                                opts.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                                opts.setPrompt("Apunta al código QR de la red vecinal")
                                opts.setBeepEnabled(true)
                                scanLauncher.launch(opts)
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AzulCunaSegura)
                        ) {
                            Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Escanear Código QR", fontWeight = FontWeight.Bold)
                        }

                        // Botón de búsqueda GPS
                        Button(
                            onClick = {
                                val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                                if (hasFine || hasCoarse) {
                                    try {
                                        fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                                            if (loc != null) {
                                                viewModel.buscarRedesAbiertasCercanas(loc.latitude, loc.longitude)
                                            }
                                        }
                                    } catch (e: SecurityException) {
                                        // Ignore
                                    }
                                } else {
                                    locationPermissionLauncher.launch(
                                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.Radar, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Buscar Redes Abiertas (GPS)", fontWeight = FontWeight.Bold)
                        }

                        // Botón para crear nueva red (para Administradores)
                        TextButton(onClick = { mostrarDialogoCrear = true }) {
                            Text("Crear Nueva Red Vecinal", fontWeight = FontWeight.Bold, color = AzulCunaSegura)
                        }
                    }
                }

                // Lista de redes cercanas encontradas por GPS
                if (uiState.redesCercanas.isNotEmpty()) {
                    Text("Redes Vecinales Abiertas Cercanas", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AzulCunaSegura)
                    uiState.redesCercanas.forEach { (cercana, dist) ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(cercana.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("A ${dist.toInt()} metros de distancia", fontSize = 12.sp, color = Color.Gray)
                                }
                                Button(
                                    onClick = { viewModel.unirseARedAbierta(cercana.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = AzulCunaSegura),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("Unirse")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Diálogo para crear red vecinal
    if (mostrarDialogoCrear) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoCrear = false },
            title = { Text("Crear Red Vecinal", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = nombreNuevaRed,
                        onValueChange = { nombreNuevaRed = it },
                        label = { Text("Nombre de la Colonia / Grupo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Text("Tipo de Red:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = tipoNuevaRed == "Abierta",
                            onClick = { tipoNuevaRed = "Abierta" }
                        )
                        Text("Abierta (GPS)")
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = tipoNuevaRed == "Cerrada",
                            onClick = { tipoNuevaRed = "Cerrada" }
                        )
                        Text("Cerrada (QR)")
                    }

                    OutlinedTextField(
                        value = radioNuevaRed,
                        onValueChange = { radioNuevaRed = it },
                        label = { Text("Radio de Cobertura (metros)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        if (hasFine) {
                            try {
                                fusedLocationClient.lastLocation.addOnSuccessListener { loc ->
                                    val lat = loc?.latitude ?: 0.0
                                    val lng = loc?.longitude ?: 0.0
                                    viewModel.crearRedVecinal(
                                        nombre = nombreNuevaRed,
                                        tipo = tipoNuevaRed,
                                        lat = lat,
                                        lng = lng,
                                        radio = radioNuevaRed.toDoubleOrNull() ?: 200.0
                                    )
                                    mostrarDialogoCrear = false
                                }
                            } catch (e: SecurityException) {
                                // Ignore
                            }
                        } else {
                            Toast.makeText(context, "Se necesita permiso de ubicación para establecer el centro de la red", Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzulCunaSegura)
                ) {
                    Text("Crear")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoCrear = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun generarQrBitmap(contenido: String): Bitmap? {
    return try {
        val encoder = BarcodeEncoder()
        encoder.encodeBitmap(contenido, BarcodeFormat.QR_CODE, 400, 400)
    } catch (e: Exception) {
        null
    }
}
