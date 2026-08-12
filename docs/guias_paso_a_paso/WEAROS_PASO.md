# Guía Paso a Paso: Construyendo el Módulo Wear OS de Cuna Segura

Esta guía documenta y desglosa paso a paso la arquitectura, configuración y construcción completa del módulo **Wear OS (Smartwatch)** de **Cuna Segura**, explicando las decisiones técnicas, patrones de diseño y bloques de código esenciales para un proyecto profesional en **Kotlin** y **Compose for Wear OS**.

---

## Objetivo de Esta Guía

Al estudiar y seguir esta guía, comprenderás:

1. **Arquitectura y Configuración**: Cómo preparar un módulo de Wear OS (`:cunasegurawear`) para pantallas circulares independientes o vinculadas.
2. **Persistencia Local (Room)**: Estructurar una base de datos local ligera para almacenar offline las configuraciones de acciones por patrón de toques (de 1 a 4 toques).
3. **Comunicación Local (Wear Data Layer)**: Implementar una comunicación de baja latencia con el smartphone por **Bluetooth Low Energy (BLE)** mediante la API de `Wearable MessageClient`.
4. **Máquina de Estados de Alertas**: Administrar el ciclo de vida del SOS en un ViewModel (COUNTDOWN, ACTIVE, LIFE_CHECK, CANCELLED, IDLE) con temporizadores adaptativos y resiliencia local.
5. **UI Circular Háptica**: Diseñar interfaces amigables para pantallas redondas con micro-animaciones en Compose y retroalimentación táctil intensiva (Vibraciones).

---

## FASE 1: Configuración Inicial del Módulo Wear OS

### Paso 1.1: Configurar dependencias en `cunasegurawear/build.gradle.kts`

El módulo Wear OS requiere dependencias específicas de Compose para Wear OS, navegación optimizada para relojes, Room local y los servicios `play-services-wearable`.

> 📋 **INSTRUCCIÓN:** Declara las librerías en `cunasegurawear/build.gradle.kts`:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.legacy.kapt) // KSP/KAPT para compilación de anotaciones de Room
}

android {
    namespace = "mx.edu.utng.cunasegurawear"
    compileSdk = 36

    defaultConfig {
        applicationId = "mx.edu.utng.cunasegura" // Compartido con el móvil para Wear Data Layer
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    // Jetpack Compose para pantallas circulares y wearables
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.foundation)
    implementation(libs.androidx.wear.compose.navigation)
    
    // Ciclo de vida y almacenamiento de datos ligero
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.health.services.client)
    implementation(libs.androidx.core.splashscreen)
    
    // Conectividad Bluetooth y Wearable API
    implementation("com.google.android.gms:play-services-wearable:18.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
    
    // SQLite local (Room)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

configurations.all {
    resolutionStrategy {
        // Fuerza metadato correcto de Kotlin para evitar conflictos de compiladores cruzados
        force("org.jetbrains.kotlin:kotlin-metadata-jvm:2.4.0")
    }
}
```

---

### Paso 1.2: Manifiesto Wear OS (`cunasegurawear/src/main/AndroidManifest.xml`)

Es fundamental declarar los permisos de geolocalización, bluetooth (para escanear y conectar con el teléfono) y marcar la aplicación como ejecutable en wearables.

> 📋 **INSTRUCCIÓN:** Configura el manifiesto de la aplicación del smartwatch:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <!-- Requiere hardware Wear OS para instalación en smartwatch -->
    <uses-feature android:name="android.hardware.type.watch" />

    <!-- Permisos de Ubicación local si el reloj opera de manera autónoma (Standalone) -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />

    <!-- Permisos de Bluetooth para emparejamiento local con el Smartphone (Android 12+) -->
    <uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.DeviceDefault">

        <meta-data
            android:name="com.google.android.wearable.standalone"
            android:value="true" /> <!-- Declara que la app puede funcionar sin el teléfono instalado -->

        <activity
            android:name=".WatchActivity"
            android:exported="true"
            android:theme="@android:style/Theme.DeviceDefault">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

---

## FASE 2: Capa de Datos Local (SQLite Room)

El smartwatch almacena de forma persistente la configuración de toques de manera local. Si se pierde la conexión de red, la máquina de estados sabrá qué acción realizar según el número de toques detectado.

### Paso 2.1: Modelo de Configuración de Toques (`data/db/TouchConfig.kt`)

> 📋 **INSTRUCCIÓN:** Crea la clase de datos de la entidad Room:

```kotlin
package mx.edu.utng.cunasegurawear.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Tabla SQLite local que asocia un número de toques (1 a 4) con una acción de pánico.
 */
@Entity(tableName = "touch_config")
data class TouchConfig(
    @PrimaryKey
    val tapNumber: Int,          // Número de toques (clave primaria: 1, 2, 3 o 4)
    val actionName: String,      // Nombre técnico de la acción (p. ej., "ALARMA_TV", "MENSAJE_SMS")
    val actionLabel: String      // Etiqueta legible para el usuario (p. ej., "Sonar Smart TV")
)
```

---

### Paso 2.2: Interfaz de Acceso a Datos (`data/db/TouchConfigDao.kt`)

El DAO expone flujos reactivos y llamadas síncronas/asíncronas al almacenamiento local.

> 📋 **INSTRUCCIÓN:** Crea la interfaz DAO:

```kotlin
package mx.edu.utng.cunasegurawear.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TouchConfigDao {
    // Emite flujos reactivos para actualizar dinámicamente la pantalla de ajustes
    @Query("SELECT * FROM touch_config ORDER BY tapNumber ASC")
    fun getAllConfigs(): Flow<List<TouchConfig>>

    // Consulta puntual no reactiva requerida para construir payloads de comunicación
    @Query("SELECT * FROM touch_config ORDER BY tapNumber ASC")
    suspend fun getAllConfigsNow(): List<TouchConfig>

    @Query("SELECT * FROM touch_config WHERE tapNumber = :taps LIMIT 1")
    suspend fun getConfigForTaps(taps: Int): TouchConfig?

    @Query("SELECT * FROM touch_config WHERE actionName = :name LIMIT 1")
    suspend fun getConfigForAction(name: String): TouchConfig?

    // Inserta o actualiza un registro reemplazando el conflicto de ID (tapNumber)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: TouchConfig)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigs(configs: List<TouchConfig>)

    @Query("DELETE FROM touch_config WHERE tapNumber = :taps")
    suspend fun deleteConfigForTaps(taps: Int)

    @Query("DELETE FROM touch_config")
    suspend fun deleteAllConfigs()
}
```

---

### Paso 2.3: Singleton de la Base de Datos (`data/db/AppDatabase.kt`)

> 📋 **INSTRUCCIÓN:** Instancia Room Database en el Wearable:

```kotlin
package mx.edu.utng.cunasegurawear.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TouchConfig::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun touchConfigDao(): TouchConfigDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Previene que múltiples hilos creen instancias duplicadas de la base de datos
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cuna_segura_wear_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
```

---

## FASE 3: Comunicación e Integración IoT (Wear Data Layer)

Esta capa implementa el canal de enlace local vía Bluetooth con el Smartphone utilizando los Google Play Services.

### Paso 3.1: Cliente de Mensajería y Sincronización (`data/wear/WearMessageClient.kt`)

`WearMessageClient` oye las actualizaciones que el teléfono envía (como la configuración global de toques hecha en el móvil) y despacha las alarmas de pánico iniciadas en el reloj.

> 📋 **INSTRUCCIÓN:** Crea el gestor de mensajes BLE:

```kotlin
package mx.edu.utng.cunasegurawear.data.wear

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.Tasks
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext

class WearMessageClient(private val context: Context) : MessageClient.OnMessageReceivedListener {

    private val TAG = "WearMessageClient"
    // Recupera la instancia de mensajería nativa de Google Play Services
    private val messageClient = Wearable.getMessageClient(context)
    private val nodeClient = Wearable.getNodeClient(context)

    // Flujo asíncrono para emitir configuraciones recibidas del teléfono
    private val _incomingConfig = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val incomingConfig: SharedFlow<String> = _incomingConfig.asSharedFlow()

    // Flujo para recibir la ventana del Check de Vida en milisegundos
    private val _incomingCheckVida = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val incomingCheckVida: SharedFlow<Long> = _incomingCheckVida.asSharedFlow()

    init {
        // Registra este objeto como listener activo del canal Bluetooth
        messageClient.addListener(this)
        Log.d(TAG, "WearMessageClient inicializado y escuchando mensajes por Bluetooth")
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        Log.d(TAG, "Mensaje de Bluetooth recibido: ${messageEvent.path}")
        
        // Intercepta configuraciones del teléfono y las pasa al flujo de escucha
        if (messageEvent.path == "/cunasegura/config/update" || messageEvent.path == "/cunasegura/config/sync") {
            val payload = String(messageEvent.data)
            _incomingConfig.tryEmit(payload)
            Log.d(TAG, "Configuración extraída: $payload")
        } else if (messageEvent.path == "/cunasegura/config/checkVida") {
            val payload = String(messageEvent.data)
            val ms = payload.toLongOrNull() ?: 120_000L
            _incomingCheckVida.tryEmit(ms)
            Log.d(TAG, "CheckVida dinámico recibido: $ms ms")
        }
    }

    /**
     * Envía la señal de activación de pánico SOS al teléfono emparejado.
     */
    suspend fun sendAlert(address: String, actionName: String): Result<Int> = withContext(Dispatchers.IO) {
        val payload = "ACTION=$actionName|ADDRESS=$address"
        Log.d(TAG, "📡 [SOS-TRIGGER] Enviando al teléfono → $payload")
        
        try {
            // Obtiene todos los nodos (teléfonos) conectados por Bluetooth a este reloj
            val nodes = Tasks.await(nodeClient.connectedNodes)
            for (node in nodes) {
                // Envía el mensaje por el canal seguro /cunasegura/sos/trigger
                messageClient.sendMessage(node.id, "/cunasegura/sos/trigger", payload.toByteArray())
            }
            if (nodes.isNotEmpty()) Result.success(3) else Result.failure(Exception("No hay teléfono conectado"))
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando alerta SOS", e)
            Result.failure(e)
        }
    }

    /**
     * Envía la señal de cancelación de la alarma al teléfono.
     */
    suspend fun cancelAlert(): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d(TAG, "📡 [SOS-CANCEL] Enviando cancelación")
        try {
            val nodes = Tasks.await(nodeClient.connectedNodes)
            for (node in nodes) {
                messageClient.sendMessage(node.id, "/cunasegura/sos/cancel", "CANCEL".toByteArray())
            }
            if (nodes.isNotEmpty()) Result.success(Unit) else Result.failure(Exception("No hay teléfono conectado"))
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando cancelación", e)
            Result.failure(e)
        }
    }

    /**
     * Envía la sincronización de toques local del reloj hacia el teléfono móvil.
     */
    suspend fun sendConfigUpdate(configPayload: String): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d(TAG, "📡 [CONFIG-UPDATE] Reloj → Teléfono: $configPayload")
        try {
            val nodes = Tasks.await(nodeClient.connectedNodes)
            for (node in nodes) {
                messageClient.sendMessage(node.id, "/cunasegura/config/update", configPayload.toByteArray())
            }
            if (nodes.isNotEmpty()) Result.success(Unit) else Result.failure(Exception("No hay teléfono conectado"))
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando configuración", e)
            Result.failure(e)
        }
    }

    /**
     * Solicita al teléfono móvil que envíe su última configuración de toques registrada.
     */
    suspend fun sendSyncRequest(): Result<Unit> = withContext(Dispatchers.IO) {
        Log.d(TAG, "📡 [CONFIG-SYNC] Solicitando configuración de toques al Teléfono...")
        try {
            val nodes = Tasks.await(nodeClient.connectedNodes)
            for (node in nodes) {
                messageClient.sendMessage(node.id, "/cunasegura/config/sync_request", "SYNC".toByteArray())
            }
            if (nodes.isNotEmpty()) Result.success(Unit) else Result.failure(Exception("No hay teléfono conectado"))
        } catch (e: Exception) {
            Log.e(TAG, "Error enviando solicitud de sincronización", e)
            Result.failure(e)
        }
    }
}
```

---

## FASE 4: Máquina de Estados y Lógica del Negocio (ViewModel)

La lógica central del smartwatch no es secuencial simple; se rige por una **Máquina de Estados Finita** que filtra las falsas alarmas y recupera la localización en segundo plano.

### Paso 4.1: Repositorio e Interfaces Auxiliares

> 📋 **INSTRUCCIÓN:** Crea las interfaces y modelos de negocio en `cunasegurawear/src/main/java/mx/edu/utng/cunasegurawear/domain/`:

#### `SosAction.kt` (Enum de Acciones Disponibles)
```kotlin
package mx.edu.utng.cunasegurawear.domain.model

enum class SosAction(val nameKey: String, val label: String) {
    MENSAJE_SMS("MENSAJE_SMS", "Enviar SMS"),
    UBICACION_TIEMPO_REAL("UBICACION_TIEMPO_REAL", "Compartir GPS"),
    ALARMA_TV("ALARMA_TV", "Sonar Smart TV"),
    LLAMAR_911("LLAMAR_911", "Llamar al 911")
}
```

#### `AlertPhase.kt` (Fases de la Alerta)
```kotlin
package mx.edu.utng.cunasegurawear.domain.model

enum class AlertPhase {
    IDLE,         // Reposo total
    COUNTDOWN,    // Cuenta regresiva de 5s para cancelar
    ACTIVE,       // SOS enviado, transmitiendo ubicación
    LIFE_CHECK,   // Preguntando "¿Estás bien?" (Life Check)
    CANCELLED     // Cancelada por el usuario
}
```

#### `AlertState.kt` (Estado Completo de UI)
```kotlin
package mx.edu.utng.cunasegurawear.domain.model

import mx.edu.utng.cunasegurawear.data.db.TouchConfig

data class AlertState(
    val phase: AlertPhase = AlertPhase.IDLE,
    val countdownSeconds: Int = 5,
    val activeActionLabel: String = "",
    val activeActionName: String = "",
    val touchConfigs: List<TouchConfig> = emptyList(),
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val gpsAddress: String = "",
    val contactsNotified: Int = 0
)
```

---

### Paso 4.2: Lógica del ViewModel (`presentation/viewmodel/WatchViewModel.kt`)

Este componente coordina el timer, el geocodificador reverso, la escucha del Wear Data Layer y el mecanismo de auto-curación (*Self-Healing*) de la base de datos Room.

> 📋 **INSTRUCCIÓN:** Copia el archivo `WatchViewModel.kt`:

```kotlin
package mx.edu.utng.cunasegurawear.presentation.viewmodel

import android.content.Context
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Locale
import mx.edu.utng.cunasegurawear.data.db.TouchConfig
import mx.edu.utng.cunasegurawear.data.db.TouchConfigDao
import mx.edu.utng.cunasegurawear.data.location.WatchLocationTracker
import mx.edu.utng.cunasegurawear.domain.model.AlertPhase
import mx.edu.utng.cunasegurawear.domain.model.AlertState
import mx.edu.utng.cunasegurawear.domain.model.SosAction
import mx.edu.utng.cunasegurawear.data.wear.WearMessageClient

class WatchViewModel(
    private val messageClient: WearMessageClient,
    private val touchConfigDao: TouchConfigDao,
    private val locationTracker: WatchLocationTracker,
    private val context: Context
) : ViewModel() {
    private val TAG = "WatchViewModel"
    private val _state = MutableStateFlow(AlertState())
    val state: StateFlow<AlertState> = _state.asStateFlow()
    
    private var countdownJob: Job? = null
    private var lifeCheckJob: Job? = null
    private var locationJob: Job? = null
    private val _checkVidaMs = MutableStateFlow(120_000L) // Por defecto: 2 minutos

    init {
        // Observa la base de datos Room y aplica Self-Healing si faltan slots configurados
        viewModelScope.launch {
            touchConfigDao.getAllConfigs().collect { configs ->
                val needsSelfHealing = configs.size < 4
                if (needsSelfHealing) {
                    viewModelScope.launch(Dispatchers.IO) {
                        touchConfigDao.insertConfigs(
                            listOf(
                                TouchConfig(1, "MENSAJE_SMS", "Enviar SMS"),
                                TouchConfig(2, "UBICACION_TIEMPO_REAL", "Compartir GPS"),
                                TouchConfig(3, "ALARMA_TV", "Sonar Smart TV"),
                                TouchConfig(4, "LLAMAR_911", "Llamar al 911")
                            )
                        )
                    }
                } else {
                    _state.update { it.copy(touchConfigs = configs) }
                }
            }
        }

        // Sincroniza configuraciones entrantes desde el teléfono móvil
        viewModelScope.launch {
            messageClient.incomingConfig.collect { payload ->
                Log.i(TAG, "📥 [CONFIG-SYNC] Configuración recibida: $payload")
                applyIncomingConfig(payload)
            }
        }
        
        viewModelScope.launch {
            messageClient.incomingCheckVida.collect { ms ->
                Log.i(TAG, "📥 [CONFIG-SYNC] Ventana CheckVida: $ms ms")
                _checkVidaMs.value = ms
            }
        }
        
        // Pide sincronización inicial al arrancar
        viewModelScope.launch {
            messageClient.sendSyncRequest()
        }
    }

    private fun applyIncomingConfig(payload: String) {
        if (payload.isBlank() || payload == "NO_CONFIG") return
        val parts = payload.split("|")
        if (parts.size < 4) return
        
        viewModelScope.launch(Dispatchers.IO) {
            val newConfigs = parts.mapIndexedNotNull { index, actionName ->
                val tapNumber = index + 1
                val action = SosAction.values().firstOrNull { it.name == actionName }
                if (action == null) null 
                else TouchConfig(tapNumber = tapNumber, actionName = action.name, actionLabel = action.label)
            }
            if (newConfigs.size == 4) {
                touchConfigDao.insertConfigs(newConfigs)
            }
        }
    }

    /**
     * Simula o dispara el número de toques detectado.
     */
    fun onSimulateTaps(taps: Int) {
        countdownJob?.cancel() // Cancela cualquier temporizador previo activo
        
        countdownJob = viewModelScope.launch {
            val config = _state.value.touchConfigs.find { it.tapNumber == taps }
            val actionLabel = config?.actionLabel ?: "SOS General"
            val actionName = config?.actionName ?: "SOS_GENERAL"

            // 1. Pone la UI en fase de Cuenta Regresiva de 5 segundos
            _state.update {
                it.copy(
                    phase = AlertPhase.COUNTDOWN,
                    countdownSeconds = 5,
                    activeActionLabel = actionLabel,
                    activeActionName = actionName
                )
            }
            
            // 2. Reduce los segundos en intervalos de 1s
            for (i in 4 downTo 0) {
                delay(1000L)
                _state.update { it.copy(countdownSeconds = i) }
            }
            
            // 3. Al completarse la cuenta regresiva sin cancelación, dispara la alerta
            if (_state.value.phase == AlertPhase.COUNTDOWN) {
                val address = _state.value.gpsAddress.ifBlank { "Ubicación actual" }
                val result = messageClient.sendAlert(address, actionName)
                
                result.onSuccess {
                    _state.update { it.copy(phase = AlertPhase.ACTIVE, isGpsActive = true) }
                    startLifeCheckTimer() // Inicializa cronómetro de revisión de vida
                    startLocationTracking() // Inicia el rastreo dinámico del GPS
                }
                result.onFailure {
                    _state.update { it.copy(phase = AlertPhase.IDLE) }
                }
            }
        }
    }

    private fun startLifeCheckTimer() {
        lifeCheckJob = viewModelScope.launch {
            delay(_checkVidaMs.value)
            if (_state.value.phase == AlertPhase.ACTIVE) {
                // Al expirar la ventana sin respuesta, entra en fase de alerta semi-bloqueante
                _state.update { it.copy(phase = AlertPhase.LIFE_CHECK) }
            }
        }
    }

    fun onLifeCheckYes() {
        lifeCheckJob?.cancel()
        stopLocationTracking()
        viewModelScope.launch {
            messageClient.cancelAlert()
            _state.update { it.copy(phase = AlertPhase.CANCELLED) }
            delay(2000L)
            _state.update { it.copy(phase = AlertPhase.IDLE) }
        }
    }

    fun onLifeCheckNo() {
        _state.update { it.copy(phase = AlertPhase.ACTIVE) }
        startLifeCheckTimer() // Reinicia la ventana de chequeo
    }

    fun onCancelCountdown() {
        countdownJob?.cancel()
        _state.update { it.copy(phase = AlertPhase.IDLE) }
    }

    private fun startLocationTracking() {
        locationTracker.startTracking()
        locationJob = viewModelScope.launch {
            locationTracker.locationFlow.collect { location ->
                location?.let { loc ->
                    _state.update { it.copy(latitude = loc.latitude, longitude = loc.longitude) }
                    
                    // Ejecuta geocodificación reversa asíncrona en el hilo IO
                    viewModelScope.launch(Dispatchers.IO) {
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            val addresses = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                            val finalAddress = addresses?.firstOrNull()?.getAddressLine(0) 
                                ?: "Lat: ${loc.latitude}, Lon: ${loc.longitude}"
                            
                            // Envía la ubicación refinada al teléfono por Bluetooth
                            messageClient.sendAlert(finalAddress, _state.value.activeActionName)
                            _state.update { it.copy(gpsAddress = finalAddress) }
                        } catch (e: Exception) {
                            val fallback = "Lat: ${loc.latitude}, Lon: ${loc.longitude}"
                            messageClient.sendAlert(fallback, _state.value.activeActionName)
                        }
                    }
                }
            }
        }
    }

    private fun stopLocationTracking() {
        locationTracker.stopTracking()
        locationJob?.cancel()
    }
}
```

---

## FASE 5: Interfaz de Usuario Declarativa y Háptica

Compose para Wear OS utiliza componentes simplificados diseñados para ajustarse a pantallas circulares.

### Paso 5.1: Pantalla de Cuenta Regresiva (`presentation/screens/CountdownScreen.kt`)

Cuenta con un anillo circular que se drena desde 100% a 0% indicando visualmente el buffer de cancelación de la falsa alarma.

> 📋 **INSTRUCCIÓN:** Crea el archivo `CountdownScreen.kt`:

```kotlin
package mx.edu.utng.cunasegurawear.presentation.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.CircularProgressIndicator
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text

@Composable
fun CountdownScreen(
    seconds: Int,
    activeActionLabel: String,
    onCancel: () -> Unit
) {
    // Animaciones fluidas para transiciones de números y barra de progreso
    val animatedSeconds by animateIntAsState(targetValue = seconds, label = "secondsAnim")
    val progressTarget = if (seconds == 0) 0f else seconds / 5f
    val animatedProgress by animateFloatAsState(targetValue = progressTarget, label = "progressAnim")

    val primaryColor = MaterialTheme.colors.primary
    val errorColor = MaterialTheme.colors.error
    val secondaryVariantColor = MaterialTheme.colors.secondaryVariant
    val onBackgroundColor = MaterialTheme.colors.onBackground
    val onSecondaryColor = MaterialTheme.colors.onSecondary

    Scaffold {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Text(
                "ALERTA EN PROCESO",
                fontSize = 10.sp,
                color = primaryColor,
                fontWeight = FontWeight.SemiBold
            )
            if (activeActionLabel.isNotEmpty()) {
                Text(
                    activeActionLabel.uppercase(),
                    fontSize = 11.sp,
                    color = onBackgroundColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(90.dp)
                    .padding(vertical = 4.dp)
            ) {
                // Indicador circular de cuenta regresiva
                CircularProgressIndicator(
                    progress = animatedProgress,
                    modifier = Modifier.fillMaxSize(),
                    startAngle = 270f,
                    indicatorColor = errorColor, // Color rojo de advertencia
                    trackColor = onBackgroundColor.copy(alpha = 0.15f),
                    strokeWidth = 4.dp
                )
                Text(
                    text = "${animatedSeconds}s",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = onBackgroundColor
                )
            }

            // Botón grande y holgado de cancelación (Target size de 48dp)
            Button(
                onClick = onCancel,
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = secondaryVariantColor,
                    contentColor = onSecondaryColor
                ),
                modifier = Modifier.size(width = 110.dp, height = 36.dp)
            ) {
                Text("CANCELAR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
```

---

### Paso 5.2: Pantalla de Verificación de Vida (`presentation/screens/LifeCheckScreen.kt`)

Esta pantalla genera una vibración háptica fuerte al iniciar (`LongPress`) e implementa botones grandes sobre una capa difuminada de Compose.

> 📋 **INSTRUCCIÓN:** Crea el archivo `LifeCheckScreen.kt`:

```kotlin
package mx.edu.utng.cunasegurawear.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text

@Composable
fun LifeCheckScreen(
    onYes: () -> Unit,
    onNo: () -> Unit
) {
    // Captura el motor de vibración háptico del smartwatch
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        // Ejecuta una vibración persistente larga para alertar táctilmente al usuario
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val primaryColor = MaterialTheme.colors.primary
    val onPrimaryColor = MaterialTheme.colors.onPrimary
    val errorColor = MaterialTheme.colors.error
    val onErrorColor = MaterialTheme.colors.onError
    val onBackgroundColor = MaterialTheme.colors.onBackground

    Scaffold {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(onBackgroundColor.copy(alpha = 0.08f)) // Fondo escarchado estilo Glassmorphism
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "CHECK DE VIDA",
                    fontSize = 11.sp,
                    color = onBackgroundColor.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "¿ESTÁS BIEN?",
                    color = errorColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón "SÍ" (Cian) - Desactiva la alerta y notifica falsedad
                Button(
                    onClick = onYes,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = primaryColor,
                        contentColor = onPrimaryColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .size(height = 42.dp, width = 0.dp)
                ) {
                    Text("SÍ", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }

                // Botón "NO" (Rojo) - Continúa transmitiendo geolocalización
                Button(
                    onClick = onNo,
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = errorColor,
                        contentColor = onErrorColor
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .size(height = 42.dp, width = 0.dp)
                ) {
                    Text("NO", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}
```

---

## FASE 6: Inicialización e Hilo Principal (`WatchActivity.kt`)

La actividad principal (`WatchActivity`) inicializa los flujos, solicita dinámicamente permisos en tiempo de ejecución y monta la navegación.

> 📋 **INSTRUCCIÓN:** Copia el archivo `WatchActivity.kt`:

```kotlin
package mx.edu.utng.cunasegurawear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import mx.edu.utng.cunasegurawear.presentation.navigation.WatchNavHost
import mx.edu.utng.cunasegurawear.presentation.theme.WatchTheme
import mx.edu.utng.cunasegurawear.presentation.viewmodel.WatchViewModel
import mx.edu.utng.cunasegurawear.presentation.viewmodel.WatchViewModelFactory

class WatchActivity : ComponentActivity() {
    
    // Inyecta el ViewModel mediante delegación manual con su respectivo Factory
    private val viewModel: WatchViewModel by viewModels {
        WatchViewModelFactory(applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Lanzador de permisos múltiples para Bluetooth y Geolocalización
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { _ -> }
        
        val permissions = mutableListOf(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        )

        // Agrega permisos bluetooth requeridos en Android 12 o superior
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissions.add(android.Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(android.Manifest.permission.BLUETOOTH_CONNECT)
        }
        
        // Lanza la solicitud asíncrona al iniciar
        requestPermissionLauncher.launch(permissions.toTypedArray())

        setContent {
            WatchTheme {
                // Vincula la UI declarativa con el host de navegación y el ViewModel
                WatchNavHost(viewModel = viewModel)
            }
        }
    }
}
```
