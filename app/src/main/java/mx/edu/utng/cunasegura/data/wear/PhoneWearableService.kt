package mx.edu.utng.cunasegura.data.wear

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.net.Uri as AndroidUri
import android.net.Uri
import android.os.Build
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import android.annotation.SuppressLint
import com.google.android.gms.location.LocationServices
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.MainActivity
import mx.edu.utng.cunasegura.data.local.entity.ConfiguracionToqueEntity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import androidx.core.app.NotificationCompat
import mx.edu.utng.cunasegura.R

class PhoneWearableService : WearableListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val TAG = "PhoneWearableService"
    private val CHANNEL_ID = "sos_alerts_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alertas SOS"
            val descriptionText = "Notificaciones de envío de mensajes de emergencia"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 200, 300, 400, 500, 400, 300, 200, 400)
                setLockscreenVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_MAX) // Prioridad máxima para Banner
            .setCategory(NotificationCompat.CATEGORY_ALARM) // Categoría de alarma/emergencia
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(100, 200, 300, 400, 500))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        Log.d(TAG, "Mensaje recibido desde Wear OS: ${messageEvent.path}")

        when (messageEvent.path) {
            "/cunasegura/sos/trigger" -> {
                val payload = String(messageEvent.data)
                serviceScope.launch { handleSosTrigger(payload) }
            }
            "/cunasegura/sos/cancel" -> {
                serviceScope.launch { handleSosCancel() }
            }
            "/cunasegura/config/update" -> {
                val payload = String(messageEvent.data)
                serviceScope.launch { handleConfigUpdate(payload) }
            }
            else -> {
                Log.w(TAG, "Ruta de mensaje desconocida: ${messageEvent.path}")
            }
        }
    }

    private suspend fun handleSosTrigger(payload: String) {
        // Payload format: "ACTION=ALARMA_TV|ADDRESS=Ubicación actual"
        Log.d(TAG, "Trigger SOS con payload: $payload")
        showNotification("Señal SOS recibida", "Procesando alerta desde el reloj...")
        
        val activarAlertaUseCase = AppModule.provideActivarAlertaUseCase(applicationContext)
        val obtenerContactosUseCase = AppModule.provideObtenerContactosUseCase(applicationContext)
        val partes = payload.split("|")
        var action = "SOS_DESCONOCIDO"
        var location = "Ubicación desconocida"
        
        for (part in partes) {
            if (part.startsWith("ACTION=")) {
                action = part.removePrefix("ACTION=")
            } else if (part.startsWith("ADDRESS=")) {
                location = part.removePrefix("ADDRESS=")
            }
        }
        
        // Obtener ubicación actual para bitácora de alertas_log
        var currentLat = 0.0
        var currentLon = 0.0
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
                val loc = fusedLocationClient.lastLocation.await()
                if (loc != null) {
                    currentLat = loc.latitude
                    currentLon = loc.longitude
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo ubicación para bitácora", e)
            }
        }
        
        // Guardar en alerts_log
        logAlertToFirebase(action, currentLat, currentLon)
        
        when (action) {
            "MENSAJE_SMS" -> {
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
                    Log.e(TAG, "Permiso SEND_SMS no otorgado")
                    showNotification("Error SOS", "Permiso de SMS no otorgado en el teléfono")
                } else {
                    val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                    Log.d(TAG, "Usuario actual en Firebase: ${firebaseUser?.uid ?: "NULL"}")

                    val contactos = obtenerContactosUseCase(usuarioId = 1).first()
                    Log.d(TAG, "Contactos recuperados: ${contactos.size}")

                    val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        applicationContext.getSystemService(SmsManager::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        SmsManager.getDefault()
                    }
                    val mensaje = "¡Alerta SOS de Cuna Segura! Necesito ayuda. Ubicación: $location"
                    
                    if (contactos.isEmpty()) {
                        Log.w(TAG, "No hay contactos registrados para enviar SMS de ayuda")
                        showNotification("SOS Fallido", "No tienes contactos de emergencia registrados")
                    }

                    contactos.forEach { contacto ->
                        try {
                            val parts = smsManager.divideMessage(mensaje)
                            smsManager.sendMultipartTextMessage(contacto.telefono, null, parts, null, null)
                            Log.i(TAG, "SMS enviado a ${contacto.nombre} (${contacto.telefono})")
                            showNotification("SMS Enviado", "Alerta enviada a ${contacto.nombre} (${contacto.telefono})")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error enviando SMS a ${contacto.telefono}", e)
                            showNotification("Error de envío", "No se pudo enviar el SMS a ${contacto.nombre}")
                        }
                    }
                }
            }
            "UBICACION_TIEMPO_REAL" -> {
                var lat = 0.0
                var lon = 0.0
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
                        val location = fusedLocationClient.lastLocation.await()
                        if (location != null) {
                            lat = location.latitude
                            lon = location.longitude
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error obteniendo ubicación", e)
                    }
                } else {
                    Log.w(TAG, "No hay permisos de ubicación para SOS")
                }

                var usuarioId = 1
                var nombreUsuario = "Vecino"
                try {
                    val dbInstance = mx.edu.utng.cunasegura.data.local.db.AppDatabase.getInstance(applicationContext)
                    val currentUser = dbInstance.usuarioDao().obtenerUsuarioActual()
                    if (currentUser != null) {
                        usuarioId = currentUser.id
                        nombreUsuario = currentUser.nombre
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching user info", e)
                }

                val alertaId = activarAlertaUseCase(usuarioId = usuarioId, nombreUsuario = nombreUsuario, latitud = lat, longitud = lon)
                val intent = Intent(applicationContext, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("EXTRA_ALERTA_ID", alertaId.toInt())
                }
                applicationContext.startActivity(intent)
            }
            "ALARMA_TV" -> {
                Log.i(TAG, "Activando Alerta de TV...")
                showNotification("Alerta Vecinal", "Enviando alerta a las Smart TVs...")
                
                var lat = 0.0
                var lon = 0.0
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    try {
                        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(applicationContext)
                        val location = fusedLocationClient.lastLocation.await()
                        if (location != null) {
                            lat = location.latitude
                            lon = location.longitude
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error obteniendo ubicación para TV", e)
                    }
                }

                // 1. Enviar vía MQTT a las TVs (Rápido / Tiempo Real)
                mx.edu.utng.cunasegura.mqtt.MqttPublisher.publishAlertaTv(lat, lon)
                
                var tvUsuarioId = 1
                var tvNombreUsuario = "Vecino"
                try {
                    val dbInstance = mx.edu.utng.cunasegura.data.local.db.AppDatabase.getInstance(applicationContext)
                    val currentUser = dbInstance.usuarioDao().obtenerUsuarioActual()
                    if (currentUser != null) {
                        tvUsuarioId = currentUser.id
                        tvNombreUsuario = currentUser.nombre
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error fetching user info for TV alert", e)
                }

                val alertaId = activarAlertaUseCase(usuarioId = tvUsuarioId, nombreUsuario = tvNombreUsuario, latitud = lat, longitud = lon)
                Log.i(TAG, "Alerta TV guardada en Firebase con ID: $alertaId")
            }
            "LLAMAR_911" -> {
                val hasCallPermission = ContextCompat.checkSelfPermission(this, android.Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED
                val intent = if (hasCallPermission) {
                    Intent(Intent.ACTION_CALL, Uri.parse("tel:911"))
                } else {
                    Intent(Intent.ACTION_DIAL, Uri.parse("tel:911"))
                }.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                applicationContext.startActivity(intent)
            }
        }
    }

    private suspend fun handleSosCancel() {
        val cancelarAlertaUseCase = AppModule.provideCancelarAlertaUseCase(applicationContext)
        cancelarAlertaUseCase(alertaId = 1)
    }

    private suspend fun handleConfigUpdate(payload: String) {
        val db = mx.edu.utng.cunasegura.data.local.db.AppDatabase.getInstance(applicationContext)
        val toqueDao = db.configuracionToqueDao()

        val nuevasAcciones = payload.split("|")
        if (nuevasAcciones.size != 4) return

        nuevasAcciones.forEachIndexed { index, accion ->
            toqueDao.insertarOActualizar(
                ConfiguracionToqueEntity(
                    id = 0,
                    usuarioId = 1, 
                    cantidadToques = index + 1, 
                    tipoAccion = accion
                )
            )
        }
    }

    private suspend fun logAlertToFirebase(action: String, lat: Double, lon: Double) {
        try {
            val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            val uid = firebaseUser?.uid ?: "unknown_uid"
            val nombreUsuario = firebaseUser?.displayName ?: "Vecino"
            
            // Mapeo del nivel (1 a 4 toques)
            val nivel = when (action) {
                "MENSAJE_SMS" -> 1
                "UBICACION_TIEMPO_REAL" -> 2
                "ALARMA_TV" -> 3
                "LLAMAR_911" -> 4
                else -> 1
            }

            // Obtener el networkId del usuario
            var networkId = uid
            try {
                val userSnap = com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("usuarios").child(uid).get().await()
                if (userSnap.exists()) {
                    networkId = userSnap.child("networkId").getValue(String::class.java) ?: uid
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener networkId del usuario", e)
            }

            val database = com.google.firebase.database.FirebaseDatabase.getInstance()
            val ref = database.getReference("alerts_log").push()
            val alertId = ref.key ?: System.currentTimeMillis().toString()

            val alertData = mapOf(
                "id" to alertId,
                "usuarioId" to uid,
                "nombreUsuario" to nombreUsuario,
                "latitud" to lat,
                "longitud" to lon,
                "tipo" to "Real",
                "timestamp" to System.currentTimeMillis(),
                "nivel" to nivel,
                "networkId" to networkId
            )
            
            ref.setValue(alertData).await()
            Log.i(TAG, "Alerta registrada en alerts_log: $alertId")
        } catch (e: Exception) {
            Log.e(TAG, "Error registrando alerta en alerts_log de Firebase", e)
        }
    }
}
