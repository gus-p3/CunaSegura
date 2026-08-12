# Guía Paso a Paso: Construyendo el Módulo Smart TV de Cuna Segura

Esta guía documenta y desglosa paso a paso la arquitectura, configuración y construcción completa del módulo **Smart TV (Android TV / Leanback)** de **Cuna Segura - Dolores Hidalgo**, explicando las decisiones técnicas, patrones de diseño y bloques de código esenciales para un proyecto profesional en **Kotlin 2.0+**, **Jetpack Compose for TV (Material 3 TV)**, **OSMDroid (OpenStreetMap)**, **Eclipse Paho MQTT** y **Firebase Realtime Database**.

---

## Objetivo de Esta Guía

Al estudiar y seguir esta guía, comprenderás:

1. Cómo estructurar una aplicación moderna para **Android TV** utilizando **Compose for TV** y la directiva **Leanback**, gestionando la navegación no táctil orientada al foco y control remoto (**D-Pad Focus Management**).
2. Cómo implementar un sistema de **Ingesta Dual en Tiempo Real**: recepción de eventos críticos de baja latencia (<100ms) mediante sockets TCP/SSL con **Eclipse Paho MQTT** y sincronización reactiva de estado persistente (redes, vecinos y miembros) con **Firebase Realtime Database**.
3. Cómo renderizar mapas dinámicos e interactivos en pantallas 1080p y 4K con **OSMDroid (OpenStreetMap)**, implementando controles visuales adaptados a control remoto (Zoom In/Out, Desplazamiento cardinal y Recentrado) sin depender de APIs propietarias ni costos de facturación.
4. Cómo diseñar una interfaz de usuario cinemática (10-foot UI) dividida entre un **Panel Lateral de Telemetría (40%)** y el **Mapa Comunitario (60%/100%)**, con personalización dinámica de colores de marcadores y soporte de pantalla completa.
5. Cómo orquestar la reproducción de alarmas sonoras prioritarias mediante **MediaPlayer** y **RingtoneManager**, superponiendo un **Modal Crítico de Emergencia SOS (AlertModalOverlay)** con parpadeo estroboscópico de alto contraste legible a distancia.

---

## FASE 1: Configuración Inicial del Entorno y Build System de Smart TV

### Paso 1.1: Configurar el Catálogo de Versiones (`gradle/libs.versions.toml`)

El catálogo de versiones define de manera centralizada las dependencias de Compose for TV, Leanback, Firebase, Paho MQTT y OSMDroid.

> 📋 **INSTRUCCIÓN:** Asegúrate de incluir las siguientes definiciones en tu archivo `gradle/libs.versions.toml`:

```toml
[versions]
agp = "9.2.1"
kotlin = "2.4.0"
coreKtx = "1.10.1"
activityCompose = "1.13.0"
composeBom = "2024.09.00"
googleServices = "4.4.2"
firebaseBom = "33.1.2"
lifecycleRuntimeKtx = "2.6.1"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version = "2.7.7" }
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-database = { group = "com.google.firebase", name = "firebase-database" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

> **CONCEPTO CLAVE:** Centralizar las dependencias garantiza compatibilidad entre Compose for TV, el motor de coroutines y el plugin de Google Services.

---

### Paso 1.2: Configurar `cunaseguratv/build.gradle.kts`

El módulo `:cunaseguratv` configura las librerías especializadas de Compose for TV, OSMDroid para cartografía, ZXing para generación del código QR de vinculación, el cliente Paho MQTT y la inyección de credenciales seguras leídas desde `local.properties`.

> 📋 **INSTRUCCIÓN:** Copia la configuración del build script de `cunaseguratv/build.gradle.kts`:

```kotlin
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    id("com.google.gms.google-services")
}

android {
    namespace = "mx.edu.utng.cunaseguratv"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "mx.edu.utng.cunaseguratv"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        val localProps = Properties().apply {
            val f = rootProject.file("local.properties")
            if (f.exists()) load(f.inputStream())
        }

        buildConfigField("String", "HIVEMQ_BROKER_URL", "\"${localProps["hivemq.brokerUrl"]}\"")
        buildConfigField("String", "HIVEMQ_USERNAME",   "\"${localProps["hivemq.username"]}\"")
        buildConfigField("String", "HIVEMQ_PASSWORD",   "\"${localProps["hivemq.password"]}\"")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    buildTypes {
        release {
            optimization {
                enable = false
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)

    // Compose for TV
    implementation("androidx.tv:tv-foundation:1.0.0")
    implementation("androidx.tv:tv-material:1.0.0")

    // Compose Base
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation("androidx.compose.foundation:foundation")
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation(libs.androidx.activity.compose)
    
    // ViewModel + Coroutines
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation(libs.kotlinx.coroutines.android)

    // Eclipse Paho MQTT
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    // ZXing for QR Code generation
    implementation("com.google.zxing:core:3.5.3")

    // OSMDroid for map (OpenStreetMap para Android)
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database)
}
```

> **CONCEPTO CLAVE:** Al inyectar las credenciales mediante `buildConfigField`, evitamos exponer tokens y contraseñas sensibles directamente en el control de versiones (Git).

---

### Paso 1.3: Configurar `cunaseguratv/src/main/AndroidManifest.xml`

El archivo de manifiesto configura los requerimientos de hardware para dispositivos Android TV:
1. `android.software.leanback`: Obligatorio para aparecer en el Launcher de Android TV.
2. `android.hardware.touchscreen` con `required="false"`: Permite la instalación en televisores y TV Boxes sin pantalla táctil.
3. `android:screenOrientation="landscape"`: Fija la orientación horizontal panorámica.
4. Categoría `android.intent.category.LEANBACK_LAUNCHER`: Declara el banner de lanzamiento para la interfaz de Android TV.

> 📋 **INSTRUCCIÓN:** Copia el manifiesto completo de `cunaseguratv/src/main/AndroidManifest.xml`:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />

    <uses-feature
        android:name="android.software.leanback"
        android:required="true" />

    <uses-feature
        android:name="android.hardware.touchscreen"
        android:required="false" />

    <application
        android:name=".CunaSeguraTVApp"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/Theme.CunaSeguraTv">

        <activity
            android:name=".TVMainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:screenOrientation="landscape">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LEANBACK_LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

---

### Paso 1.4: Configurar Recursos XML (`strings.xml`, `colors.xml`, `themes.xml`)

> 📋 **INSTRUCCIÓN:** Crea y define los recursos básicos en `cunaseguratv/src/main/res/values/`:

**`strings.xml`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">CunaSeguraTV</string>
</resources>
```

**`colors.xml`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="purple_200">#FFBB86FC</color>
    <color name="purple_500">#FF6200EE</color>
    <color name="purple_700">#FF3700B3</color>
    <color name="teal_200">#FF03DAC5</color>
    <color name="teal_700">#FF018786</color>
    <color name="black">#FF000000</color>
    <color name="white">#FFFFFFFF</color>
    <!-- Cuna Segura Colors -->
    <color name="primaryDark">#85D1E8</color>
    <color name="backgroundDark">#0F1416</color>
    <color name="errorDark">#FFB4AB</color>
</resources>
```

**`themes.xml`:**
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.CunaSeguraTv" parent="@android:style/Theme.DeviceDefault.NoActionBar">
        <item name="android:colorPrimary">@color/primaryDark</item>
        <item name="android:colorBackground">@color/backgroundDark</item>
    </style>
</resources>
```

---

## FASE 2: Sistema de Diseño y Tema Visual (Compose for TV)

### Paso 2.1: Paleta de Colores Cinemática y Esquema Oscuro (`CunaSeguraTvTheme.kt`)

La UI para televisión requiere contraste alto (10-foot UI) optimizado para pantallas OLED y LED en salas oscuras o con iluminación tenue.

> 📋 **INSTRUCCIÓN:** Crea el archivo `cunaseguratv/src/main/java/mx/edu/utng/cunaseguratv/presentation/theme/CunaSeguraTvTheme.kt`:

```kotlin
package mx.edu.utng.cunaseguratv.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.tv.material3.ColorScheme
import androidx.tv.material3.darkColorScheme
import androidx.tv.material3.MaterialTheme
import androidx.compose.runtime.Composable

/** Tokens de color del sistema de diseño para Android TV (Optimizado para alto contraste en pantallas OLED/LED). */
val primaryDark = Color(0xFFAAC7FF)
val onPrimaryDark = Color(0xFF0A305F)
val primaryContainerDark = Color(0xFF274777)
val onPrimaryContainerDark = Color(0xFFD6E3FF)

val secondaryDark = Color(0xFFBEC6DC)
val onSecondaryDark = Color(0xFF283141)
val secondaryContainerDark = Color(0xFF3E4758)
val onSecondaryContainerDark = Color(0xFFDAE2F9)

val tertiaryDark = Color(0xFF85D1E8)
val onTertiaryDark = Color(0xFF00363F)
val tertiaryContainerDark = Color(0xFF004E5B)
val onTertiaryContainerDark = Color(0xFFA6EEFF)

val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)

val backgroundDark = Color(0xFF0F1416)
val onBackgroundDark = Color(0xFFE2E2E9)
val surfaceDark = Color(0xFF1B2022)
val onSurfaceDark = Color(0xFFE2E2E9)
val surfaceVariantDark = Color(0xFF44474E)
val onSurfaceVariantDark = Color(0xFFC4C6D0)

/** Esquema de colores oscuros exclusivo para Compose for TV. */
private val cunaSeguraTvColorScheme: ColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark
)

/**
 * Tema principal del módulo TV basado en `androidx.tv.material3.MaterialTheme`.
 *
 * Aplica una paleta cromática oscura cinemática y provee el contexto de diseño
 * para todos los componentes de Compose for TV en la aplicación.
 *
 * @param content Contenido composable envuelto en el tema.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
@Composable
fun CunaSeguraTvTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = cunaSeguraTvColorScheme,
        content = content
    )
}
```

> **CONCEPTO CLAVE:** `androidx.tv.material3.MaterialTheme` difiere del `MaterialTheme` estándar de Android móvil porque sus esquemas de color y tipografía están diseñados para el manejo del estado de foco (`focus`) y legibilidad a distancia.

---

## FASE 3: Capa de Datos, Modelos y Comunicación IoT

### Paso 3.1: Modelo Inmutable de Alerta Comunitaria (`AlertaTV.kt`)

> 📋 **INSTRUCCIÓN:** Crea el archivo `cunaseguratv/src/main/java/mx/edu/utng/cunaseguratv/data/AlertaTV.kt`:

```kotlin
package mx.edu.utng.cunaseguratv.data

/**
 * Modelo de datos inmutable que representa una alerta de seguridad comunitaria en la TV.
 *
 * Utilizado para mapear la información recibida desde Firebase Realtime Database
 * dentro del nodo `/alertas` y reflejar el historial reciente de incidentes en el Dashboard.
 *
 * @property id Identificador entero secuencial de la alerta.
 * @property usuarioId Identificador del usuario que emitió la alerta.
 * @property nombreUsuario Nombre completo del vecino que activó la señal de auxilio.
 * @property estado Estado operativo de la alerta (e.g. "activa", "cancelada", "atendida").
 * @property latitud Coordenada GPS de latitud del incidente.
 * @property longitud Coordenada GPS de longitud del incidente.
 * @property fueAtendida Bandera booleana que indica si las autoridades o vecinos acudieron al auxilio.
 * @property esFalsaAlarma Bandera booleana que marca si la alerta fue clasificada como falsa alarma.
 * @property creadoEn Marca de tiempo epoch en milisegundos cuando se detonó el incidente.
 * @property networkId Identificador de la red vecinal a la que pertenece el usuario emisor.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
data class AlertaTV(
    val id: Int = 0,
    val usuarioId: Int = 0,
    val nombreUsuario: String = "Vecino",
    val estado: String = "activa",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fueAtendida: Boolean = false,
    val esFalsaAlarma: Boolean = false,
    val creadoEn: Long = System.currentTimeMillis(),
    val networkId: String = ""
)
```

---

### Paso 3.2: Modelos Serializables JSON para Mensajería MQTT (`AlertaMqttMessage.kt`)

> 📋 **INSTRUCCIÓN:** Crea el archivo `cunaseguratv/src/main/java/mx/edu/utng/cunaseguratv/mqtt/AlertaMqttMessage.kt`:

```kotlin
package mx.edu.utng.cunaseguratv.mqtt

import kotlinx.serialization.Serializable

/**
 * Carga útil (Payload) serializable en JSON enviada y recibida mediante el broker MQTT en el tópico `cunasegura/alertas`.
 *
 * Transporta la información crítica en tiempo real sobre la activación o cancelación
 * de una alerta de emergencia originada en un Smartwatch (Wear OS) o Smartphone.
 *
 * @property usuarioId Identificador del usuario emisor del evento.
 * @property nombreUsuario Nombre legible del vecino que detonó la alarma.
 * @property latitud Coordenada GPS de latitud donde se originó el evento.
 * @property longitud Coordenada GPS de longitud donde se originó el evento.
 * @property nivelAlerta Cantidad de toques físicos (1-4) que determinan la severidad o tipo de alerta.
 * @property estado Estado de la alarma ("activa" para disparar sirena/modal, "cancelada" para apagarla).
 * @property timestamp Marca de tiempo epoch en milisegundos de la emisión del mensaje.
 * @property networkId Identificador de la red vecinal a la que pertenece el usuario.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
@Serializable
data class AlertaMqttMessage(
    val usuarioId: Int,
    val nombreUsuario: String = "Vecino Desconocido",
    val latitud: Double,
    val longitud: Double,
    val nivelAlerta: Int = 3, // Cantidad de toques
    val estado: String = "activa",
    val timestamp: Long = System.currentTimeMillis(),
    val networkId: String = ""
)

/**
 * Mensaje de telemetría y presencia serializable en JSON emitido en el tópico `cunasegura/tv/status`.
 *
 * Permite a otros nodos del ecosistema (como la app móvil) conocer si la Smart TV
 * está en línea y lista para proyectar alertas de emergencia.
 *
 * @property tvId Identificador único asignado a la Smart TV.
 * @property networkId Identificador de la red vecinal a la que está emparejada la TV.
 * @property isOnline Estado booleano de conexión (true = conectada, false = desconectada).
 * @property timestamp Marca de tiempo epoch en milisegundos de la publicación del estado.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
@Serializable
data class TvStatusMessage(
    val tvId: String,
    val networkId: String,
    val isOnline: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
```

---

### Paso 3.3: Configuración del Broker y Credenciales Seguras (`MqttConfig.kt`)

> 📋 **INSTRUCCIÓN:** Crea el archivo `cunaseguratv/src/main/java/mx/edu/utng/cunaseguratv/mqtt/MqttConfig.kt`:

```kotlin
package mx.edu.utng.cunaseguratv.mqtt

import mx.edu.utng.cunaseguratv.BuildConfig

/**
 * Objeto de configuración centralizada para la comunicación MQTT en la Central de Monitoreo (Smart TV).
 *
 * Administra las credenciales leídas dinámicamente desde [BuildConfig] generadas a partir de `local.properties`,
 * así como los nombres de los tópicos del protocolo y parámetros de calidad de servicio (QoS).
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
object MqttConfig {
    /** URL del Broker MQTT (ej. `ssl://xxxx.hivemq.cloud:8883` o `tcp://broker.hivemq.com:1883`). */
    val BROKER_URL: String = BuildConfig.HIVEMQ_BROKER_URL

    /** Nombre de usuario para la autenticación en el Broker HiveMQ. */
    val USERNAME:   String = BuildConfig.HIVEMQ_USERNAME

    /** Contraseña para la autenticación en el Broker HiveMQ. */
    val PASSWORD:   String = BuildConfig.HIVEMQ_PASSWORD

    /** Tópico para la recepción de alertas de emergencia SOS en tiempo real. */
    const val TOPIC_ALERTAS = "cunasegura/alertas"

    /** Tópico para el intercambio de mensajes de vinculación y emparejamiento con el móvil. */
    const val TOPIC_VINCULACION = "cunasegura/tv/vinculacion"

    /** Tópico para publicar el estado de conectividad (Online/Offline) de la Smart TV. */
    const val TOPIC_STATUS = "cunasegura/tv/status"

    /** Nivel de Calidad de Servicio (QoS 1 = Al menos una entrega garantizada). */
    const val QOS = 1

    /** Prefijo para generar identificadores de cliente únicos por cada Smart TV. */
    const val CLIENT_TV_BASE = "cunasegura-tv-"
}
```

---

## FASE 4: Sincronización en la Nube y Protocolos de Red (Firebase & MQTT)

### Paso 4.1: Listener Reactivo con Coroutines Flow para Firebase RTDB (`FirebaseAlertListener.kt`)

> 📋 **INSTRUCCIÓN:** Crea el archivo `cunaseguratv/src/main/java/mx/edu/utng/cunaseguratv/data/FirebaseAlertListener.kt`:

```kotlin
package mx.edu.utng.cunaseguratv.data

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/**
 * Servicio encargado de escuchar y sincronizar en tiempo real las alertas de seguridad
 * almacenadas en Firebase Realtime Database para el módulo Smart TV.
 *
 * Transforma los eventos de [ValueEventListener] de Firebase en un [Flow] asíncrono y reactivo,
 * aplicando filtros de expiración temporal basados en `tiempoVidaAlerta`, eliminando duplicados
 * y ordenando las alertas cronológicamente.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
class FirebaseAlertListener {

    private val dbRef = FirebaseDatabase.getInstance().getReference("alertas")
    private val TAG = "FirebaseAlertListener"

    /**
     * Inicia la escucha reactiva de alertas activas desde Firebase Realtime Database.
     *
     * Realiza las siguientes operaciones:
     * 1. Consulta la configuración global (`configuracion_global/tiempoVidaAlerta`) para determinar
     *    la ventana de validez temporal de las alertas (por defecto 720 minutos / 12 horas).
     * 2. Suscribe un [ValueEventListener] en `/alertas`.
     * 3. Filtra alertas con estado `"activa"` pertenecientes a [targetNetworkId] o globales.
     * 4. Descarta alertas expiradas respecto a la marca temporal actual.
     * 5. Agrupa por vecino para conservar únicamente la alerta más reciente por usuario.
     * 6. Emite la lista resultante ordenada en forma descendente por fecha de creación.
     *
     * @param targetNetworkId Identificador opcional de la red vecinal vinculada. Si está vacío, procesa todas las alertas.
     * @return [Flow] reactivo con la lista de alertas activas [AlertaTV] actualizadas en tiempo real.
     */
    fun escucharAlertasActivas(targetNetworkId: String = ""): Flow<List<AlertaTV>> = callbackFlow {
        var tiempoVidaMs = 720L * 60 * 1000 // 720 minutos por defecto (12h)
        FirebaseDatabase.getInstance().getReference("configuracion_global").child("tiempoVidaAlerta").get().addOnSuccessListener { snap ->
            val minutos = snap.getValue(Double::class.java) ?: 720.0
            tiempoVidaMs = (minutos * 60 * 1000).toLong()
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alertas = mutableListOf<AlertaTV>()
                for (child in snapshot.children) {
                    try {
                        val estado = child.child("estado").getValue(String::class.java) ?: ""
                        val netId = child.child("networkId").getValue(String::class.java) ?: ""

                        // Solo procesar si está activa y (si hay targetNetworkId) coincide con la red de la TV
                        if (estado == "activa" && (targetNetworkId.isEmpty() || netId == targetNetworkId || netId.isEmpty())) {
                            val rawUsuarioId = child.child("usuarioId").value
                            val usuarioIdInt = when (rawUsuarioId) {
                                is Long -> rawUsuarioId.toInt()
                                is Int -> rawUsuarioId
                                is String -> rawUsuarioId.toIntOrNull() ?: 0
                                else -> 0
                            }

                            alertas.add(
                                AlertaTV(
                                    id = child.child("id").getValue(Int::class.java) ?: 0,
                                    usuarioId = usuarioIdInt,
                                    nombreUsuario = child.child("nombreUsuario").getValue(String::class.java) ?: "Vecino",
                                    estado = estado,
                                    latitud = child.child("latitud").getValue(Double::class.java) ?: 0.0,
                                    longitud = child.child("longitud").getValue(Double::class.java) ?: 0.0,
                                    fueAtendida = child.child("fueAtendida").getValue(Boolean::class.java) ?: false,
                                    esFalsaAlarma = child.child("esFalsaAlarma").getValue(Boolean::class.java) ?: false,
                                    creadoEn = child.child("creadoEn").getValue(Long::class.java) ?: 0L,
                                    networkId = netId
                                )
                            )
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing alerta en TV", e)
                    }
                }
                
                val ahora = System.currentTimeMillis()
                val activas = alertas.filter { (ahora - it.creadoEn) <= tiempoVidaMs }
                
                // Agrupar por usuario y quedarnos solo con la alerta más reciente por persona
                val activasPorUsuario = activas.groupBy { it.nombreUsuario }.map { entry ->
                    entry.value.maxByOrNull { it.creadoEn }!!
                }
                
                trySend(activasPorUsuario.sortedByDescending { it.creadoEn })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase listener cancelled", error.toException())
            }
        }

        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }
}
```

> **CONCEPTO CLAVE:** El operador `callbackFlow` convierte el listener de Firebase basado en callbacks en un flujo frío de Kotlin Coroutines, limpiando la suscripción con `awaitClose` cuando el flujo deja de recolectarse.

---

### Paso 4.2: Cliente Suscriptor Asíncrono Eclipse Paho MQTT (`MqttTvSubscriber.kt`)

> 📋 **INSTRUCCIÓN:** Crea el archivo `cunaseguratv/src/main/java/mx/edu/utng/cunaseguratv/mqtt/MqttTvSubscriber.kt`:

```kotlin
package mx.edu.utng.cunaseguratv.mqtt

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import java.util.UUID

/**
 * Carga útil recibida en el tópico `cunasegura/tv/vinculacion` cuando el smartphone
 * escanea el código QR de la Smart TV y envía el emparejamiento con la red vecinal.
 *
 * @property tvId Identificador único de la TV que se vinculó.
 * @property networkId Identificador de la red vecinal asociada a la vinculación.
 */
@kotlinx.serialization.Serializable
data class TvVinculacionMqttMessage(
    val tvId: String,
    val networkId: String
)

/**
 * Cliente suscriptor MQTT asíncrono diseñado para Android TV.
 *
 * Implementa [MqttCallback] y [IMqttActionListener] mediante la librería Eclipse Paho MQTT.
 * Mantiene una conexión ligera TCP/SSL abierta con el broker HiveMQ, procesa la llegada
 * de alertas de seguridad instantáneas, filtra mensajes por `networkId` y emite
 * notificaciones reactivas a través de [MutableStateFlow].
 *
 * @property alertFlow Flujo reactivo mutable donde se emiten las alertas recibidas en tiempo real.
 * @property vinculacionFlow Flujo reactivo mutable donde se despachan eventos de vinculación remota.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
class MqttTvSubscriber(
    private val alertFlow: MutableStateFlow<AlertaMqttMessage?>,
    private val vinculacionFlow: MutableStateFlow<TvVinculacionMqttMessage?> = MutableStateFlow(null)
) {
    private var client: MqttAsyncClient? = null
    private val TAG = "MQTT_TV"
    private val clientId = MqttConfig.CLIENT_TV_BASE + UUID.randomUUID().toString().substring(0, 8)
    private var networkId: String = ""

    /** Estado reactivo que expone si la TV mantiene una conexión activa con el broker MQTT. */
    val isConnected = MutableStateFlow(false)

    /**
     * Actualiza el identificador de la red vecinal a la que pertenece esta Smart TV.
     * Permite filtrar alertas entrantes para no mostrar eventos de otras vecindades.
     *
     * @param id Identificador único de la red vecinal.
     */
    fun setNetworkId(id: String) {
        networkId = id
    }

    /**
     * Establece la conexión asíncrona con el broker MQTT configurado en [MqttConfig].
     *
     * Configura:
     * - Opciones SSL/TLS con [javax.net.ssl.SSLSocketFactory].
     * - Reconexión automática habilitada (`isAutomaticReconnect = true`).
     * - Tiempos de keep-alive (20s) y timeout de conexión (10s).
     * - Suscripción a tópicos de alertas y vinculación tras una conexión exitosa.
     * - Notificación de estado en línea mediante [publicarEstadoTv].
     */
    fun connect() {
        if (client?.isConnected == true) return

        try {
            client = MqttAsyncClient(
                MqttConfig.BROKER_URL,
                clientId,
                MemoryPersistence()
            )

            client?.setCallback(object : MqttCallback {
                /**
                 * Callback invocado cuando arriba un nuevo mensaje MQTT en cualquiera de los tópicos suscritos.
                 *
                 * @param topic Nombre del tópico en el que se recibió el mensaje.
                 * @param msg Objeto de mensaje con la carga binaria (payload) recibida.
                 */
                override fun messageArrived(topic: String, msg: MqttMessage) {
                    try {
                        val payloadStr = String(msg.payload)
                        Log.d(TAG, "Mensaje MQTT recibido en topic $topic: $payloadStr")
                        
                        if (topic == MqttConfig.TOPIC_VINCULACION) {
                            val vinculacionMsg = Json.decodeFromString<TvVinculacionMqttMessage>(payloadStr)
                            vinculacionFlow.value = vinculacionMsg
                        } else if (topic == MqttConfig.TOPIC_ALERTAS) {
                            val alertaMsg = Json.decodeFromString<AlertaMqttMessage>(payloadStr)
                            
                            // Filtrar: la TV debe estar vinculada (networkId no vacío) y la alerta debe pertenecer a su misma red vecinal
                            if (networkId.isNotEmpty() && (alertaMsg.networkId == networkId || alertaMsg.networkId.isEmpty())) {
                                if (alertaMsg.estado == "activa") {
                                    alertFlow.value = alertaMsg
                                } else if (alertaMsg.estado == "cancelada") {
                                    // Si se cancela y es la misma alerta activa, limpiamos
                                    if (alertFlow.value?.usuarioId == alertaMsg.usuarioId) {
                                        alertFlow.value = null
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Error al decodificar mensaje MQTT: ${e.message}")
                    }
                }

                /**
                 * Invocado cuando la conexión con el broker MQTT se interrumpe inesperadamente.
                 *
                 * @param cause Causa de la desconexión.
                 */
                override fun connectionLost(cause: Throwable?) {
                    Log.w(TAG, "Conexión perdida con el broker MQTT: ${cause?.message}")
                    isConnected.value = false
                }

                /**
                 * Invocado cuando se completa la entrega de un mensaje publicado (QoS 1 o 2).
                 *
                 * @param token Token de entrega asociado.
                 */
                override fun deliveryComplete(token: IMqttDeliveryToken?) {}
            })

            val options = MqttConnectOptions().apply {
                userName = MqttConfig.USERNAME
                password = MqttConfig.PASSWORD.toCharArray()
                isCleanSession = true
                socketFactory = javax.net.ssl.SSLSocketFactory.getDefault()
                connectionTimeout = 10
                keepAliveInterval = 20
                isAutomaticReconnect = true
            }

            Log.d(TAG, "Conectando al broker MQTT: ${MqttConfig.BROKER_URL} ...")
            client?.connect(options, null, object : IMqttActionListener {
                /**
                 * Invocado cuando la conexión con el broker se establece exitosamente.
                 * Realiza la suscripción a los tópicos y publica la presencia en línea.
                 */
                override fun onSuccess(token: IMqttToken?) {
                    isConnected.value = true
                    Log.d(TAG,"✅ TV conectada a MQTT con ID: $clientId")
                    
                    // Suscribirse a las alertas generales y tópicos de vinculación
                    client?.subscribe(MqttConfig.TOPIC_ALERTAS, MqttConfig.QOS)
                    client?.subscribe(MqttConfig.TOPIC_VINCULACION, MqttConfig.QOS)
                    Log.d(TAG,"✅ Suscrito a ${MqttConfig.TOPIC_ALERTAS} y ${MqttConfig.TOPIC_VINCULACION}")
                    
                    publicarEstadoTv(true)
                }

                /**
                 * Invocado cuando la solicitud de conexión al broker falla.
                 */
                override fun onFailure(token: IMqttToken?, ex: Throwable?) {
                    isConnected.value = false
                    Log.e(TAG,"❌ Error de conexión MQTT", ex)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Error inicializando MQTT client", e)
        }
    }

    /**
     * Publica el estado de telemetría y disponibilidad de la Smart TV en el tópico `cunasegura/tv/status`.
     *
     * @param online `true` si la televisión está lista para recibir alarmas, `false` si se apaga o desconecta.
     */
    private fun publicarEstadoTv(online: Boolean) {
        try {
            if (client?.isConnected == true) {
                val statusMsg = TvStatusMessage(tvId = clientId, networkId = networkId, isOnline = online)
                val payload = Json.encodeToString(TvStatusMessage.serializer(), statusMsg).toByteArray()
                client?.publish(MqttConfig.TOPIC_STATUS, payload, MqttConfig.QOS, true)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error publicando estado", e)
        }
    }

    /**
     * Cierra la sesión activa con el broker MQTT y libera los sockets de red.
     * Publica previamente el estado offline de la televisión.
     */
    fun disconnect() {
        try {
            publicarEstadoTv(false)
            client?.disconnect()
            isConnected.value = false
        } catch (e: Exception) {
            Log.e(TAG, "Error desconectando MQTT", e)
        }
    }
}
```

---

## FASE 5: Inicialización y Ciclo de Vida de la Aplicación

### Paso 5.1: Clase Application y Caché de OpenStreetMap (`CunaSeguraTVApp.kt`)

> 📋 **INSTRUCCIÓN:** Crea el archivo `cunaseguratv/src/main/java/mx/edu/utng/cunaseguratv/CunaSeguraTVApp.kt`:

```kotlin
package mx.edu.utng.cunaseguratv

import android.app.Application
import java.io.File

/**
 * Clase principal de la aplicación para el módulo Android TV (Cuna Segura TV).
 *
 * Extiende de [Application] y actúa como el punto de entrada global para inicializar
 * configuraciones compartidas del sistema antes de que se cree cualquier Activity.
 * En particular, inicializa el motor de mapas OpenStreetMap (OSMDroid), configurando
 * el User-Agent específico y los directorios de caché de teselas (tiles) en disco.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
class CunaSeguraTVApp : Application() {

    /**
     * Se ejecuta al arrancar el proceso de la aplicación.
     * Configura los parámetros globales de OSMDroid requeridos para evitar bloqueos
     * de descarga de mapas por parte de los servidores de OpenStreetMap:
     * - `userAgentValue`: Identificador único de la app para peticiones HTTP de teselas.
     * - `osmdroidBasePath`: Directorio base para almacenamiento temporal del mapa.
     * - `osmdroidTileCache`: Subcarpeta exclusiva para el caché de imágenes de mapas.
     */
    override fun onCreate() {
        super.onCreate()
        val osmConfig = org.osmdroid.config.Configuration.getInstance()
        osmConfig.userAgentValue = packageName
        
        val basePath = File(cacheDir, "osmdroid")
        osmConfig.osmdroidBasePath = basePath
        osmConfig.osmdroidTileCache = File(basePath, "tiles")
    }
}
```

---

## FASE 6: Capa de Presentación y Gestión de Estado Reactivo (MVVM + StateFlow)

### Paso 6.1: ViewModel Central de Monitoreo y Audio Alarma (`TvMonitorViewModel.kt`)

> 📋 **INSTRUCCIÓN:** Crea el archivo `cunaseguratv/src/main/java/mx/edu/utng/cunaseguratv/presentation/TvMonitorViewModel.kt`:

```kotlin
package mx.edu.utng.cunaseguratv.presentation

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.media.MediaPlayer
import android.media.RingtoneManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import mx.edu.utng.cunaseguratv.data.AlertaTV
import mx.edu.utng.cunaseguratv.data.FirebaseAlertListener
import mx.edu.utng.cunaseguratv.mqtt.AlertaMqttMessage
import mx.edu.utng.cunaseguratv.mqtt.MqttTvSubscriber
import mx.edu.utng.cunaseguratv.mqtt.TvVinculacionMqttMessage
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.UUID

/**
 * Modelo de datos ligero para ubicar geográficamente a un vecino en el mapa.
 *
 * @property id Identificador único del usuario/vecino en Firebase.
 * @property nombre Nombre legible del vecino.
 * @property lat Coordenada de latitud GPS.
 * @property lon Coordenada de longitud GPS.
 */
data class VecinoLocation(val id: String, val nombre: String, val lat: Double, val lon: Double)

/**
 * Modelo de información detallada de contacto de un vecino de la red comunitaria.
 *
 * @property id Identificador único del vecino.
 * @property nombre Nombre completo del vecino.
 * @property telefono Número telefónico registrado.
 * @property correo Correo electrónico del usuario.
 * @property lat Coordenada de latitud actual.
 * @property lon Coordenada de longitud actual.
 */
data class VecinoInfo(
    val id: String,
    val nombre: String,
    val telefono: String,
    val correo: String,
    val lat: Double,
    val lon: Double
)

/**
 * Estado inmutable de la interfaz de usuario para la pantalla de Android TV.
 *
 * Sigue el patrón Unidirectional Data Flow (UDF) exponiendo todos los datos reactivos que
 * consumen las pantallas [mx.edu.utng.cunaseguratv.presentation.screens.VinculacionScreen],
 * [mx.edu.utng.cunaseguratv.presentation.screens.DashboardScreen] y
 * [mx.edu.utng.cunaseguratv.presentation.screens.AlertModalOverlay].
 *
 * @property isVinculada Indica si la TV está vinculada activamente a una red vecinal.
 * @property networkId Identificador de la red vecinal emparejada.
 * @property networkNombre Nombre legible de la red vecinal.
 * @property usuarioNombre Nombre del titular o usuario que vinculó la TV.
 * @property usuarioCorreo Correo electrónico del titular vinculado.
 * @property usuarioId Identificador del usuario que vinculó la TV.
 * @property qrCode Imagen [Bitmap] generada con el código QR de vinculación inicial.
 * @property mqttConnected Estado de conexión TCP/SSL con el Broker MQTT.
 * @property alertaActiva Alerta SOS de emergencia en curso (despliega modal y sirena sonora).
 * @property alertasRecientes Lista histórica de alertas registradas en la red vecinal.
 * @property showAlertModal Controla la visibilidad del modal crítico a pantalla completa.
 * @property isSilenced Indica si el operador de la TV silenció manualmente la alarma auditiva.
 * @property vecinosLocations Lista de coordenadas de los vecinos activos para dibujar marcadores.
 * @property vecinosList Lista de vecinos registrados con información de contacto.
 * @property colorUsuario Color hexadecimal ARGB para el marcador del usuario vinculado en el mapa.
 * @property colorVecinos Color hexadecimal ARGB para los marcadores de otros vecinos en el mapa.
 * @property colorAlertas Color hexadecimal ARGB para el marcador de emergencias SOS en el mapa.
 * @property showColorPicker Controla la visibilidad del diálogo selector de colores del mapa.
 */
data class TvUiState(
    val isVinculada: Boolean = false,
    val networkId: String = "",
    val networkNombre: String = "",
    val usuarioNombre: String = "",
    val usuarioCorreo: String = "",
    val usuarioId: String = "",
    val qrCode: Bitmap? = null,
    val mqttConnected: Boolean = false,
    val alertaActiva: AlertaMqttMessage? = null,
    val alertasRecientes: List<AlertaTV> = emptyList(),
    val showAlertModal: Boolean = false,
    val isSilenced: Boolean = false,
    val vecinosLocations: List<VecinoLocation> = emptyList(),
    val vecinosList: List<VecinoInfo> = emptyList(),
    // Colores personalizables de los marcadores del mapa
    val colorUsuario: Int = android.graphics.Color.parseColor("#2196F3"),   // Azul
    val colorVecinos: Int = android.graphics.Color.parseColor("#4CAF50"),   // Verde
    val colorAlertas: Int = android.graphics.Color.parseColor("#F44336"),   // Rojo
    val showColorPicker: Boolean = false
)

/**
 * ViewModel principal de la estación de monitoreo Android TV (Architecture Component).
 *
 * Centraliza la lógica de negocio, la orquestación de red y la gestión del estado para la TV:
 * - Generación local de códigos QR de emparejamiento con ZXing.
 * - Sincronización en tiempo real mediante Firebase Realtime Database.
 * - Recepción de eventos push instantáneos vía MQTT (Eclipse Paho).
 * - Reproducción de sirenas sonoras de emergencia con [MediaPlayer].
 * - Persistencia de colores y claves en [android.content.SharedPreferences].
 *
 * @param application Instancia global de la aplicación Android.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
class TvMonitorViewModel(application: Application) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(TvUiState())
    /** Flujo de estado inmutable público expuesto a la UI de Compose. */
    val state: StateFlow<TvUiState> = _state.asStateFlow()

    private val alertMqttFlow = MutableStateFlow<AlertaMqttMessage?>(null)
    private val vinculacionMqttFlow = MutableStateFlow<TvVinculacionMqttMessage?>(null)
    private val mqttSubscriber = MqttTvSubscriber(alertMqttFlow, vinculacionMqttFlow)
    
    private val firebaseListener = FirebaseAlertListener()
    private var mediaPlayer: android.media.MediaPlayer? = null

    private var netInfoListenerRef: com.google.firebase.database.DatabaseReference? = null
    private var netInfoListener: ValueEventListener? = null

    private var titularListenerRef: com.google.firebase.database.DatabaseReference? = null
    private var titularListener: ValueEventListener? = null

    private var vecinosListenerRef: com.google.firebase.database.DatabaseReference? = null
    private var vecinosListener: ValueEventListener? = null

    init {
        // Cargar colores personalizados guardados previamente en SharedPreferences
        cargarColores()

        // Inicialmente generar QR de vinculación
        generarQRVinculacion()
        
        // Conectar al broker MQTT
        mqttSubscriber.connect()

        // Monitorear el estado de conexión del suscriptor MQTT
        viewModelScope.launch {
            mqttSubscriber.isConnected.collect { connected ->
                _state.update { it.copy(mqttConnected = connected) }
            }
        }

        // Escuchar alertas instantáneas vía MQTT y disparar la alarma sonora
        viewModelScope.launch {
            alertMqttFlow.collect { mqttAlerta ->
                // Solo reproducir alarma si la TV está vinculada
                if (mqttAlerta != null && _state.value.isVinculada) {
                    reproducirAlarma()
                    _state.update { 
                        it.copy(
                            alertaActiva = mqttAlerta,
                            showAlertModal = true,
                            isSilenced = false
                        )
                    }
                } else if (mqttAlerta == null) {
                    // Si se anula es porque fue cancelada
                    detenerAlarma()
                    _state.update { 
                        it.copy(
                            alertaActiva = null,
                            showAlertModal = false,
                            isSilenced = false
                        )
                    }
                }
            }
        }

        // Escuchar alertas históricas/recientes en Firebase (solo para la red vinculada)
        viewModelScope.launch {
            firebaseListener.escucharAlertasActivas().collect { alertas ->
                val currentNetId = _state.value.networkId
                val alertasFiltradas = if (currentNetId.isNotEmpty()) {
                    alertas.filter { it.networkId == currentNetId || it.networkId.isEmpty() }
                } else emptyList()

                _state.update { it.copy(alertasRecientes = alertasFiltradas) }
                
                if (_state.value.isVinculada) {
                    val masReciente = alertasFiltradas.firstOrNull { it.estado == "activa" }
                    if (masReciente != null && _state.value.alertaActiva == null) {
                        val alertaMqtt = AlertaMqttMessage(
                            usuarioId = masReciente.usuarioId,
                            nombreUsuario = masReciente.nombreUsuario,
                            latitud = masReciente.latitud,
                            longitud = masReciente.longitud,
                            estado = masReciente.estado,
                            timestamp = masReciente.creadoEn
                        )
                        reproducirAlarma()
                        _state.update {
                            it.copy(
                                alertaActiva = alertaMqtt,
                                showAlertModal = true,
                                isSilenced = false
                            )
                        }
                    } else if (alertasFiltradas.none { it.estado == "activa" } && _state.value.alertaActiva != null) {
                        detenerAlarma()
                        _state.update {
                            it.copy(
                                alertaActiva = null,
                                showAlertModal = false,
                                isSilenced = false
                            )
                        }
                    }
                }
            }
        }

        // Escuchar vinculaciones vía MQTT despachadas desde el smartphone
        viewModelScope.launch {
            vinculacionMqttFlow.collect { vinculacion ->
                if (vinculacion != null) {
                    val prefs = getApplication<Application>().getSharedPreferences("CunaSeguraTV", Context.MODE_PRIVATE)
                    val tvId = prefs.getString("tvId", null)
                    if (tvId != null && tvId == vinculacion.tvId) {
                        simularVinculacionExitosa(vinculacion.networkId, null)
                    }
                }
            }
        }
    }

    /**
     * Suscribe listeners reactivos a los nodos de Firebase correspondientes a la red vecinal,
     * al titular que vinculó la televisión y a todos los miembros vecinos asociados.
     *
     * @param networkId Identificador de la red vecinal a monitorear.
     * @param linkedBy UID de Firebase del usuario que realizó la vinculación.
     */
    private fun escucharVecinosYRedFirebase(networkId: String, linkedBy: String?) {
        val db = FirebaseDatabase.getInstance()

        // Limpiar listeners anteriores para evitar fugas de memoria o conflictos
        netInfoListenerRef?.let { ref -> netInfoListener?.let { l -> ref.removeEventListener(l) } }
        titularListenerRef?.let { ref -> titularListener?.let { l -> ref.removeEventListener(l) } }
        vecinosListenerRef?.let { ref -> vecinosListener?.let { l -> ref.removeEventListener(l) } }

        // 1. Escuchar la información de la red en /networks
        val netRef = db.getReference("networks").child(networkId)
        netInfoListenerRef = netRef
        netInfoListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val netName = snapshot.child("nombre").getValue(String::class.java) ?: "Red Vecinal"
                    _state.update { it.copy(networkNombre = netName) }
                } else {
                    // Si no está en /networks, intentar buscar el nombre del usuario creador
                    db.getReference("usuarios").child(networkId).child("nombre").get()
                        .addOnSuccessListener { userSnap ->
                            val name = userSnap.getValue(String::class.java)
                            val defaultNetName = if (!name.isNullOrBlank()) "Red de $name" else "Red Vecinal"
                            _state.update { it.copy(networkNombre = defaultNetName) }
                        }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        netRef.addValueEventListener(netInfoListener!!)

        // 2. Escuchar datos del usuario vinculador o titular de la red en /usuarios
        val userToListen = if (!linkedBy.isNullOrEmpty()) linkedBy else networkId
        val titRef = db.getReference("usuarios").child(userToListen)
        titularListenerRef = titRef
        titularListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val nombre = snapshot.child("nombre").getValue(String::class.java) ?: "Vecino"
                    val correo = snapshot.child("correo").getValue(String::class.java) ?: ""
                    _state.update { 
                        it.copy(
                            usuarioNombre = nombre,
                            usuarioCorreo = correo,
                            usuarioId = userToListen
                        ) 
                    }
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        titRef.addValueEventListener(titularListener!!)

        // 3. Escuchar a todos los vecinos pertenecientes a esta red en /usuarios
        val vecRef = db.getReference("usuarios")
        vecinosListenerRef = vecRef
        vecinosListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val locations = mutableListOf<VecinoLocation>()
                val infoList = mutableListOf<VecinoInfo>()
                var titularNombre = _state.value.usuarioNombre
                var titularCorreo = _state.value.usuarioCorreo

                for (child in snapshot.children) {
                    val userNetId = child.child("networkId").getValue(String::class.java) ?: ""
                    val userId = child.key ?: ""
                    val nombre = child.child("nombre").getValue(String::class.java) ?: "Vecino"
                    val correo = child.child("correo").getValue(String::class.java) ?: ""
                    val telefono = child.child("telefono").getValue(String::class.java) ?: ""
                    val lat = child.child("latActual").getValue(Double::class.java) ?: 0.0
                    val lon = child.child("lonActual").getValue(Double::class.java) ?: 0.0

                    if (userNetId == networkId || userId == networkId) {
                        infoList.add(VecinoInfo(userId, nombre, telefono, correo, lat, lon))
                        if (lat != 0.0 && lon != 0.0) {
                            locations.add(VecinoLocation(userId, nombre, lat, lon))
                        }
                        // Priorizar al usuario que vinculó la TV
                        if (!linkedBy.isNullOrEmpty() && userId == linkedBy) {
                            titularNombre = nombre
                            titularCorreo = correo
                        } else if (linkedBy.isNullOrEmpty() && (titularNombre.isEmpty() || titularNombre == "Vecino" || userId == networkId)) {
                            titularNombre = nombre
                            titularCorreo = correo
                        }
                    }
                }
                _state.update { 
                    it.copy(
                        vecinosLocations = locations,
                        vecinosList = infoList,
                        usuarioNombre = if (titularNombre.isNotBlank()) titularNombre else it.usuarioNombre,
                        usuarioCorreo = if (titularCorreo.isNotBlank()) titularCorreo else it.usuarioCorreo,
                        usuarioId = userToListen
                    ) 
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        vecRef.addValueEventListener(vecinosListener!!)
    }

    /**
     * Inicializa y reproduce en bucle la alarma auditiva de emergencia del sistema utilizando [RingtoneManager].
     */
    private fun reproducirAlarma() {
        if (mediaPlayer == null) {
            val context = getApplication<Application>().applicationContext
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer.create(context, uri)
            mediaPlayer?.isLooping = true
        }
        mediaPlayer?.start()
    }

    /**
     * Detiene la reproducción de la sirena auditiva y libera los recursos del [MediaPlayer].
     */
    private fun detenerAlarma() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    /**
     * Silencia la alarma sonora activa a petición del usuario de la TV sin descartar el estado de emergencia visual.
     */
    fun silenciarAlarma() {
        detenerAlarma()
        _state.update { it.copy(isSilenced = true) }
    }

    /**
     * Cierra la sesión activa de la TV, eliminando el registro del nodo `/tvs/{tvId}` en Firebase,
     * deteniendo alarmas y regenerando un nuevo código QR de vinculación.
     */
    fun cerrarSesion() {
        val prefs = getApplication<Application>().getSharedPreferences("CunaSeguraTV", Context.MODE_PRIVATE)
        val tvId = prefs.getString("tvId", null)
        if (tvId != null) {
            FirebaseDatabase.getInstance().getReference("tvs").child(tvId).removeValue()
        }
        detenerAlarma()
        _state.update {
            TvUiState(
                isVinculada = false,
                networkId = "",
                networkNombre = "",
                usuarioNombre = "",
                usuarioCorreo = "",
                mqttConnected = it.mqttConnected,
                qrCode = it.qrCode
            )
        }
        generarQRVinculacion()
    }

    /**
     * Genera un código QR con ZXing codificando el esquema de Deep Link `cunasegura://vincular?tvId={tvId}`.
     * Escucha el nodo `/tvs/{tvId}` en Firebase para completar el emparejamiento cuando el móvil escanea el código.
     */
    private fun generarQRVinculacion() {
        viewModelScope.launch {
            val prefs = getApplication<Application>().getSharedPreferences("CunaSeguraTV", Context.MODE_PRIVATE)
            var tvId = prefs.getString("tvId", null)
            if (tvId == null) {
                tvId = UUID.randomUUID().toString()
                prefs.edit().putString("tvId", tvId).apply()
            }
            
            // Escuchar el nodo /tvs/$tvId en Firebase para detectar networkId y usuario vinculador
            val dbRef = FirebaseDatabase.getInstance().getReference("tvs").child(tvId)
            dbRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val networkId = snapshot.child("networkId").getValue(String::class.java)
                    val linkedBy = snapshot.child("linkedBy").getValue(String::class.java)

                    if (!networkId.isNullOrEmpty()) {
                        simularVinculacionExitosa(networkId, linkedBy)
                    }

                    if (!linkedBy.isNullOrEmpty()) {
                        escucharCambiosRedUsuario(tvId, linkedBy)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    android.util.Log.e("TvMonitorViewModel", "Error en Firebase: ${error.message}")
                }
            })

            val content = "cunasegura://vincular?tvId=$tvId"
            
            try {
                val writer = QRCodeWriter()
                val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
                val width = bitMatrix.width
                val height = bitMatrix.height
                val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
                
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        bmp.setPixel(x, y, if (bitMatrix.get(x, y)) Color.BLACK else Color.WHITE)
                    }
                }
                
                _state.update { it.copy(qrCode = bmp) }
                
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private var usuarioListenerRef: com.google.firebase.database.DatabaseReference? = null
    private var usuarioListener: ValueEventListener? = null

    /**
     * Escucha en tiempo real si el usuario que vinculó esta TV se une o sale de una red vecinal en la app móvil.
     *
     * @param tvId Identificador único de la TV.
     * @param linkedByUid UID del usuario en Firebase.
     */
    private fun escucharCambiosRedUsuario(tvId: String, linkedByUid: String) {
        usuarioListenerRef?.let { ref ->
            usuarioListener?.let { l -> ref.removeEventListener(l) }
        }

        val userNetRef = FirebaseDatabase.getInstance().getReference("usuarios").child(linkedByUid).child("networkId")
        usuarioListenerRef = userNetRef
        usuarioListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val newNetworkId = snapshot.getValue(String::class.java)
                if (!newNetworkId.isNullOrEmpty() && newNetworkId != _state.value.networkId) {
                    android.util.Log.d("TvMonitorViewModel", "🔔 La TV detectó cambio de red vecinal del usuario $linkedByUid a: $newNetworkId")
                    FirebaseDatabase.getInstance().getReference("tvs").child(tvId).child("networkId").setValue(newNetworkId)
                    simularVinculacionExitosa(newNetworkId, linkedByUid)
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        }
        userNetRef.addValueEventListener(usuarioListener!!)
    }

    /**
     * Ejecuta el proceso de vinculación exitosa con la red vecinal especificada.
     * Configura el tópico MQTT, inicia la sincronización de vecinos y actualiza el estado de la UI.
     *
     * @param networkId Identificador de la red vecinal vinculada.
     * @param linkedBy Identificador del usuario que autorizó la vinculación.
     */
    fun simularVinculacionExitosa(networkId: String = "RED-VECINAL-123", linkedBy: String? = null) {
        mqttSubscriber.setNetworkId(networkId)
        escucharVecinosYRedFirebase(networkId, linkedBy)
        _state.update { 
            it.copy(
                isVinculada = true,
                networkId = networkId,
                usuarioId = linkedBy ?: it.usuarioId
            )
        }
    }

    /**
     * Oculta el modal de emergencia SOS a pantalla completa para permitir la interacción directa con el mapa.
     */
    fun descartarModalAlerta() {
        _state.update { it.copy(showAlertModal = false) }
    }

    /**
     * Alterna la visibilidad del diálogo de personalización de colores para marcadores del mapa.
     */
    fun toggleColorPicker() {
        _state.update { it.copy(showColorPicker = !it.showColorPicker) }
    }

    /**
     * Guarda la selección de colores personalizada para marcadores del mapa en [SharedPreferences] y actualiza el estado.
     *
     * @param colorUsuario Color para el titular vinculado.
     * @param colorVecinos Color para otros vecinos de la red.
     * @param colorAlertas Color para alertas de emergencia SOS.
     */
    fun guardarColores(colorUsuario: Int, colorVecinos: Int, colorAlertas: Int) {
        val prefs = getApplication<Application>().getSharedPreferences("mapa_colores", android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .putInt("colorUsuario", colorUsuario)
            .putInt("colorVecinos", colorVecinos)
            .putInt("colorAlertas", colorAlertas)
            .apply()
        _state.update { it.copy(colorUsuario = colorUsuario, colorVecinos = colorVecinos, colorAlertas = colorAlertas, showColorPicker = false) }
    }

    /**
     * Carga los colores personalizados para marcadores del mapa almacenados en [SharedPreferences].
     */
    fun cargarColores() {
        val prefs = getApplication<Application>().getSharedPreferences("mapa_colores", android.content.Context.MODE_PRIVATE)
        val colorU = prefs.getInt("colorUsuario", android.graphics.Color.parseColor("#2196F3"))
        val colorV = prefs.getInt("colorVecinos", android.graphics.Color.parseColor("#4CAF50"))
        val colorA = prefs.getInt("colorAlertas", android.graphics.Color.parseColor("#F44336"))
        _state.update { it.copy(colorUsuario = colorU, colorVecinos = colorV, colorAlertas = colorA) }
    }

    /**
     * Se invoca cuando el ViewModel es destruido. Desconecta el cliente MQTT y detiene la alarma activa.
     */
    override fun onCleared() {
        super.onCleared()
        mqttSubscriber.disconnect()
        detenerAlarma()
    }
}
```

---

## FASE 7: Pantallas e Interfaces de Usuario de Smart TV (Compose for TV & D-Pad)

### Paso 7.1: Pantalla de Vinculación por Código QR (`VinculacionScreen.kt`)

> 📋 **INSTRUCCIÓN:** Crea el archivo `cunaseguratv/src/main/java/mx/edu/utng/cunaseguratv/presentation/screens/VinculacionScreen.kt`:

```kotlin
package mx.edu.utng.cunaseguratv.presentation.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import mx.edu.utng.cunaseguratv.presentation.TvUiState

/**
 * Pantalla de vinculación inicial para Smart TV mediante código QR.
 *
 * Muestra el código QR generado dinámicamente que codifica el identificador de la televisión.
 * Los usuarios pueden escanear este código desde la app móvil de Cuna Segura para asociar
 * la Smart TV a su red vecinal sin requerir ingreso manual de credenciales con el control remoto.
 *
 * @param state Estado reactivo actual de la interfaz de usuario [TvUiState].
 * @param onSimularVinculacion Lambda callback invocado para propósitos de prueba o simulación en emulador.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
@Composable
fun VinculacionScreen(
    state: TvUiState,
    onSimularVinculacion: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "🛡️ VINCULAR CUNA SEGURA TV",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Escanea este código QR desde la aplicación móvil en tu Smartphone\npara asociar esta pantalla a tu Red Vecinal.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Renderizar Bitmap del QR
            if (state.qrCode != null) {
                Image(
                    bitmap = state.qrCode.asImageBitmap(),
                    contentDescription = "Código QR de Vinculación",
                    modifier = Modifier
                        .size(280.dp)
                        .background(androidx.compose.ui.graphics.Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                        .padding(16.dp)
                )
            } else {
                Text("Generando código QR...")
            }
            
            // Botón enfocado para simulación en entorno de emulador sin cámara
            Button(onClick = onSimularVinculacion) {
                Text("Simular Vinculación Exitosa")
            }
        }
    }
}
```

---

### Paso 7.2: Modal Flotante de Emergencia Crítica SOS (`AlertModalOverlay.kt`)

> 📋 **INSTRUCCIÓN:** Crea el archivo `cunaseguratv/src/main/java/mx/edu/utng/cunaseguratv/presentation/screens/AlertModalOverlay.kt`:

```kotlin
package mx.edu.utng.cunaseguratv.presentation.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import mx.edu.utng.cunaseguratv.mqtt.AlertaMqttMessage

/**
 * Capa emergente superpuesta (Modal Overlay) desplegada a pantalla completa ante una alerta SOS crítica.
 *
 * Presenta una animación de parpadeo estroboscópico de color rojo de alto contraste para captar de inmediato
 * la atención de los residentes u operadores de la televisión en la habitación. Despliega el nombre del vecino,
 * su identificador, el nivel de toques físicos y sus coordenadas GPS exactas.
 *
 * Provee acciones navegables por D-Pad para silenciar la alarma auditiva y descartar el diálogo para navegar
 * directamente al mapa del incidente.
 *
 * @param alerta Datos del mensaje de alerta MQTT [AlertaMqttMessage] que detonó la emergencia.
 * @param onDescartar Lambda callback para cerrar el modal y enfocar el mapa.
 * @param onSilenciar Lambda callback para silenciar el tono de sirena auditivo.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
@Composable
fun AlertModalOverlay(
    alerta: AlertaMqttMessage,
    onDescartar: () -> Unit,
    onSilenciar: () -> Unit
) {
    // Animación de parpadeo rojo estroboscópico para máxima visibilidad en pantallas 1080p/4K
    val infiniteTransition = rememberInfiniteTransition()
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.error.copy(alpha = alphaAnim)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .background(MaterialTheme.colorScheme.background, shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp))
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "🚨 ¡EMERGENCIA VECINAL! 🚨",
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "El vecino ${alerta.nombreUsuario} (ID: ${alerta.usuarioId}) activó una alerta SOS.",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Text(
                text = "Nivel de Alerta: ${alerta.nivelAlerta} toques",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error
            )
            
            Text(
                text = "Ubicación GPS: ${alerta.latitud}, ${alerta.longitud}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.padding(top = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Button(onClick = onDescartar) {
                    Text("VER EN MAPA")
                }
                
                Button(
                    onClick = {
                        onSilenciar()
                        onDescartar()
                    },
                    colors = ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("SILENCIAR Y VER MAPA")
                }
            }
        }
    }
}
```

---

### Paso 7.3: Dashboard Central, Mapa OSMDroid y Controles D-Pad (`DashboardScreen.kt`)

> 📋 **INSTRUCCIÓN:** Crea el archivo `cunaseguratv/src/main/java/mx/edu/utng/cunaseguratv/presentation/screens/DashboardScreen.kt`:

```kotlin
package mx.edu.utng.cunaseguratv.presentation.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.Card
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import mx.edu.utng.cunaseguratv.data.AlertaTV
import mx.edu.utng.cunaseguratv.presentation.TvUiState
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

/**
 * Pantalla principal del Dashboard de monitoreo para Smart TV.
 *
 * Ofrece una vista cinemática dividida en dos secciones principales:
 * 1. **Panel Lateral Dinámico (40%)**: Muestra información de la red vecinal, titular vinculado,
 *    lista de vecinos en tiempo real (activos/offline) y el historial de últimas alertas registradas.
 *    Es completamente animado y colapsable para expandir el mapa al 100% del ancho de pantalla.
 * 2. **Mapa Comunitario Interactivo (60% / 100%)**: Renderiza mapas de OpenStreetMap mediante OSMDroid (TileSource MAPNIK).
 *    Dibuja marcadores circulares coloreados para el usuario actual, otros vecinos y alertas SOS activas.
 *    Incluye controles en pantalla navegables mediante la cruceta direccional (D-Pad) del control remoto:
 *    Zoom In, Zoom Out, Desplazamiento cardinal (▲, ▼, ◄, ►) y botón de Recentrado táctico (🎯).
 *
 * @param state Estado reactivo de la interfaz [TvUiState].
 * @param onSilenciar Callback para silenciar la sirena auditiva activa.
 * @param onCerrarSesion Callback para desvincular la TV y regresar al flujo de código QR.
 * @param onToggleColorPicker Callback para abrir o cerrar el panel de selección de colores.
 * @param onGuardarColores Callback para persistir los colores personalizados de los marcadores.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
@Composable
fun DashboardScreen(
    state: TvUiState,
    onSilenciar: () -> Unit,
    onCerrarSesion: () -> Unit,
    onToggleColorPicker: () -> Unit = {},
    onGuardarColores: (Int, Int, Int) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var isSidebarExpanded by remember { mutableStateOf(true) }
    
    val conf = Configuration.getInstance()
    conf.load(context, androidx.preference.PreferenceManager.getDefaultSharedPreferences(context))
    conf.userAgentValue = "CunaSeguraTV/1.0 (${context.packageName})"
    conf.osmdroidBasePath = context.cacheDir
    conf.osmdroidTileCache = java.io.File(context.cacheDir, "osmdroid")

    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
            isHorizontalMapRepetitionEnabled = false
            isVerticalMapRepetitionEnabled = false
            isFocusable = false
            isFocusableInTouchMode = false
            descendantFocusability = android.view.ViewGroup.FOCUS_BLOCK_DESCENDANTS
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(21.1561, -100.9325))
            onResume()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // --- NAVBAR (ENCABEZADO) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🛡️ CUNA SEGURA TV",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Black
                )
                
                Button(
                    onClick = { isSidebarExpanded = !isSidebarExpanded },
                    colors = ButtonDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(if (isSidebarExpanded) "Ocultar Panel" else "Mostrar Panel")
                }
            }

            // Datos de sesión activa
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "👤 Sesión: ${state.usuarioNombre.ifEmpty { "Usuario Vinculado" }}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "🏠 Red: ${state.networkNombre.ifEmpty { state.networkId.take(12) }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(
                                color = if (state.mqttConnected) androidx.compose.ui.graphics.Color.Green else MaterialTheme.colorScheme.error,
                                shape = androidx.compose.foundation.shape.CircleShape
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (state.mqttConnected) "Conectado" else "Desconectado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onCerrarSesion,
                    colors = ButtonDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError
                    )
                ) {
                    Text("Cerrar Sesión TV", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                Button(
                    onClick = onToggleColorPicker,
                    colors = ButtonDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.tertiary,
                        contentColor = MaterialTheme.colorScheme.onTertiary
                    )
                ) {
                    Text("🎨 Colores", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // --- CONTENIDO PRINCIPAL (MAPA + PANEL LATERAL) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // --- SIDEBAR LATERAL ---
            androidx.compose.animation.AnimatedVisibility(
                visible = isSidebarExpanded,
                modifier = Modifier.weight(0.4f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(start = 24.dp, top = 16.dp, bottom = 24.dp, end = 12.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Card para Red Vecinal e información del usuario
                    Card(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.tv.material3.CardDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "RED VECINAL VINCULADA",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = state.networkNombre.ifEmpty { state.networkId.ifEmpty { "Cargando..." } },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Titular: ${state.usuarioNombre.ifEmpty { "Vecino" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (state.usuarioCorreo.isNotEmpty()) {
                                Text(
                                    text = state.usuarioCorreo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }

                    // Card para Lista de Vecinos
                    Card(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.tv.material3.CardDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "VECINOS EN ESTA RED",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${state.vecinosList.size} registrados",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))

                            if (state.vecinosList.isEmpty()) {
                                Text(
                                    text = "Esperando datos de vecinos...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    state.vecinosList.forEach { vecino ->
                                        val tieneGps = vecino.lat != 0.0 && vecino.lon != 0.0
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(
                                                    MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                                    RoundedCornerShape(8.dp)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = vecino.nombre,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                if (vecino.telefono.isNotEmpty()) {
                                                    Text(
                                                        text = "📞 ${vecino.telefono}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = if (tieneGps) "🟢 Activo" else "⚪ Sin GPS",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (tieneGps) Color(0xFF4CAF50) else Color.Gray,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Card para Historial de Alertas Recientes
                    Card(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = androidx.tv.material3.CardDefaults.colors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "HISTORIAL DE ALERTAS",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (state.alertasRecientes.isEmpty()) {
                                Text(
                                    text = "No hay alertas registradas en este período.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    state.alertasRecientes.take(5).forEach { alerta ->
                                        AlertaHistoryItem(alerta, state)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- CONTENEDOR DEL MAPA Y CONTROLES D-PAD ---
            Box(
                modifier = Modifier
                    .weight(if (isSidebarExpanded) 0.6f else 1.0f)
                    .fillMaxHeight()
                    .padding(end = 24.dp, top = 16.dp, bottom = 24.dp, start = if (isSidebarExpanded) 0.dp else 24.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
            ) {
                // Renderizar MapView nativo con marcadores actualizados
                AndroidView(
                    factory = { mapView },
                    modifier = Modifier.fillMaxSize(),
                    update = { map ->
                        map.overlays.clear()

                        // 1. Marcador del titular vinculado
                        val titularVecino = state.vecinosLocations.firstOrNull { it.id == state.usuarioId }
                        if (titularVecino != null) {
                            val marker = Marker(map).apply {
                                position = GeoPoint(titularVecino.lat, titularVecino.lon)
                                title = "⭐ Mi Ubicación (${titularVecino.nombre})"
                                icon = createColoredMarker(context, state.colorUsuario, 42)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            }
                            map.overlays.add(marker)
                        }

                        // 2. Marcadores de otros vecinos de la red
                        state.vecinosLocations.filter { it.id != state.usuarioId }.forEach { vecino ->
                            val marker = Marker(map).apply {
                                position = GeoPoint(vecino.lat, vecino.lon)
                                title = "👤 Vecino: ${vecino.nombre}"
                                icon = createColoredMarker(context, state.colorVecinos, 36)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            }
                            map.overlays.add(marker)
                        }

                        // 3. Marcador de alerta activa (si existe)
                        state.alertaActiva?.let { alerta ->
                            val marker = Marker(map).apply {
                                position = GeoPoint(alerta.latitud, alerta.longitud)
                                title = "🚨 ALERTA: ${alerta.nombreUsuario}"
                                icon = createColoredMarker(context, state.colorAlertas, 50)
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            }
                            map.overlays.add(marker)
                            map.controller.animateTo(GeoPoint(alerta.latitud, alerta.longitud))
                        }

                        map.invalidate()
                    }
                )

                // --- CONTROLES EN PANTALLA NAVEGABLES POR D-PAD ---
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        // Zoom In
                        Button(
                            onClick = { mapView.controller.zoomIn() },
                            colors = ButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("➕ Zoom In", fontWeight = FontWeight.Bold)
                        }

                        // Zoom Out
                        Button(
                            onClick = { mapView.controller.zoomOut() },
                            colors = ButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        ) {
                            Text("➖ Zoom Out", fontWeight = FontWeight.Bold)
                        }

                        // Recentrar en la ubicación del usuario o alerta
                        Button(
                            onClick = {
                                val target = state.alertaActiva?.let { GeoPoint(it.latitud, it.longitud) }
                                    ?: state.vecinosLocations.firstOrNull { it.id == state.usuarioId }?.let { GeoPoint(it.lat, it.lon) }
                                    ?: GeoPoint(21.1561, -100.9325)
                                mapView.controller.animateTo(target)
                                mapView.controller.setZoom(16.0)
                            },
                            colors = ButtonDefaults.colors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text("🎯 Recentrar", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // Diálogo Modal de Selección de Colores
    if (state.showColorPicker) {
        ColorPickerDialog(
            colorUsuario = state.colorUsuario,
            colorVecinos = state.colorVecinos,
            colorAlertas = state.colorAlertas,
            onGuardar = { u, v, a -> onGuardarColores(u, v, a) },
            onCerrar = onToggleColorPicker
        )
    }
}

/**
 * Elemento de lista para renderizar un registro en el historial de alertas recientes de la Smart TV.
 */
@Composable
fun AlertaHistoryItem(alerta: AlertaTV, state: TvUiState) {
    Card(
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        colors = androidx.tv.material3.CardDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🚨 ${alerta.nombreUsuario}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (alerta.estado == "activa") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = alerta.estado.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (alerta.estado == "activa") MaterialTheme.colorScheme.error else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            val sdf = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
            val fecha = sdf.format(java.util.Date(alerta.creadoEn))
            Text(
                text = "Fecha: $fecha",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Genera un icono [BitmapDrawable] circular personalizado para superponer en marcadores de OSMDroid.
 */
fun createColoredMarker(context: android.content.Context, color: Int, sizeDp: Int = 36): BitmapDrawable {
    val density = context.resources.displayMetrics.density
    val sizePx = (sizeDp * density).toInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = color
        style = Paint.Style.FILL
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - 2, paint)

    val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = android.graphics.Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 3 * density
    }
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - 2, strokePaint)

    return BitmapDrawable(context.resources, bitmap)
}

/** Paleta de colores predefinidos disponibles para asignación en el diálogo selector de colores. */
private val COLORES_DISPONIBLES = listOf(
    0xFF2196F3.toInt() to "Azul",
    0xFF4CAF50.toInt() to "Verde",
    0xFFF44336.toInt() to "Rojo",
    0xFFFF9800.toInt() to "Naranja",
    0xFF9C27B0.toInt() to "Morado",
    0xFFE91E63.toInt() to "Rosa",
    0xFF00BCD4.toInt() to "Cian",
    0xFFFFEB3B.toInt() to "Amarillo",
    0xFF795548.toInt() to "Café",
    0xFF607D8B.toInt() to "Gris",
    0xFFFFFFFF.toInt() to "Blanco",
    0xFF212121.toInt() to "Negro"
)

/**
 * Diálogo modal para la configuración y personalización de los colores de los marcadores del mapa.
 */
@Composable
fun ColorPickerDialog(
    colorUsuario: Int,
    colorVecinos: Int,
    colorAlertas: Int,
    onGuardar: (Int, Int, Int) -> Unit,
    onCerrar: () -> Unit
) {
    var selUsuario by remember { mutableStateOf(colorUsuario) }
    var selVecinos by remember { mutableStateOf(colorVecinos) }
    var selAlertas by remember { mutableStateOf(colorAlertas) }

    Dialog(onDismissRequest = onCerrar) {
        Card(
            onClick = {},
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight()
                .padding(16.dp),
            colors = androidx.tv.material3.CardDefaults.colors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🎨 Personalizar Colores de Marcadores",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )

                ColorSelectorRow("⭐ Color Mi Ubicación (Titular):", selUsuario) { selUsuario = it }
                ColorSelectorRow("👥 Color Vecinos de la Red:", selVecinos) { selVecinos = it }
                ColorSelectorRow("🚨 Color Alertas SOS Emergencia:", selAlertas) { selAlertas = it }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onCerrar,
                        colors = ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text("Cancelar")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = { onGuardar(selUsuario, selVecinos, selAlertas) },
                        colors = ButtonDefaults.colors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Guardar Colores", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Fila horizontal con selector visual de colores basada en círculos con feedback de selección.
 */
@Composable
fun ColorSelectorRow(label: String, selectedColor: Int, onSelect: (Int) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            COLORES_DISPONIBLES.forEach { (colorVal, _) ->
                val isSelected = colorVal == selectedColor
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(colorVal))
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color.White else Color.Gray,
                            shape = CircleShape
                        )
                        .clickable { onSelect(colorVal) }
                )
            }
        }
    }
}
```

---

## FASE 8: Host de la Actividad Principal y Enrutamiento

### Paso 8.1: Activity Principal de Android TV (`TVMainActivity.kt`)

> 📋 **INSTRUCCIÓN:** Crea el archivo `cunaseguratv/src/main/java/mx/edu/utng/cunaseguratv/TVMainActivity.kt`:

```kotlin
package mx.edu.utng.cunaseguratv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utng.cunaseguratv.presentation.TvMonitorViewModel
import mx.edu.utng.cunaseguratv.presentation.screens.AlertModalOverlay
import mx.edu.utng.cunaseguratv.presentation.screens.DashboardScreen
import mx.edu.utng.cunaseguratv.presentation.screens.VinculacionScreen
import mx.edu.utng.cunaseguratv.presentation.theme.CunaSeguraTvTheme

/**
 * Actividad principal del módulo Android TV para Cuna Segura.
 *
 * Configurada en orientación fija horizontal (Landscape) y compatible con Leanback Launcher.
 * Aloja el árbol de composición de Jetpack Compose for TV y coordina el flujo de pantallas:
 * 1. [VinculacionScreen]: Cuando la TV no ha sido vinculada mediante código QR a una red vecinal.
 * 2. [DashboardScreen]: Vista cinemática de monitoreo cuando la TV ya está vinculada a una red.
 * 3. [AlertModalOverlay]: Capa emergente superpuesta que se despliega automáticamente ante una alerta SOS crítica.
 *
 * @author Cuna Segura Team
 * @version 1.0
 */
class TVMainActivity : ComponentActivity() {
    
    /**
     * Punto de entrada del ciclo de vida de la actividad.
     * Establece el contenido visual declarativo utilizando [CunaSeguraTvTheme] y
     * suscribe el estado de la UI desde [TvMonitorViewModel].
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            CunaSeguraTvTheme {
                val viewModel: TvMonitorViewModel = viewModel()
                val state by viewModel.state.collectAsState()

                Box(modifier = Modifier.fillMaxSize()) {
                    // Si la TV no está vinculada a una red vecinal, muestra el código QR de emparejamiento
                    if (!state.isVinculada) {
                        VinculacionScreen(
                            state = state,
                            onSimularVinculacion = { viewModel.simularVinculacionExitosa() }
                        )
                    } else {
                        // Dashboard de monitoreo principal con mapa y panel lateral dinámico
                        DashboardScreen(
                            state = state,
                            onSilenciar = { viewModel.silenciarAlarma() },
                            onCerrarSesion = { viewModel.cerrarSesion() },
                            onToggleColorPicker = { viewModel.toggleColorPicker() },
                            onGuardarColores = { u, v, a -> viewModel.guardarColores(u, v, a) }
                        )
                    }
                    
                    // Modal de emergencia de alta prioridad superpuesto sobre cualquier vista activa
                    if (state.showAlertModal && state.alertaActiva != null) {
                        AlertModalOverlay(
                            alerta = state.alertaActiva!!,
                            onDescartar = { viewModel.descartarModalAlerta() },
                            onSilenciar = { viewModel.silenciarAlarma() }
                        )
                    }
                }
            }
        }
    }
}
```

---

## FASE 9: Compilación, Despliegue y Verificación Integral del Módulo Smart TV

### Paso 9.1: Compilación del Módulo con Gradle Wrapper

Para compilar de forma aislada el APK de Android TV:

```bash
# Limpiar caché previo
.\gradlew :cunaseguratv:clean

# Compilar APK Debug
.\gradlew :cunaseguratv:assembleDebug
```

El artefacto resultante se genera en:
`cunaseguratv/build/outputs/apk/debug/cunaseguratv-debug.apk`

---

### Paso 9.2: Despliegue e Instalación en Dispositivo o Emulador Android TV con ADB

Para instalar el módulo en un emulador Android TV (1080p o 4K) o televisión física mediante conexión inalámbrica ADB:

```bash
# Conectar por IP a Android TV física (modo desarrollador activado)
adb connect 192.168.1.100:5555

# Instalar APK compilado
.\gradlew :cunaseguratv:installDebug

# O mediante ADB directo
adb install -r cunaseguratv/build/outputs/apk/debug/cunaseguratv-debug.apk
```

---

### Paso 9.3: Matriz de Pruebas de Aceptación End-to-End y Flujo de Verificación

| # | Caso de Prueba | Acción en el Sistema | Resultado Esperado |
|---|---|---|---|
| 1 | **Arranque y Vinculación Inicial** | Abrir la app en Android TV | Muestra `VinculacionScreen` con código QR generado por ZXing (`cunasegura://vincular?tvId=...`). |
| 2 | **Emparejamiento QR desde Móvil** | Escanear QR con el módulo móvil | La TV transiciona automáticamente a `DashboardScreen`, mostrando el nombre de la red y del titular. |
| 3 | **Carga Cartográfica OSMDroid** | Renderizado de OpenStreetMap | Descarga teselas de OSM sin API Key y dibuja marcadores circulares del usuario y vecinos. |
| 4 | **Navegación con Control Remoto D-Pad** | Usar flechas de control en TV | Enfoque (`focus`) claro en botones Zoom In, Zoom Out, Recentrar y Ocultar/Mostrar Panel. |
| 5 | **Recepción de Alerta SOS por MQTT** | Disparar pánico desde reloj/móvil | `AlertModalOverlay` estalla en pantalla con parpadeo rojo estroboscópico y sirena en `MediaPlayer`. |
| 6 | **Silenciado de Alarma** | Presionar "Silenciar y Ver Mapa" | Detiene el audio de la sirena y centra la cámara del mapa en las coordenadas del vecino emisor. |
| 7 | **Personalización de Colores** | Presionar botón "🎨 Colores" | Abre `ColorPickerDialog` permitiendo cambiar colores de marcadores y persistiendo en `SharedPreferences`. |
| 8 | **Cierre de Sesión** | Presionar "Cerrar Sesión TV" | Elimina la TV de `/tvs/{tvId}` en Firebase y regresa a `VinculacionScreen`. |
