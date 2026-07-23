package mx.edu.utng.cunasegura.data.location

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mx.edu.utng.cunasegura.MainActivity
import mx.edu.utng.cunasegura.R
import mx.edu.utng.cunasegura.data.local.prefs.PreferencesManager

class LocationTrackerService : Service() {

    private val CHANNEL_ID = "LocationTrackerChannel"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        setupLocationCallback()
        setupAlertsLogListener()
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Cuna Segura Activa")
            .setContentText("Actualizando ubicación de la red vecinal")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                    1,
                    notification,
                    android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
            } else {
                startForeground(1, notification)
            }
        } catch (e: Exception) {
            Log.e("LocationTracker", "No se pudo iniciar Foreground Service (posible falta de permisos o restricción API 34): ${e.message}")
        }
        
        requestLocationUpdates()

        return START_STICKY
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Log.d("LocationTracker", "Ubicación actualizada: ${location.latitude}, ${location.longitude}")
                    updateLocationInFirebase(location.latitude, location.longitude)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5 * 60 * 1000L) // 5 minutes
            .setMinUpdateIntervalMillis(5 * 60 * 1000L)
            .build()
            
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun updateLocationInFirebase(lat: Double, lon: Double) {
        serviceScope.launch {
            val userId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
            if (userId != null) {
                val dbRef = FirebaseDatabase.getInstance().getReference("usuarios").child(userId)
                val updates = mapOf<String, Any>(
                    "latActual" to lat,
                    "lonActual" to lon
                )
                dbRef.updateChildren(updates)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        removeAlertsLogListener()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Canal de Rastreo de Ubicación",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private var alertsLogListener: com.google.firebase.database.ChildEventListener? = null
    private var currentUserNetworkId: String? = null
    private var networkIdListener: com.google.firebase.database.ValueEventListener? = null

    private fun setupAlertsLogListener() {
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser ?: return
        val currentUid = authUser.uid
        val database = FirebaseDatabase.getInstance()

        // 1. Escuchar el networkId del usuario actual
        networkIdListener = object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                currentUserNetworkId = snapshot.getValue(String::class.java)
                Log.d("LocationTracker", "Mi networkId actual es: $currentUserNetworkId")
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                Log.e("LocationTracker", "Error al escuchar networkId", error.toException())
            }
        }
        database.getReference("usuarios").child(currentUid).child("networkId")
            .addValueEventListener(networkIdListener!!)

        // 2. Escuchar nuevas alertas en alerts_log
        alertsLogListener = object : com.google.firebase.database.ChildEventListener {
            override fun onChildAdded(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {
                try {
                    val alertUid = snapshot.child("usuarioId").getValue(String::class.java) ?: ""
                    val alertNetworkId = snapshot.child("networkId").getValue(String::class.java) ?: ""
                    val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                    val nombreUsuario = snapshot.child("nombreUsuario").getValue(String::class.java) ?: "Vecino"
                    val nivel = snapshot.child("nivel").getValue(Int::class.java) ?: 1

                    // Validar:
                    // - No es el usuario actual
                    // - Es de la misma red vecinal
                    // - Es reciente (menos de 45 segundos)
                    val isRecent = Math.abs(System.currentTimeMillis() - timestamp) < 180000 // 3 minutos
                    val isSameNetwork = currentUserNetworkId.isNullOrEmpty() || alertNetworkId.isEmpty() || alertNetworkId == currentUserNetworkId || alertNetworkId == currentUid || currentUserNetworkId == alertUid
                    val isDifferentUser = alertUid.isNotEmpty() && alertUid != currentUid

                    Log.d("LocationTracker", "Alerta detectada: de=$nombreUsuario, network=$alertNetworkId, actualNetwork=$currentUserNetworkId, recent=$isRecent, different=$isDifferentUser")

                    if (isRecent && isSameNetwork && isDifferentUser) {
                        showEmergencyNotification(nombreUsuario, nivel)
                    }
                } catch (e: Exception) {
                    Log.e("LocationTracker", "Error al procesar alerta del alerts_log", e)
                }
            }

            override fun onChildChanged(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: com.google.firebase.database.DataSnapshot) {}
            override fun onChildMoved(snapshot: com.google.firebase.database.DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        }
        database.getReference("alerts_log").addChildEventListener(alertsLogListener!!)
    }

    private fun removeAlertsLogListener() {
        val database = FirebaseDatabase.getInstance()
        val authUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (authUser != null && networkIdListener != null) {
            database.getReference("usuarios").child(authUser.uid).child("networkId")
                .removeEventListener(networkIdListener!!)
        }
        if (alertsLogListener != null) {
            database.getReference("alerts_log").removeEventListener(alertsLogListener!!)
        }
    }

    private fun showEmergencyNotification(vecinoNombre: String, nivel: Int) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val channelId = "neighbor_alerts_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Alertas Vecinales Cuna Segura"
            val desc = "Alertas SOS en tiempo real de tus vecinos de la red"
            val channel = NotificationChannel(channelId, name, NotificationManager.IMPORTANCE_HIGH).apply {
                description = desc
                enableLights(true)
                lightColor = android.graphics.Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(100, 800, 200, 800, 200, 800, 200, 1000)
                setSound(
                    android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_ALARM),
                    android.media.AudioAttributes.Builder()
                        .setUsage(android.media.AudioAttributes.USAGE_ALARM)
                        .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🚨 ¡VECINO EN PELIGRO! 🚨")
            .setContentText("$vecinoNombre ha activado una alerta SOS de nivel $nivel!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("¡Atención! Tu vecino $vecinoNombre ha activado una alerta SOS (Nivel $nivel - Toques del reloj). Revisa el mapa para ver su ubicación."))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        notificationManager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
