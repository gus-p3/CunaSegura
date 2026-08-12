# Guía Paso a Paso: Construyendo el Módulo Móvil de Cuna Segura

Esta guía documenta y desglosa paso a paso la arquitectura, configuración y construcción completa del módulo **Móvil (Android Smartphone)** de **Cuna Segura - Dolores Hidalgo**, explicando las decisiones técnicas, patrones de diseño y bloques de código esenciales para un proyecto profesional en **Kotlin 2.1** y **Jetpack Compose (Material 3)**.

---

## Objetivo de Esta Guía

Al estudiar y seguir esta guía, comprenderás:

1. Cómo estructurar un proyecto Android profesional moderno con **Kotlin 2.1** y **Jetpack Compose (Material 3)** bajo los principios de **Clean Architecture** y **MVVM (Model-View-ViewModel)** con flujo unidireccional de datos (*Unidirectional Data Flow - UDF*) y estados reactivos (`StateFlow`).
2. Cómo implementar persistencia local robusta y reactiva con **SQLite Room** (Entidades `UsuarioEntity`, `AlertaEntity`, `ContactoEmergenciaEntity`, `ConfiguracionToqueEntity`, DAOs y Singleton `AppDatabase` con pre-población de admin).
3. Cómo integrar servicios de hardware avanzados: **OpenStreetMap (osmdroid)**, geolocalización en primer plano (**Foreground Services** con `FusedLocationProviderClient`) y notificaciones persistentes de alta prioridad con canal sonoro de alarma vecinal.
4. Cómo orquestar la comunicación multicanal y sincronización IoT: **Wear OS Data Layer RPC Listener** (`PhoneWearableService`) para activación de pánico desde Smartwatch, despacho automático de SMS con geolocalización, broker **MQTT TLS HiveMQ Cloud** (`MqttPublisher`) para activación de Smart TVs comunitarias y backend en la nube con **Firebase Authentication** y **Firebase Realtime Database**.
5. Cómo implementar módulos de administración global, censo de usuarios, estadísticas vecinales y políticas de seguridad comunitaria.

---

## FASE 1: Configuración Inicial del Entorno y Build System

### Paso 1.1: Configurar el Catálogo de Versiones (`gradle/libs.versions.toml`)

El catálogo de dependencias centraliza las versiones y librerías utilizadas en todos los submódulos del proyecto.

> 📋 **INSTRUCCIÓN:** Copia las dependencias de `gradle/libs.versions.toml`:
```toml
[versions]
googleServices = "4.4.2"
firebaseBom = "33.1.2"
agp = "9.2.1"
coreKtx = "1.10.1"
junit = "4.13.2"
junitVersion = "1.1.5"
espressoCore = "3.5.1"
lifecycleRuntimeKtx = "2.6.1"
activityCompose = "1.13.0"
kotlin = "2.4.0"
composeBom = "2024.09.00"
playServicesWearable = "20.0.1"
composeMaterial3 = "1.5.6"
composeFoundation = "1.5.6"
composeUiTooling = "1.5.6"
wearToolingPreview = "1.0.0"
coreSplashscreen = "1.2.0"
wearCompose = "1.3.1"
datastore = "1.1.1"
healthServices = "1.1.0-alpha03"
kotlinxCoroutinesTest = "1.8.1"
room = "2.7.0-alpha13"
coreKtxVersion = "1.19.0"
ksp = "2.3.9"
kotlinxCoroutines = "1.8.1"
navigationCompose = "2.7.7"
lifecycleViewmodelCompose = "2.7.0"
material3 = "1.2.1"
mapsCompose = "4.3.3"
playServicesMaps = "18.2.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
junit = { group = "junit", name = "junit", version.ref = "junit" }
androidx-junit = { group = "androidx.test.ext", name = "junit", version.ref = "junitVersion" }
androidx-espresso-core = { group = "androidx.test.espresso", name = "espresso-core", version.ref = "espressoCore" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-lifecycle-viewmodel-ktx = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }
androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-wear-compose-material = { group = "androidx.wear.compose", name = "compose-material", version.ref = "wearCompose" }
androidx-wear-compose-foundation = { group = "androidx.wear.compose", name = "compose-foundation", version.ref = "wearCompose" }
androidx-wear-compose-navigation = { group = "androidx.wear.compose", name = "compose-navigation", version.ref = "wearCompose" }
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }
androidx-health-services-client = { group = "androidx.health", name = "health-services-client", version.ref = "healthServices" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "kotlinxCoroutinesTest" }
play-services-wearable = { group = "com.google.android.gms", name = "play-services-wearable", version.ref = "playServicesWearable" }
compose-material3 = { group = "androidx.wear.compose", name = "compose-material3", version.ref = "composeMaterial3" }
androidx-compose-foundation = { group = "androidx.wear.compose", name = "compose-foundation", version.ref = "composeFoundation" }
compose-ui-tooling = { group = "androidx.wear.compose", name = "compose-ui-tooling", version.ref = "composeUiTooling" }
androidx-wear-tooling-preview = { group = "androidx.wear", name = "wear-tooling-preview", version.ref = "wearToolingPreview" }
androidx-core-splashscreen = { group = "androidx.core", name = "core-splashscreen", version.ref = "coreSplashscreen" }
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtxVersion" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "kotlinxCoroutines" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleViewmodelCompose" }
maps-compose = { group = "com.google.maps.android", name = "maps-compose", version.ref = "mapsCompose" }
play-services-maps = { group = "com.google.android.gms", name = "play-services-maps", version.ref = "playServicesMaps" }
firebase-bom = { group = "com.google.firebase", name = "firebase-bom", version.ref = "firebaseBom" }
firebase-analytics = { group = "com.google.firebase", name = "firebase-analytics" }
firebase-database = { group = "com.google.firebase", name = "firebase-database" }
firebase-messaging = { group = "com.google.firebase", name = "firebase-messaging" }
firebase-auth = { group = "com.google.firebase", name = "firebase-auth" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
legacy-kapt = { id = "com.google.devtools.ksp", version.ref = "ksp" }
google-services = { id = "com.google.gms.google-services", version.ref = "googleServices" }
```

> **CONCEPTO CLAVE:** El archivo `libs.versions.toml` permite versionar dependencias de manera determinista, garantizando que el compilador KSP para Room, Kotlin Coroutines y Jetpack Compose permanezcan perfectamente sincronizados.

---

### Paso 1.2: Configurar `app/build.gradle.kts`

El módulo `:app` configura el compilador KSP, las credenciales seguras de HiveMQ desde `local.properties`, dependencias de Room, Firebase, OpenStreetMap (osmdroid), ZXing para escaneo QR, Paho MQTT Client y gráficos con Vico.

> 📋 **INSTRUCCIÓN:** Copia la configuración del build script de `app/build.gradle.kts`:
```kotlin
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.0.21"
    alias(libs.plugins.google.services)
}

android {
    namespace = "mx.edu.utng.cunasegura"
    compileSdk = 36

    defaultConfig {
        applicationId = "mx.edu.utng.cunasegura"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
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
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // Room SQLite
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Preferences DataStore
    implementation(libs.androidx.datastore.preferences)

    // ZXing for QR Code scanning
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
    
    // Navigation Compose
    implementation(libs.androidx.navigation.compose)

    // ViewModel + Compose
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)

    // OpenStreetMap (osmdroid)
    implementation("org.osmdroid:osmdroid-android:6.1.18")

    // Firebase Suite
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.database)
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.auth)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
    implementation("com.google.android.gms:play-services-wearable:18.1.0")
    implementation("com.google.android.gms:play-services-location:21.2.0")

    // Eclipse Paho MQTT
    implementation("org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5")
    implementation("org.eclipse.paho:org.eclipse.paho.android.service:1.1.1")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")

    // Vico Charting Library para estadísticas
    implementation("com.patrykandpatrick.vico:compose:1.15.0")
    implementation("com.patrykandpatrick.vico:compose-m3:1.15.0")

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
```

---

### Paso 1.3: Configurar `app/src/main/AndroidManifest.xml`

Declara los permisos críticos de ubicación en primer y segundo plano, llamadas telefónicas 911, envío de SMS de emergencia, notificaciones, servicios en primer plano y declaración de receptores y servicios.

> 📋 **INSTRUCCIÓN:** Copia el manifest de `app/src/main/AndroidManifest.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Permisos de red: comunicación con Firebase Realtime DB y Broker MQTT TLS -->
    <uses-permission android:name="android.permission.INTERNET" />

    <!-- Permisos de ubicación precisa y en segundo plano para el rastreo del vecino -->
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_BACKGROUND_LOCATION" />

    <!-- Permiso de llamada telefónica directa (911 y marcador en mapa) -->
    <uses-permission android:name="android.permission.CALL_PHONE" />
    
    <!-- Permiso de cámara para escaneo de códigos QR de redes vecinales -->
    <uses-permission android:name="android.permission.CAMERA" />

    <!-- Permiso para enviar SMS de auxilio con geolocalización desde el reloj -->
    <uses-permission android:name="android.permission.SEND_SMS" />

    <!-- Permiso para notificaciones push de alta prioridad (Android 13+) -->
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <!-- Permisos Bluetooth y Foreground Service para enlace con el reloj Wear OS -->
    <uses-permission android:name="android.permission.BLUETOOTH" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
    <uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
    <uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_CONNECTED_DEVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION" />

    <application
        android:allowBackup="true"
        android:dataExtractionRules="@xml/data_extraction_rules"
        android:fullBackupContent="@xml/backup_rules"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.CunaSegura">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.CunaSegura"
            android:windowSoftInputMode="adjustResize">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <!-- Servicio receptor de eventos RPC enviados desde el Smartwatch Wear OS -->
        <service
            android:name=".data.wear.PhoneWearableService"
            android:exported="true">
            <intent-filter>
                <action android:name="com.google.android.gms.wearable.MESSAGE_RECEIVED" />
                <data
                    android:host="*"
                    android:pathPrefix="/cunasegura"
                    android:scheme="wear" />
            </intent-filter>
        </service>

        <!-- Servicio en primer plano: rastreo GPS continuo y escucha de alertas comunitarias -->
        <service
            android:name=".data.location.LocationTrackerService"
            android:foregroundServiceType="location"
            android:exported="false" />
    </application>

</manifest>
```

---

## FASE 2: Inyección de Dependencias, Tipografía y Preferencias

### Paso 2.1: Contenedor Centralizado de Dependencias (`di/AppModule.kt`)

`AppModule` provee instancias únicas (Singletons) de la base de datos Room, repositorios de datos, cliente MQTT y casos de uso del dominio, garantizando desacoplamiento e inversión de control.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/di/AppModule.kt`:
```kotlin
package mx.edu.utng.cunasegura.di

import android.content.Context
import mx.edu.utng.cunasegura.data.local.db.AppDatabase
import mx.edu.utng.cunasegura.data.repository.AlertaRepositoryImpl
import mx.edu.utng.cunasegura.data.repository.ContactoRepositoryImpl
import mx.edu.utng.cunasegura.data.repository.IContactoRepository
import mx.edu.utng.cunasegura.data.repository.NetworkRepositoryImpl
import mx.edu.utng.cunasegura.data.repository.UsuarioRepositoryImpl
import mx.edu.utng.cunasegura.domain.repository.IAlertaRepository
import mx.edu.utng.cunasegura.domain.repository.INetworkRepository
import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository
import mx.edu.utng.cunasegura.domain.usecase.*
import mx.edu.utng.cunasegura.mqtt.MqttPublisher

/**
 * Contenedor de Inyección de Dependencias manual para el módulo móvil.
 *
 * Centraliza la creación y provisión de:
 * - Base de datos Room ([AppDatabase])
 * - Publicador MQTT ([MqttPublisher])
 * - Repositorios de datos ([IUsuarioRepository], [IAlertaRepository], [IContactoRepository], [INetworkRepository])
 * - Casos de uso de la capa de dominio
 */
object AppModule {

    @Volatile
    private var database: AppDatabase? = null

    @Volatile
    private var mqttPublisher: MqttPublisher? = null

    /** Provee la instancia Singleton de [AppDatabase]. */
    fun provideDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: AppDatabase.getInstance(context).also { database = it }
        }
    }

    /** Provee la instancia Singleton de [MqttPublisher]. */
    fun provideMqttPublisher(context: Context): MqttPublisher {
        return mqttPublisher ?: synchronized(this) {
            mqttPublisher ?: MqttPublisher(context.applicationContext).also { mqttPublisher = it }
        }
    }

    // --- Repositorios ---
    fun provideUsuarioRepository(context: Context): IUsuarioRepository {
        return UsuarioRepositoryImpl(provideDatabase(context).usuarioDao())
    }

    fun provideAlertaRepository(context: Context): IAlertaRepository {
        return AlertaRepositoryImpl(
            provideDatabase(context).alertaDao(),
            provideMqttPublisher(context),
            context
        )
    }

    fun provideContactoRepository(context: Context): IContactoRepository {
        return ContactoRepositoryImpl(provideDatabase(context).contactoDao())
    }

    fun provideNetworkRepository(context: Context): INetworkRepository {
        return NetworkRepositoryImpl()
    }

    // --- Casos de Uso ---
    fun provideValidarLoginUseCase(context: Context) = ValidarLoginUseCase(provideUsuarioRepository(context))
    fun provideValidarAdminUseCase(context: Context) = ValidarAdminUseCase(provideUsuarioRepository(context))
    fun provideObtenerUsuarioUseCase(context: Context) = ObtenerUsuarioUseCase(provideUsuarioRepository(context))
    fun provideGuardarUsuarioUseCase(context: Context) = GuardarUsuarioUseCase(provideUsuarioRepository(context))
    fun provideLimpiarSesionLocalUseCase(context: Context) = LimpiarSesionLocalUseCase(provideUsuarioRepository(context))
    fun provideObtenerUsuarioActualUseCase(context: Context) = ObtenerUsuarioActualUseCase(provideUsuarioRepository(context))
    fun provideActivarAlertaUseCase(context: Context) = ActivarAlertaUseCase(provideAlertaRepository(context))
    fun provideCancelarAlertaUseCase(context: Context) = CancelarAlertaUseCase(provideAlertaRepository(context))
    fun provideObtenerContactosUseCase(context: Context) = ObtenerContactosUseCase(provideContactoRepository(context))
    fun provideAgregarContactoUseCase(context: Context) = AgregarContactoUseCase(provideContactoRepository(context))
    fun provideEliminarContactoUseCase(context: Context) = EliminarContactoUseCase(provideContactoRepository(context))
}
```

---

### Paso 2.2: Configuración Tipográfica (`ui/theme/Type.kt`)

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/ui/theme/Type.kt`:
```kotlin
package mx.edu.utng.cunasegura.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Jerarquía tipográfica Material 3 para la aplicación móvil Cuna Segura.
 */
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
)
```

---

### Paso 2.3: Gestor de Preferencias Locales (`data/local/prefs/PreferencesManager.kt`)

`PreferencesManager` encapsula la persistencia liviana con `SharedPreferences` para almacenar banderas de vinculación y sincronización de datos con el reloj Wear OS.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/data/local/prefs/PreferencesManager.kt`:
```kotlin
package mx.edu.utng.cunasegura.data.local.prefs

import android.content.Context
import android.content.SharedPreferences

/**
 * Gestor de preferencias locales basado en [SharedPreferences].
 *
 * Almacena de manera liviana el estado de vinculación con el Smartwatch Wear OS y sincronizaciones.
 *
 * @param context Contexto de la aplicación.
 */
class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("cuna_segura_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WATCH_LINKED = "watch_linked"
    }

    /**
     * Guarda el estado de vinculación del reloj inteligente.
     *
     * @param linked `true` si el reloj está enlazado; de lo contrario, `false`.
     */
    fun setWatchLinked(linked: Boolean) {
        prefs.edit().putBoolean(KEY_WATCH_LINKED, linked).apply()
    }

    /**
     * Comprueba si existe un reloj Wear OS vinculado actualmente.
     *
     * @return `true` si está enlazado; `false` en caso contrario.
     */
    fun isWatchLinked(): Boolean {
        return prefs.getBoolean(KEY_WATCH_LINKED, false)
    }
}
```

---

## FASE 3: Capa de Datos Local (SQLite Room)

### Paso 3.1: Entidades de Base de Datos

Las entidades definen el esquema relacional local para usuarios, alertas de emergencia, contactos de auxilio y configuración del botón de pánico en Wear OS.

> 📋 **INSTRUCCIÓN:** Crea las siguientes entidades en `app/src/main/java/mx/edu/utng/cunasegura/data/local/entity/`:

#### `UsuarioEntity.kt`
```kotlin
package mx.edu.utng.cunasegura.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa la tabla 'usuarios' en la base de datos SQLite local.
 *
 * Almacena los datos del perfil activo en el dispositivo para acceso offline y sesión rápida.
 *
 * @property id Clave primaria autoincremental.
 * @property nombre Nombre completo del vecino o administrador.
 * @property telefono Teléfono móvil de contacto.
 * @property correo Correo electrónico utilizado para la autenticación.
 * @property password Contraseña cifrada o hash local.
 * @property rol Rol asignado en el sistema ('usuario', 'admin', 'admin_global').
 * @property redVecinalId Identificador de la red vecinal a la que pertenece.
 * @property redVecinalNombre Nombre descriptivo de la red comunitaria.
 */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String,
    val telefono: String,
    val correo: String,
    val password: String,
    val rol: String = "usuario",
    val redVecinalId: Int = 0,
    val redVecinalNombre: String = ""
)
```

#### `AlertaEntity.kt`
```kotlin
package mx.edu.utng.cunasegura.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa la tabla 'alertas' para registro e histórico local de emergencias.
 *
 * @property id Clave primaria autoincremental de la alerta.
 * @property usuarioId Identificador del usuario que generó el evento de auxilio.
 * @property nombreUsuario Nombre de la persona en riesgo.
 * @property latitud Coordenada de latitud GPS del incidente.
 * @property longitud Coordenada de longitud GPS del incidente.
 * @property estado Estado de la alerta ('ACTIVA', 'CANCELADA', 'EXPIRADA').
 * @property fechaCreacion Timestamp en milisegundos de la activación.
 */
@Entity(tableName = "alertas")
data class AlertaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val nombreUsuario: String,
    val latitud: Double,
    val longitud: Double,
    val estado: String = "ACTIVA",
    val fechaCreacion: Long = System.currentTimeMillis()
)
```

#### `ContactoEmergenciaEntity.kt`
```kotlin
package mx.edu.utng.cunasegura.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que representa la tabla 'contactos_emergencia'.
 *
 * @property id Clave primaria autoincremental.
 * @property usuarioId ID del usuario propietario del contacto.
 * @property nombre Nombre o alias del contacto de auxilio.
 * @property telefono Teléfono de 10 dígitos para llamadas o SMS de emergencia.
 * @property relacion Parentesco o vínculo ('Mamá', 'Papá', 'Pareja', 'Hermano/a', 'Otro').
 */
@Entity(tableName = "contactos_emergencia")
data class ContactoEmergenciaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val nombre: String,
    val telefono: String,
    val relacion: String
)
```

#### `ConfiguracionToqueEntity.kt`
```kotlin
package mx.edu.utng.cunasegura.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad Room que almacena la configuración de activación rápida por toques desde el reloj Wear OS.
 *
 * @property id Clave primaria de la configuración (típicamente 1 como registro único).
 * @property usuarioId ID del usuario asociado.
 * @property toquesRequeridos Número de pulsaciones consecutivas necesarias para activar la alerta SOS.
 * @property intervaloMaximoMs Ventana máxima de tiempo en milisegundos entre toques consecutivos.
 */
@Entity(tableName = "configuracion_toques")
data class ConfiguracionToqueEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val toquesRequeridos: Int = 3,
    val intervaloMaximoMs: Long = 2000L
)
```

---

### Paso 3.2: Interfaces de Acceso a Datos (DAOs)

> 📋 **INSTRUCCIÓN:** Crea los siguientes DAOs en `app/src/main/java/mx/edu/utng/cunasegura/data/local/dao/`:

#### `UsuarioDao.kt`
```kotlin
package mx.edu.utng.cunasegura.data.local.dao

import androidx.room.*
import mx.edu.utng.cunasegura.data.local.entity.UsuarioEntity

/**
 * Data Access Object (DAO) para operaciones CRUD sobre la tabla 'usuarios'.
 */
@Dao
interface UsuarioDao {

    /** Inserta o actualiza un usuario en la base de datos local. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(usuario: UsuarioEntity): Long

    /** Valida credenciales contra la base de datos local. */
    @Query("SELECT * FROM usuarios WHERE correo = :correo AND password = :password LIMIT 1")
    suspend fun login(correo: String, password: String): UsuarioEntity?

    /** Obtiene el usuario activo en sesión (primer registro). */
    @Query("SELECT * FROM usuarios LIMIT 1")
    suspend fun obtenerUsuarioActual(): UsuarioEntity?

    /** Obtiene un usuario por ID. */
    @Query("SELECT * FROM usuarios WHERE id = :id")
    suspend fun obtenerPorId(id: Int): UsuarioEntity?

    /** Obtiene todos los usuarios registrados localmente. */
    @Query("SELECT * FROM usuarios")
    suspend fun obtenerTodos(): List<UsuarioEntity>

    /** Elimina todos los usuarios de la base de datos local. */
    @Query("DELETE FROM usuarios")
    suspend fun eliminarTodos()
}
```

#### `AlertaDao.kt`
```kotlin
package mx.edu.utng.cunasegura.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import mx.edu.utng.cunasegura.data.local.entity.AlertaEntity

/**
 * Data Access Object (DAO) para operaciones sobre la tabla 'alertas'.
 */
@Dao
interface AlertaDao {

    /** Inserta una nueva alerta de emergencia en SQLite Room. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(alerta: AlertaEntity): Long

    /** Cancela una alerta cambiando su estado a 'CANCELADA'. */
    @Query("UPDATE alertas SET estado = 'CANCELADA' WHERE id = :alertaId")
    suspend fun cancelarAlerta(alertaId: Int)

    /** Obtiene una alerta por su ID. */
    @Query("SELECT * FROM alertas WHERE id = :alertaId")
    suspend fun obtenerPorId(alertaId: Int): AlertaEntity?

    /** Obtiene la alerta activa más reciente de un usuario. */
    @Query("SELECT * FROM alertas WHERE usuarioId = :usuarioId AND estado = 'ACTIVA' ORDER BY fechaCreacion DESC LIMIT 1")
    suspend fun obtenerAlertaActiva(usuarioId: Int): AlertaEntity?

    /** Flujo reactivo de todas las alertas ordenadas cronológicamente. */
    @Query("SELECT * FROM alertas ORDER BY fechaCreacion DESC")
    fun obtenerTodas(): Flow<List<AlertaEntity>>

    /** Consulta directa de todas las alertas registradas. */
    @Query("SELECT * FROM alertas ORDER BY fechaCreacion DESC")
    suspend fun obtenerTodasSync(): List<AlertaEntity>
}
```

#### `ContactoDao.kt`
```kotlin
package mx.edu.utng.cunasegura.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import mx.edu.utng.cunasegura.data.local.entity.ContactoEmergenciaEntity

/**
 * Data Access Object (DAO) para la gestión de contactos de auxilio.
 */
@Dao
interface ContactoDao {

    /** Inserta o actualiza un contacto de emergencia. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(contacto: ContactoEmergenciaEntity): Long

    /** Elimina un contacto por su ID. */
    @Query("DELETE FROM contactos_emergencia WHERE id = :id")
    suspend fun eliminarPorId(id: Int)

    /** Flujo reactivo de contactos de un usuario. */
    @Query("SELECT * FROM contactos_emergencia WHERE usuarioId = :usuarioId")
    fun obtenerContactos(usuarioId: Int): Flow<List<ContactoEmergenciaEntity>>

    /** Consulta directa de contactos asociados a un usuario. */
    @Query("SELECT * FROM contactos_emergencia WHERE usuarioId = :usuarioId")
    suspend fun obtenerPorUsuario(usuarioId: Int): List<ContactoEmergenciaEntity>
}
```

#### `ConfiguracionToqueDao.kt`
```kotlin
package mx.edu.utng.cunasegura.data.local.dao

import androidx.room.*
import mx.edu.utng.cunasegura.data.local.entity.ConfiguracionToqueEntity

/**
 * Data Access Object (DAO) para la configuración de toques del reloj Wear OS.
 */
@Dao
interface ConfiguracionToqueDao {

    /** Inserta o actualiza la configuración de toques. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun guardar(config: ConfiguracionToqueEntity): Long

    /** Obtiene la configuración de toques de un usuario. */
    @Query("SELECT * FROM configuracion_toques WHERE usuarioId = :usuarioId LIMIT 1")
    suspend fun obtenerPorUsuario(usuarioId: Int): ConfiguracionToqueEntity?
}
```

---

### Paso 3.3: Base de Datos Room y Pre-población (`AppDatabase.kt`)

Configura la base de datos Room SQLite, exponiendo los DAOs e inicializando automáticamente el usuario administrador global por defecto en el primer arranque.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/data/local/db/AppDatabase.kt`:
```kotlin
package mx.edu.utng.cunasegura.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import mx.edu.utng.cunasegura.data.local.dao.AlertaDao
import mx.edu.utng.cunasegura.data.local.dao.ConfiguracionToqueDao
import mx.edu.utng.cunasegura.data.local.dao.ContactoDao
import mx.edu.utng.cunasegura.data.local.dao.UsuarioDao
import mx.edu.utng.cunasegura.data.local.entity.AlertaEntity
import mx.edu.utng.cunasegura.data.local.entity.ConfiguracionToqueEntity
import mx.edu.utng.cunasegura.data.local.entity.ContactoEmergenciaEntity
import mx.edu.utng.cunasegura.data.local.entity.UsuarioEntity

/**
 * Base de datos principal Room de Cuna Segura.
 *
 * Administra las tablas locales y cuenta con un callback de pre-población
 * para insertar al Administrador Global por defecto en la primera creación.
 */
@Database(
    entities = [
        UsuarioEntity::class,
        AlertaEntity::class,
        ContactoEmergenciaEntity::class,
        ConfiguracionToqueEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun alertaDao(): AlertaDao
    abstract fun contactoDao(): ContactoDao
    abstract fun configuracionToqueDao(): ConfiguracionToqueDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Retorna la instancia Singleton de [AppDatabase], creándola si es necesario.
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "cuna_segura.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }

    /**
     * Callback para pre-poblar la base de datos con el usuario administrador global inicial.
     */
    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val usuarioDao = database.usuarioDao()
                    usuarioDao.insertar(
                        UsuarioEntity(
                            id = 1,
                            nombre = "Administrador",
                            telefono = "4180000000",
                            correo = "admin@cunasegura.com",
                            password = "admin",
                            rol = "admin",
                            redVecinalNombre = "Dolores Hidalgo Centro"
                        )
                    )
                }
            }
        }
    }
}
```

---

## FASE 4: Servicios de Segundo Plano, Wear OS y Comunicación IoT

### Paso 4.1: Servicio de Rastreo GPS en Primer Plano (`LocationTrackerService.kt`)

`LocationTrackerService` es un `ForegroundService` que rastrea las coordenadas GPS del ciudadano en tiempo real mediante `FusedLocationProviderClient`, actualiza su posición en Firebase Realtime Database y escucha alertas SOS emitidas por vecinos de su red para disparar una notificación con canal sonoro de alarma.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/data/location/LocationTrackerService.kt`:
```kotlin
package mx.edu.utng.cunasegura.data.location

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import mx.edu.utng.cunasegura.MainActivity

/**
 * Servicio en primer plano (Foreground Service) para rastreo GPS continuo y escucha de emergencias vecinales.
 *
 * Responsabilidades:
 * 1. Monitorear la ubicación GPS con [FusedLocationProviderClient] y actualizarla en `usuarios/{uid}/latActual` y `lonActual`.
 * 2. Suscribir un listener en tiempo real al nodo `alerts_log` de Firebase Realtime Database.
 * 3. Emitir notificaciones de alta prioridad con sonido de alarma cuando un vecino activa el botón de pánico.
 */
class LocationTrackerService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var alertsListener: ChildEventListener? = null
    private val serviceStartTime = System.currentTimeMillis()

    companion object {
        private const val TAG = "LocationTrackerService"
        private const val NOTIFICATION_ID = 1001
        private const val CHANNEL_ID = "location_tracker_channel"
        private const val ALARM_CHANNEL_ID = "emergency_alarm_channel"

        /** Inicia el servicio de rastreo GPS en primer plano. */
        fun start(context: Context) {
            val intent = Intent(context, LocationTrackerService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        /** Detiene el servicio de rastreo GPS. */
        fun stop(context: Context) {
            val intent = Intent(context, LocationTrackerService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    updateLocationInFirebase(location)
                }
            }
        }

        listenForEmergencyAlerts()
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        startLocationUpdates()
        return START_STICKY
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_BALANCED_POWER_ACCURACY,
            15000L // Intervalo de 15 segundos
        ).apply {
            setMinUpdateIntervalMillis(10000L)
            setWaitForAccurateLocation(false)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e(TAG, "Permiso de ubicación no concedido", e)
        }
    }

    private fun updateLocationInFirebase(location: Location) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("usuarios").child(uid)
        val updates = mapOf<String, Any>(
            "latActual" to location.latitude,
            "lonActual" to location.longitude
        )
        dbRef.updateChildren(updates)
    }

    private fun listenForEmergencyAlerts() {
        val dbRef = FirebaseDatabase.getInstance().getReference("alerts_log")
        alertsListener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val timestamp = snapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                if (timestamp > serviceStartTime) {
                    val nombre = snapshot.child("nombre").getValue(String::class.java) ?: "Vecino"
                    val tipo = snapshot.child("tipo").getValue(String::class.java) ?: "SOS"
                    val lat = snapshot.child("lat").getValue(Double::class.java) ?: 0.0
                    val lon = snapshot.child("lon").getValue(Double::class.java) ?: 0.0
                    showEmergencyNotification(nombre, tipo, lat, lon)
                }
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
        }
        dbRef.addChildEventListener(alertsListener!!)
    }

    private fun showEmergencyNotification(nombre: String, tipo: String, lat: Double, lon: Double) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("OPEN_ALERT_MAP", true)
            putExtra("ALERT_LAT", lat)
            putExtra("ALERT_LON", lon)
            putExtra("ALERT_USER", nombre)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
            .setContentTitle("🚨 ¡ALERTA VECINAL: $tipo!")
            .setContentText("$nombre ha activado la alarma de pánico.")
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify((System.currentTimeMillis() % 10000).toInt(), notification)
    }

    private fun buildForegroundNotification(): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Cuna Segura Activa")
            .setContentText("Protegiendo tu ubicación y monitoreando la red vecinal")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val trackerChannel = NotificationChannel(
                CHANNEL_ID,
                "Rastreo de Ubicación Cuna Segura",
                NotificationManager.IMPORTANCE_LOW
            )
            val alarmChannel = NotificationChannel(
                ALARM_CHANNEL_ID,
                "Alarmas de Emergencia Vecinal",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                enableVibration(true)
                description = "Notificaciones de emergencia vecinal con alta prioridad sonora"
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(trackerChannel)
            manager.createNotificationChannel(alarmChannel)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        fusedLocationClient.removeLocationUpdates(locationCallback)
        alertsListener?.let {
            FirebaseDatabase.getInstance().getReference("alerts_log").removeEventListener(it)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
```

---

### Paso 4.2: Servicio de Escucha Wearable Data Layer (`PhoneWearableService.kt`)

`PhoneWearableService` implementa `WearableListenerService` para recibir eventos remotos de botón de pánico desde el Smartwatch Wear OS por Bluetooth, despachando SMS con ubicación, llamadas directas al 911 y publicando alertas en Smart TVs mediante MQTT.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/data/wear/PhoneWearableService.kt`:
```kotlin
package mx.edu.utng.cunasegura.data.wear

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.MainActivity
import mx.edu.utng.cunasegura.data.local.db.AppDatabase
import mx.edu.utng.cunasegura.mqtt.MqttPublisher

/**
 * Servicio de escucha Wearable Data Layer para eventos emitidos desde el Smartwatch Wear OS.
 *
 * Rutas RPC soportadas:
 * - `/cunasegura/sos`: Dispara SMS masivo a contactos, publica alerta MQTT en Smart TV y registra en Firebase.
 * - `/cunasegura/call_911`: Realiza una llamada telefónica directa al 911.
 * - `/cunasegura/open_screen`: Abre la pantalla principal o mapa de auxilio en el smartphone.
 */
class PhoneWearableService : WearableListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val db by lazy { AppDatabase.getInstance(applicationContext) }
    private val mqttPublisher by lazy { MqttPublisher(applicationContext) }

    companion object {
        private const val TAG = "PhoneWearableService"
        const val PATH_SOS = "/cunasegura/sos"
        const val PATH_CALL_911 = "/cunasegura/call_911"
        const val PATH_OPEN_SCREEN = "/cunasegura/open_screen"
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        super.onMessageReceived(messageEvent)
        val path = messageEvent.path
        val data = String(messageEvent.data, Charsets.UTF_8)
        Log.d(TAG, "Mensaje recibido del reloj: path=$path, data=$data")

        when (path) {
            PATH_SOS -> serviceScope.launch { procesarAlertaSOS(data) }
            PATH_CALL_911 -> realizarLlamada911()
            PATH_OPEN_SCREEN -> abrirAppEnPantalla()
        }
    }

    private suspend fun procesarAlertaSOS(tipoAlerta: String) {
        val user = db.usuarioDao().obtenerUsuarioActual()
        val nombre = user?.nombre ?: "Vecino Cuna Segura"
        val userId = user?.id ?: 0

        // Obtener última ubicación GPS precisa
        val (lat, lon) = obtenerUbicacionGPS()
        val mapsUrl = "https://maps.google.com/?q=$lat,$lon"
        val mensajeSms = "¡EMERGENCIA CUNA SEGURA!\n$nombre necesita auxilio ($tipoAlerta).\nUbicación: $mapsUrl"

        // 1. Enviar SMS a los contactos de emergencia
        val contactos = db.contactoDao().obtenerPorUsuario(userId)
        contactos.forEach { contacto ->
            enviarSms(contacto.telefono, mensajeSms)
        }

        // 2. Publicar alerta MQTT hacia Smart TVs vinculadas
        mqttPublisher.publicarAlerta(
            alertaId = System.currentTimeMillis().toInt(),
            usuario = nombre,
            lat = lat,
            lon = lon,
            tipo = tipoAlerta
        )

        // 3. Registrar en Firebase Realtime Database
        try {
            val alertLog = mapOf(
                "nombre" to nombre,
                "tipo" to tipoAlerta,
                "lat" to lat,
                "lon" to lon,
                "timestamp" to System.currentTimeMillis(),
                "uid" to (FirebaseAuth.getInstance().currentUser?.uid ?: "")
            )
            FirebaseDatabase.getInstance().getReference("alerts_log").push().setValue(alertLog).await()
        } catch (e: Exception) {
            Log.e(TAG, "Error guardando alerta en Firebase", e)
        }
    }

    private fun enviarSms(telefono: String, mensaje: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            try {
                val smsManager = getSystemService(SmsManager::class.java)
                val parts = smsManager.divideMessage(mensaje)
                smsManager.sendMultipartTextMessage(telefono, null, parts, null, null)
                Log.d(TAG, "SMS de emergencia enviado a $telefono")
            } catch (e: Exception) {
                Log.e(TAG, "Error al enviar SMS a $telefono", e)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun obtenerUbicacionGPS(): Pair<Double, Double> {
        return try {
            val client = LocationServices.getFusedLocationProviderClient(this)
            val cts = CancellationTokenSource()
            val location = client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
            if (location != null) {
                Pair(location.latitude, location.longitude)
            } else {
                Pair(21.1565, -100.9327) // Coordenadas Dolores Hidalgo por defecto
            }
        } catch (e: Exception) {
            Pair(21.1565, -100.9327)
        }
    }

    private fun realizarLlamada911() {
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:911")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(intent)
        }
    }

    private fun abrirAppEnPantalla() {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }
}
```

---

### Paso 4.3: Cliente de Publicación MQTT sobre TLS (`MqttPublisher.kt`)

`MqttPublisher` gestiona la conexión cifrada TLS con **HiveMQ Cloud** para transmitir avisos de pánico e instrucciones de vinculación de Smart TVs mediante los topics `cunasegura/alertas` y `cunasegura/vinculacion/{codigo}`.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/mqtt/MqttPublisher.kt`:
```kotlin
package mx.edu.utng.cunasegura.mqtt

import android.content.Context
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import mx.edu.utng.cunasegura.BuildConfig
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.json.JSONObject
import javax.net.ssl.SSLSocketFactory

/**
 * Publicador MQTT sobre TLS para HiveMQ Cloud.
 *
 * Publica eventos en los topics:
 * - `cunasegura/alertas`: Alertas vecinales emitidas por usuarios.
 * - `cunasegura/vinculacion/{codigo}`: Mensajes de sincronización y enlace con Smart TVs.
 */
class MqttPublisher(private val context: Context) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var client: MqttAsyncClient? = null

    companion object {
        private const val TAG = "MqttPublisher"
        const val TOPIC_ALERTAS = "cunasegura/alertas"
        const val TOPIC_VINCULACION_PREFIX = "cunasegura/vinculacion/"
    }

    /**
     * Establece la conexión segura TLS con el broker HiveMQ Cloud.
     */
    fun conectar(onConnected: (() -> Unit)? = null) {
        val brokerUrl = BuildConfig.HIVEMQ_BROKER_URL
        val username = BuildConfig.HIVEMQ_USERNAME
        val password = BuildConfig.HIVEMQ_PASSWORD

        if (brokerUrl.isBlank()) {
            Log.w(TAG, "HiveMQ brokerUrl no configurado en BuildConfig")
            return
        }

        try {
            val clientId = "CunaSegura_Mobile_" + System.currentTimeMillis()
            client = MqttAsyncClient(brokerUrl, clientId, MemoryPersistence())

            val options = MqttConnectOptions().apply {
                isCleanSession = true
                userName = username
                setPassword(password.toCharArray())
                socketFactory = SSLSocketFactory.getDefault()
                connectionTimeout = 10
                keepAliveInterval = 30
                isAutomaticReconnect = true
            }

            client?.connect(options, null, object : IMqttActionListener {
                override fun onSuccess(asyncActionToken: IMqttToken?) {
                    Log.d(TAG, "Conectado exitosamente a HiveMQ Cloud")
                    onConnected?.invoke()
                }

                override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                    Log.e(TAG, "Error conectando a HiveMQ Cloud", exception)
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Excepción en conexión MQTT", e)
        }
    }

    /**
     * Publica una alerta de emergencia SOS hacia las Smart TVs del vecindario.
     */
    fun publicarAlerta(alertaId: Int, usuario: String, lat: Double, lon: Double, tipo: String = "PANICO") {
        val json = JSONObject().apply {
            put("alertaId", alertaId)
            put("usuario", usuario)
            put("lat", lat)
            put("lon", lon)
            put("tipo", tipo)
            put("timestamp", System.currentTimeMillis())
        }.toString()

        publicarMensaje(TOPIC_ALERTAS, json)
    }

    /**
     * Publica el payload de vinculación hacia la Smart TV que expone el código QR.
     */
    fun publicarVinculacionTV(codigoTV: String, tvId: String, networkId: String, redNombre: String) {
        val json = JSONObject().apply {
            put("tvId", tvId)
            put("networkId", networkId)
            put("redNombre", redNombre)
            put("timestamp", System.currentTimeMillis())
        }.toString()

        val topic = "$TOPIC_VINCULACION_PREFIX$codigoTV"
        publicarMensaje(topic, json)
    }

    private fun publicarMensaje(topic: String, payload: String) {
        scope.launch {
            try {
                if (client?.isConnected != true) {
                    conectar {
                        enviarPayload(topic, payload)
                    }
                } else {
                    enviarPayload(topic, payload)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error publicando mensaje MQTT", e)
            }
        }
    }

    private fun enviarPayload(topic: String, payload: String) {
        val message = MqttMessage(payload.toByteArray(Charsets.UTF_8)).apply {
            qos = 1
            isRetained = false
        }
        client?.publish(topic, message, null, object : IMqttActionListener {
            override fun onSuccess(asyncActionToken: IMqttToken?) {
                Log.d(TAG, "Mensaje publicado en topic '$topic': $payload")
            }

            override fun onFailure(asyncActionToken: IMqttToken?, exception: Throwable?) {
                Log.e(TAG, "Fallo al publicar en topic '$topic'", exception)
            }
        })
    }
}
```

---

### Paso 4.4: Servicio Remoto Firestore (`FirestoreService.kt`)

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/data/remote/FirestoreService.kt`:
```kotlin
package mx.edu.utng.cunasegura.data.remote

/**
 * Servicio remoto para integración con servicios cloud alternativos.
 */
class FirestoreService {
    // Stub de servicio remoto de respaldo
}
```

---

## FASE 5: Capa de Dominio (Modelos Puros, Interfaces de Repositorio y Casos de Uso)

### Paso 5.1: Modelos de Dominio

Los modelos de dominio representan entidades de negocio independientes de frameworks o librerías de persistencia.

> 📋 **INSTRUCCIÓN:** Crea los siguientes modelos en `app/src/main/java/mx/edu/utng/cunasegura/domain/model/`:

- `Usuario.kt`: Perfil de usuario con roles (`admin`, `usuario`), red vecinal asociada y coordenadas en vivo.
- `Alerta.kt`: Datos de incidentes SOS, estados y timestamps.
- `ContactoEmergencia.kt`: Contactos de confianza y vínculos familiares.
- `ConfiguracionToque.kt`: Parámetros de toques consecutivos para activación Wear OS.
- `Network.kt`: Red vecinal comunitaria con radio de cobertura, parámetros anti-falsa alarma y Smart TV vinculada.
- `Vecindad.kt`: Agrupación vecinal y métricas de seguridad.

---

### Paso 5.2: Interfaces de Repositorio

> 📋 **INSTRUCCIÓN:** Crea los contratos de repositorios en `app/src/main/java/mx/edu/utng/cunasegura/domain/repository/` y `app/src/main/java/mx/edu/utng/cunasegura/data/repository/`:

- `IUsuarioRepository.kt`: Operaciones de autenticación, sesión y censo.
- `IAlertaRepository.kt`: Activación, cancelación y flujos de alertas vecinales en tiempo real.
- `INetworkRepository.kt`: CRUD de redes vecinales, moderación y parámetros globales del sistema.
- `IContactoRepository.kt`: Directorio de contactos de auxilio.

---

### Paso 5.3: Casos de Uso Atómicos

> 📋 **INSTRUCCIÓN:** Crea los 11 casos de uso en `app/src/main/java/mx/edu/utng/cunasegura/domain/usecase/`:

1. `ActivarAlertaUseCase.kt`: Emite alerta SOS y retorna el ID generado.
2. `CancelarAlertaUseCase.kt`: Cancela la alerta activa.
3. `ObtenerUsuarioActualUseCase.kt`: Recupera la sesión activa.
4. `GuardarUsuarioUseCase.kt`: Persiste los datos del usuario en Room.
5. `LimpiarSesionLocalUseCase.kt`: Sanea la base de datos local al cerrar sesión.
6. `ValidarLoginUseCase.kt`: Valida credenciales contra el repositorio.
7. `ValidarAdminUseCase.kt`: Verifica privilegios de administrador.
8. `ObtenerUsuarioUseCase.kt`: Obtiene el usuario por su identificador.
9. `ObtenerContactosUseCase.kt`: Expone el flujo reactivo de contactos.
10. `AgregarContactoUseCase.kt`: Agrega o actualiza un contacto de auxilio.
11. `EliminarContactoUseCase.kt`: Elimina un contacto por ID.

---

## FASE 6: Implementación de Repositorios (Data Layer)

### Paso 6.1: Repositorio de Usuarios y Autenticación (`UsuarioRepositoryImpl.kt`)

Gestiona la autenticación mediante Firebase Auth, mapeo entre `UsuarioEntity` y `Usuario`, y persistencia offline.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/data/repository/UsuarioRepositoryImpl.kt`:
```kotlin
package mx.edu.utng.cunasegura.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository

/**
 * Implementación concreta de [IUsuarioRepository] basada en Firebase Authentication y Firebase Realtime Database.
 *
 * Utiliza la rama `/usuarios/{uid}` como única fuente de verdad para sincronización en tiempo real entre vecinos y administración.
 */
class UsuarioRepositoryImpl : IUsuarioRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()

    /**
     * Guarda o actualiza los datos del usuario autenticado en Firebase Realtime Database.
     *
     * @param usuario Datos del modelo de dominio.
     */
    override suspend fun guardarUsuario(usuario: Usuario) {
        val firebaseUser = auth.currentUser ?: return
        val map = mapOf(
            "nombre" to usuario.nombre,
            "telefono" to usuario.telefono,
            "correo" to usuario.correo,
            "rol" to usuario.rol,
            "tvVinculada" to usuario.tvVinculada,
            "networkId" to usuario.networkId.ifBlank { firebaseUser.uid },
            "fechaIngreso" to usuario.fechaIngreso
        )
        db.getReference("usuarios").child(firebaseUser.uid).updateChildren(map).await()
    }

    /**
     * Busca un usuario registrado en Firebase mediante una consulta indexada por su número de teléfono.
     *
     * @param telefono Número de 10 dígitos.
     * @return [Usuario] encontrado o `null`.
     */
    override suspend fun buscarPorTelefono(telefono: String): Usuario? {
        val snapshot = db.getReference("usuarios")
            .orderByChild("telefono")
            .equalTo(telefono)
            .limitToFirst(1)
            .get()
            .await()
        
        val child = snapshot.children.firstOrNull() ?: return null
        return Usuario(
            id = 0,
            nombre = child.child("nombre").getValue(String::class.java) ?: "",
            telefono = child.child("telefono").getValue(String::class.java) ?: "",
            correo = child.child("correo").getValue(String::class.java) ?: "",
            password = "",
            rol = child.child("rol").getValue(String::class.java) ?: "usuario",
            estado = child.child("estado").getValue(String::class.java) ?: "activo",
            tvVinculada = child.child("tvVinculada").getValue(Boolean::class.java) ?: false,
            networkId = child.child("networkId").getValue(String::class.java) ?: "",
            fechaIngreso = child.child("fechaIngreso").getValue(Long::class.java) ?: 0L
        )
    }

    override suspend fun validarAdmin(correo: String, password: String): Usuario? {
        // Redundante con Firebase Auth
        return null
    }

    override suspend fun validarLogin(correo: String, password: String): Usuario? {
        // Redundante con Firebase Auth
        return null
    }

    /**
     * Recupera todos los usuarios registrados en el nodo `/usuarios` de Firebase Realtime Database.
     *
     * @return Lista completa de [Usuario].
     */
    override suspend fun obtenerTodosLosUsuarios(): List<Usuario> {
        val snapshot = db.getReference("usuarios").get().await()
        return snapshot.children.mapNotNull { child ->
            Usuario(
                id = 0,
                nombre = child.child("nombre").getValue(String::class.java) ?: "",
                telefono = child.child("telefono").getValue(String::class.java) ?: "",
                correo = child.child("correo").getValue(String::class.java) ?: "",
                password = "",
                rol = child.child("rol").getValue(String::class.java) ?: "usuario",
                estado = child.child("estado").getValue(String::class.java) ?: "activo",
                tvVinculada = child.child("tvVinculada").getValue(Boolean::class.java) ?: false,
                networkId = child.child("networkId").getValue(String::class.java) ?: "",
                fechaIngreso = child.child("fechaIngreso").getValue(Long::class.java) ?: 0L,
                uid = child.key ?: ""
            )
        }
    }

    /**
     * Obtiene los datos del perfil activo en Firebase Authentication y su registro en la base de datos.
     *
     * @return [Usuario] activo o `null` si no hay sesión iniciada.
     */
    override suspend fun obtenerUsuarioActual(): Usuario? {
        val firebaseUser = auth.currentUser ?: return null
        val snapshot = db.getReference("usuarios").child(firebaseUser.uid).get().await()
        if (!snapshot.exists()) {
            val email = firebaseUser.email ?: ""
            return Usuario(
                id = 0,
                nombre = firebaseUser.displayName ?: email.substringBefore("@"),
                telefono = "",
                correo = email,
                password = "",
                rol = if (email == "admin@cunasegura.com") "admin" else "usuario",
                estado = "activo",
                tvVinculada = false
            )
        }
        return Usuario(
            id = 0,
            nombre = snapshot.child("nombre").getValue(String::class.java) ?: firebaseUser.displayName ?: firebaseUser.email?.substringBefore("@") ?: "",
            telefono = snapshot.child("telefono").getValue(String::class.java) ?: "",
            correo = snapshot.child("correo").getValue(String::class.java) ?: firebaseUser.email ?: "",
            password = "",
            rol = snapshot.child("rol").getValue(String::class.java) ?: "usuario",
            estado = snapshot.child("estado").getValue(String::class.java) ?: "activo",
            tvVinculada = snapshot.child("tvVinculada").getValue(Boolean::class.java) ?: false,
            networkId = snapshot.child("networkId").getValue(String::class.java) ?: "",
            fechaIngreso = snapshot.child("fechaIngreso").getValue(Long::class.java) ?: 0L,
            uid = firebaseUser.uid
        )
    }

    /**
     * Actualiza los datos de nombre, teléfono y opcionalmente la contraseña del usuario en Firebase.
     *
     * @param nombre Nuevo nombre completo.
     * @param telefono Nuevo teléfono.
     * @param nuevaPassword Nueva contraseña opcional.
     * @return [Result] que encapsula éxito o la excepción generada.
     */
    override suspend fun actualizarPerfilUsuario(nombre: String, telefono: String, nuevaPassword: String?): Result<Unit> {
        val firebaseUser = auth.currentUser ?: return Result.failure(Exception("Sesión no iniciada"))
        return try {
            val updates = mutableMapOf<String, Any>(
                "nombre" to nombre,
                "telefono" to telefono
            )
            db.getReference("usuarios").child(firebaseUser.uid).updateChildren(updates).await()

            if (!nuevaPassword.isNullOrBlank()) {
                firebaseUser.updatePassword(nuevaPassword).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Limpieza de sesión gestionada por Firebase Auth.
     */
    override suspend fun limpiarSesionLocal() {
        // No-op: La sesión se gestiona únicamente mediante Firebase Auth
    }
}
```

---

### Paso 6.2: Repositorio de Alertas y Difusión Híbrida (`AlertaRepositoryImpl.kt`)

Sincroniza eventos de pánico entre SQLite Room, el nodo `alerts_log` de Firebase Realtime Database y el broker MQTT HiveMQ Cloud.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/data/repository/AlertaRepositoryImpl.kt`:
```kotlin
package mx.edu.utng.cunasegura.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.map
import mx.edu.utng.cunasegura.data.local.dao.AlertaDao
import mx.edu.utng.cunasegura.data.local.entity.AlertaEntity
import mx.edu.utng.cunasegura.domain.model.Alerta
import mx.edu.utng.cunasegura.domain.repository.IAlertaRepository
import android.util.Log
import kotlinx.coroutines.tasks.await

/**
 * Implementación híbrida de [IAlertaRepository] que persiste localmente en SQLite Room mediante [AlertaDao],
 * sincroniza en tiempo real con Firebase Realtime Database y dispara mensajes de emergencia MQTT a Smart TVs.
 *
 * @property alertaDao DAO de alertas para persistencia local offline-first.
 */
class AlertaRepositoryImpl(
    private val alertaDao: AlertaDao
) : IAlertaRepository {

    private val dbRef = FirebaseDatabase.getInstance().getReference("alertas")
    private val TAG = "AlertaRepository"

    /**
     * Inserta una alerta SOS en la base de datos local y la publica tanto en Firebase como en el broker MQTT.
     *
     * @param alerta Datos de la alerta emitida.
     * @return Identificador asignado a la alerta.
     */
    override suspend fun crearAlerta(alerta: Alerta): Long {
        var id = System.currentTimeMillis() % 1000000
        try {
            // Verificar si el usuario ya tiene una alerta activa reciente (últimos 45s) para evitar duplicados por GPS o pulsaciones múltiples
            val alertaActiva = alertaDao.buscarAlertaActivaPorUsuario(alerta.usuarioId)
            val esReciente = alertaActiva != null && (Math.abs(System.currentTimeMillis() - alertaActiva.creadoEn) < 45000)
            
            val entityToInsert = if (esReciente && alertaActiva != null) {
                id = alertaActiva.id.toLong()
                alerta.copy(id = alertaActiva.id, creadoEn = alertaActiva.creadoEn).toEntity()
            } else {
                alerta.toEntity()
            }

            val insertedId = alertaDao.insertarAlerta(entityToInsert)
            if (insertedId > 0 && !esReciente) {
                id = insertedId
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error insertando alerta local en Room DB", e)
        }
        val alertaConId = alerta.copy(id = id.toInt())
        
        // Obtener nombreUsuario y networkId reales del usuario actual si vienen incompletos
        var nombreFinal = alertaConId.nombreUsuario
        var networkId = alertaConId.networkId
        try {
            val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                val userSnap = FirebaseDatabase.getInstance().getReference("usuarios")
                    .child(firebaseUser.uid).get().await()
                if (userSnap.exists()) {
                    val realName = userSnap.child("nombre").getValue(String::class.java)
                        ?: firebaseUser.displayName
                        ?: firebaseUser.email?.substringBefore("@")
                    if (!realName.isNullOrBlank()) {
                        nombreFinal = realName
                    }
                    if (networkId.isEmpty()) {
                        networkId = userSnap.child("networkId").getValue(String::class.java) ?: firebaseUser.uid
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error al obtener datos del usuario para la alerta", e)
        }
        if (nombreFinal.isBlank()) nombreFinal = "Vecino"

        // Sync to Firebase Realtime Database
        val map = mapOf(
            "id" to alertaConId.id,
            "usuarioId" to alertaConId.usuarioId,
            "nombreUsuario" to nombreFinal,
            "estado" to alertaConId.estado,
            "latitud" to alertaConId.latitud,
            "longitud" to alertaConId.longitud,
            "fueAtendida" to alertaConId.fueAtendida,
            "esFalsaAlarma" to alertaConId.esFalsaAlarma,
            "creadoEn" to alertaConId.creadoEn,
            "networkId" to networkId
        )
        dbRef.child(alertaConId.id.toString()).setValue(map).addOnFailureListener {
            Log.e(TAG, "Error sync Firebase: crearAlerta", it)
        }

        // Publicar por MQTT para que las TVs de la red vecinal la reciban e inicien alarma
        mx.edu.utng.cunasegura.mqtt.MqttPublisher.publishAlertaTv(
            usuarioId = alertaConId.usuarioId,
            nombreUsuario = nombreFinal,
            lat = alertaConId.latitud,
            lon = alertaConId.longitud,
            networkId = networkId,
            estado = "activa"
        )
        
        return id
    }

    /**
     * Cancela la alerta tanto en Room como en Firebase y notifica el cese de alarma a las Smart TVs vía MQTT.
     *
     * @param id Identificador de la alerta a cancelar.
     */
    override suspend fun cancelarAlerta(id: Int) {
        try {
            alertaDao.actualizarEstado(id = id, estado = "cancelada")
            
            // Sync to Firebase
            dbRef.child(id.toString()).child("estado").setValue("cancelada").addOnFailureListener {
                Log.e(TAG, "Error sync Firebase: cancelarAlerta", it)
            }

            // Notificar cancelación por MQTT a las Smart TVs
            var networkId = ""
            var usuarioIdInt = 1
            var nombre = "Vecino"
            var lat = 0.0
            var lon = 0.0

            val alerta = alertaDao.buscarPorId(id)
            if (alerta != null) {
                usuarioIdInt = alerta.usuarioId
                nombre = alerta.nombreUsuario
                lat = alerta.latitud
                lon = alerta.longitud
            }

            try {
                val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (firebaseUser != null) {
                    val netSnap = FirebaseDatabase.getInstance().getReference("usuarios")
                        .child(firebaseUser.uid).child("networkId").get().await()
                    networkId = netSnap.getValue(String::class.java) ?: firebaseUser.uid
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error al obtener networkId para cancelar MQTT", e)
            }

            mx.edu.utng.cunasegura.mqtt.MqttPublisher.publishAlertaTv(
                usuarioId = usuarioIdInt,
                nombreUsuario = nombre,
                lat = lat,
                lon = lon,
                networkId = networkId,
                estado = "cancelada"
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error en cancelarAlerta", e)
        }
    }

    /**
     * Busca una alerta en la base de datos local por su ID.
     *
     * @param id Identificador de la alerta.
     * @return [Alerta] si existe o `null`.
     */
    override suspend fun obtenerAlertaPorId(id: Int): Alerta? {
        return alertaDao.buscarPorId(id)?.toDomain()
    }

    /**
     * Observa de forma reactiva la alerta activa del usuario especificado.
     *
     * @param usuarioId ID del usuario.
     * @return [Flow] con la entidad [Alerta] o `null`.
     */
    override fun obtenerAlertaActiva(usuarioId: Int): Flow<Alerta?> {
        return alertaDao.obtenerAlertaActivaPorUsuario(usuarioId).map { entity ->
            entity?.toDomain()
        }
    }

    /**
     * Escucha en tiempo real todas las alertas activas en Firebase y aplica el filtro temporal `tiempoVidaAlerta`.
     *
     * @return [Flow] reactivo con la lista de alertas vigentes.
     */
    override fun obtenerAlertasVecinalesActivas(): Flow<List<Alerta>> = callbackFlow {
        // Fetch config once when flow starts
        var tiempoVidaMs = 720L * 60 * 1000 // 720 minutes default (12h)
        FirebaseDatabase.getInstance().getReference("configuracion_global").child("tiempoVidaAlerta").get().addOnSuccessListener { snap ->
            val minutos = snap.getValue(Double::class.java) ?: 720.0
            tiempoVidaMs = (minutos * 60 * 1000).toLong()
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val alertas = mutableListOf<Alerta>()
                for (child in snapshot.children) {
                    try {
                        val estado = child.child("estado").getValue(String::class.java) ?: ""
                        if (estado == "activa") {
                            val rawUsuarioId = child.child("usuarioId").value
                            val usuarioIdInt = when (rawUsuarioId) {
                                is Long -> rawUsuarioId.toInt()
                                is Int -> rawUsuarioId
                                is String -> rawUsuarioId.toIntOrNull() ?: 0
                                else -> 0
                            }
                            val netId = child.child("networkId").getValue(String::class.java) ?: ""

                            alertas.add(
                                Alerta(
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
                        Log.e(TAG, "Error parsing alerta", e)
                    }
                }
                // Filtrar por tiempoVidaAlerta
                val ahora = System.currentTimeMillis()
                val activas = alertas.filter { alerta ->
                    (ahora - alerta.creadoEn) <= tiempoVidaMs
                }
                trySend(activas)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "Firebase listener cancelled", error.toException())
            }
        }

        dbRef.addValueEventListener(listener)
        awaitClose { dbRef.removeEventListener(listener) }
    }

    /**
     * Consulta el catálogo total de alertas históricas en Firebase.
     *
     * @return Lista completa de [Alerta].
     */
    override suspend fun obtenerTodasLasAlertas(): List<Alerta> {
        return try {
            val snapshot = dbRef.get().await()
            val list = mutableListOf<Alerta>()
            for (child in snapshot.children) {
                val alerta = Alerta(
                    id = child.child("id").getValue(Int::class.java) ?: 0,
                    usuarioId = child.child("usuarioId").getValue(Int::class.java) ?: 0,
                    nombreUsuario = child.child("nombreUsuario").getValue(String::class.java) ?: "Vecino",
                    estado = child.child("estado").getValue(String::class.java) ?: "",
                    latitud = child.child("latitud").getValue(Double::class.java) ?: 0.0,
                    longitud = child.child("longitud").getValue(Double::class.java) ?: 0.0,
                    fueAtendida = child.child("fueAtendida").getValue(Boolean::class.java) ?: false,
                    esFalsaAlarma = child.child("esFalsaAlarma").getValue(Boolean::class.java) ?: false,
                    creadoEn = child.child("creadoEn").getValue(Long::class.java) ?: 0L
                )
                list.add(alerta)
            }
            list
        } catch (e: Exception) {
            Log.e(TAG, "Error getting all alerts: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Consulta las alertas asociadas a una red vecinal comunitaria filtradas por su vigencia.
     *
     * @param networkId Identificador de la red vecinal.
     * @return Lista de alertas de la comunidad ordenadas cronológicamente.
     */
    override suspend fun obtenerAlertasPorNetworkId(networkId: String): List<Alerta> {
        return try {
            val snapshot = dbRef.get().await()
            val list = mutableListOf<Alerta>()
            for (child in snapshot.children) {
                val netId = child.child("networkId").getValue(String::class.java) ?: ""
                if (netId == networkId || networkId.isBlank()) {
                    val rawUsuarioId = child.child("usuarioId").value
                    val usuarioIdInt = when (rawUsuarioId) {
                        is Long -> rawUsuarioId.toInt()
                        is Int -> rawUsuarioId
                        is String -> rawUsuarioId.toIntOrNull() ?: 0
                        else -> 0
                    }
                    val alerta = Alerta(
                        id = child.child("id").getValue(Int::class.java) ?: 0,
                        usuarioId = usuarioIdInt,
                        nombreUsuario = child.child("nombreUsuario").getValue(String::class.java) ?: "Vecino",
                        estado = child.child("estado").getValue(String::class.java) ?: "",
                        latitud = child.child("latitud").getValue(Double::class.java) ?: 0.0,
                        longitud = child.child("longitud").getValue(Double::class.java) ?: 0.0,
                        fueAtendida = child.child("fueAtendida").getValue(Boolean::class.java) ?: false,
                        esFalsaAlarma = child.child("esFalsaAlarma").getValue(Boolean::class.java) ?: false,
                        creadoEn = child.child("creadoEn").getValue(Long::class.java) ?: 0L,
                        networkId = netId
                    )
                    list.add(alerta)
                }
            }
            
            // Obtener config
            val configSnap = FirebaseDatabase.getInstance().getReference("configuracion_global").child("tiempoVidaAlerta").get().await()
            val minutos = configSnap.getValue(Double::class.java) ?: 720.0
            val tiempoVidaMs = (minutos * 60 * 1000).toLong()
            val ahora = System.currentTimeMillis()
            
            list.filter { (ahora - it.creadoEn) <= tiempoVidaMs }.sortedByDescending { it.creadoEn }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting network alerts: ${e.message}", e)
            emptyList()
        }
    }

    // -------------------------------------------------------------------------
    // Mappers Entity <-> Domain
    // -------------------------------------------------------------------------

    private fun Alerta.toEntity(): AlertaEntity =
        AlertaEntity(
            id = this.id,
            usuarioId = this.usuarioId,
            nombreUsuario = this.nombreUsuario,
            estado = this.estado,
            latitud = this.latitud,
            longitud = this.longitud,
            fueAtendida = this.fueAtendida,
            esFalsaAlarma = this.esFalsaAlarma,
            creadoEn = this.creadoEn
        )

    private fun AlertaEntity.toDomain(): Alerta =
        Alerta(
            id = this.id,
            usuarioId = this.usuarioId,
            nombreUsuario = this.nombreUsuario,
            estado = this.estado,
            latitud = this.latitud,
            longitud = this.longitud,
            fueAtendida = this.fueAtendida,
            esFalsaAlarma = this.esFalsaAlarma,
            creadoEn = this.creadoEn
        )
}
```

---

### Paso 6.3: Repositorio de Contactos de Emergencia (`ContactoRepositoryImpl.kt`)

Mantiene la lista de contactos de confianza bajo la rama `/usuarios/{uid}/contactos/` de Firebase Realtime Database con listeners reactivos.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/data/repository/ContactoRepositoryImpl.kt`:
```kotlin
package mx.edu.utng.cunasegura.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.domain.model.ContactoEmergencia
import mx.edu.utng.cunasegura.domain.repository.IContactoRepository
import kotlin.random.Random

/**
 * Implementación de [IContactoRepository] utilizando Firebase Realtime Database como origen de datos en la nube.
 *
 * Mantiene la lista de contactos bajo la ruta `/usuarios/{uid}/contactos/` de forma reactiva con [callbackFlow].
 */
class ContactoRepositoryImpl : IContactoRepository {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseDatabase.getInstance()

    /**
     * Guarda un nuevo contacto de confianza en la nube bajo el perfil del usuario actual.
     *
     * @param contacto Datos del contacto a persistir.
     */
    override suspend fun agregarContacto(contacto: ContactoEmergencia) {
        val firebaseUser = auth.currentUser ?: return
        val ref = db.getReference("usuarios").child(firebaseUser.uid).child("contactos")
        
        // Generamos un id numérico aleatorio único si viene en cero (como en el formulario de Compose)
        val id = if (contacto.id == 0) Random.nextInt(1, Int.MAX_VALUE) else contacto.id
        
        val map = mapOf(
            "id" to id,
            "nombre" to contacto.nombre,
            "telefono" to contacto.telefono,
            "relacion" to contacto.relacion,
            "creadoEn" to contacto.creadoEn
        )
        ref.child(id.toString()).setValue(map).await()
    }

    /**
     * Elimina un contacto de emergencia de la base de datos remota por su ID.
     *
     * @param id Identificador del contacto.
     */
    override suspend fun eliminarContacto(id: Int) {
        val firebaseUser = auth.currentUser ?: return
        db.getReference("usuarios")
            .child(firebaseUser.uid)
            .child("contactos")
            .child(id.toString())
            .removeValue()
            .await()
    }

    /**
     * Observa en tiempo real el listado de contactos del usuario mediante un listener de Firebase.
     *
     * @param usuarioId Identificador del usuario.
     * @return [Flow] reactivo con la lista actualizada de contactos de emergencia.
     */
    override fun obtenerContactos(usuarioId: Int): Flow<List<ContactoEmergencia>> = callbackFlow {
        val firebaseUser = auth.currentUser
        if (firebaseUser == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val ref = db.getReference("usuarios").child(firebaseUser.uid).child("contactos")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { child ->
                    val id = child.child("id").getValue(Int::class.java) ?: 0
                    val nombre = child.child("nombre").getValue(String::class.java) ?: ""
                    val telefono = child.child("telefono").getValue(String::class.java) ?: ""
                    val relacion = child.child("relacion").getValue(String::class.java) ?: ""
                    val creadoEn = child.child("creadoEn").getValue(Long::class.java) ?: System.currentTimeMillis()
                    
                    ContactoEmergencia(
                        id = id,
                        usuarioId = usuarioId,
                        nombre = nombre,
                        telefono = telefono,
                        relacion = relacion,
                        creadoEn = creadoEn
                    )
                }
                trySend(lista)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
```

---

### Paso 6.4: Repositorio de Redes Vecinales Comunitarias (`NetworkRepositoryImpl.kt`)

Administra las redes vecinales, membresías de vecinos, moderación comunitaria y parámetros globales del sistema.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/data/repository/NetworkRepositoryImpl.kt`:
```kotlin
package mx.edu.utng.cunasegura.data.repository

import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.domain.model.Network
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.INetworkRepository

/**
 * Implementación de [INetworkRepository] conectada con Firebase Realtime Database.
 *
 * Administra los nodos `/networks/{networkId}`, `/configuracion_global` y sincroniza las membresías de los usuarios.
 */
class NetworkRepositoryImpl : INetworkRepository {
    private val db = FirebaseDatabase.getInstance()

    /**
     * Guarda una nueva red vecinal comunitaria en Firebase.
     *
     * @param network Modelo de la red.
     */
    override suspend fun crearNetwork(network: Network) {
        db.getReference("networks").child(network.id).setValue(network).await()
    }

    /**
     * Consulta una red vecinal por su identificador único.
     *
     * @param id Identificador de la red.
     * @return [Network] o `null` si no existe.
     */
    override suspend fun obtenerNetworkPorId(id: String): Network? {
        val snapshot = db.getReference("networks").child(id).get().await()
        if (!snapshot.exists()) return null
        return snapshot.getValue(Network::class.java)
    }

    /**
     * Obtiene el listado de redes comunitarias públicas/abiertas basadas en geolocalización.
     *
     * @return Lista de redes de tipo `Abierta`.
     */
    override suspend fun obtenerRedesAbiertas(): List<Network> {
        val snapshot = db.getReference("networks")
            .orderByChild("tipo")
            .equalTo("Abierta")
            .get()
            .await()
        return snapshot.children.mapNotNull { it.getValue(Network::class.java) }
    }

    /**
     * Vincula a un usuario como miembro de una red vecinal.
     *
     * @param usuarioId UID del usuario en Firebase.
     * @param networkId ID de la red vecinal.
     * @return `true` si la operación se completó exitosamente.
     */
    override suspend fun unirseARed(usuarioId: String, networkId: String): Boolean {
        return try {
            // 1. Agregar el usuario a la lista de miembros de la red
            db.getReference("networks")
                .child(networkId)
                .child("miembros")
                .child(usuarioId)
                .setValue(true)
                .await()

            // 2. Actualizar el networkId y fechaIngreso en el perfil del usuario
            val updates = mapOf(
                "networkId" to networkId,
                "fechaIngreso" to System.currentTimeMillis()
            )
            db.getReference("usuarios")
                .child(usuarioId)
                .updateChildren(updates)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Desvincula a un usuario de su red vecinal actual.
     *
     * @param usuarioId UID del usuario.
     * @param networkId ID de la red actual.
     * @return `true` si se desvinculó con éxito.
     */
    override suspend fun salirDeRed(usuarioId: String, networkId: String): Boolean {
        return try {
            // 1. Remover de miembros de la red
            db.getReference("networks")
                .child(networkId)
                .child("miembros")
                .child(usuarioId)
                .removeValue()
                .await()

            // 2. Restablecer el networkId del usuario a su propio UID (por defecto)
            db.getReference("usuarios")
                .child(usuarioId)
                .child("networkId")
                .setValue(usuarioId)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene el listado completo de vecinos que integran una red vecinal específica.
     *
     * @param networkId ID de la red comunitaria.
     * @return Lista de [Usuario] que pertenecen a la red.
     */
    override suspend fun obtenerMiembrosDeRed(networkId: String): List<Usuario> {
        val network = obtenerNetworkPorId(networkId) ?: return emptyList()
        val uids = network.miembros.keys
        val list = mutableListOf<Usuario>()
        for (uid in uids) {
            val userSnap = db.getReference("usuarios").child(uid).get().await()
            if (userSnap.exists()) {
                val user = Usuario(
                    id = 0,
                    nombre = userSnap.child("nombre").getValue(String::class.java) ?: "",
                    telefono = userSnap.child("telefono").getValue(String::class.java) ?: "",
                    correo = userSnap.child("correo").getValue(String::class.java) ?: "",
                    password = "",
                    rol = userSnap.child("rol").getValue(String::class.java) ?: "usuario",
                    estado = userSnap.child("estado").getValue(String::class.java) ?: "activo",
                    tvVinculada = userSnap.child("tvVinculada").getValue(Boolean::class.java) ?: false,
                    networkId = userSnap.child("networkId").getValue(String::class.java) ?: "",
                    fechaIngreso = userSnap.child("fechaIngreso").getValue(Long::class.java) ?: 0L,
                    uid = uid
                )
                list.add(user)
            }
        }
        return list
    }

    /**
     * Expulsa a un miembro de la red vecinal y reinicia sus credenciales comunitarias.
     *
     * @param usuarioId UID del miembro expulsado.
     * @param networkId ID de la red.
     * @return `true` si la expulsión se realizó correctamente.
     */
    override suspend fun expulsarMiembro(usuarioId: String, networkId: String): Boolean {
        return try {
            db.getReference("networks")
                .child(networkId)
                .child("miembros")
                .child(usuarioId)
                .removeValue()
                .await()

            val updates = mapOf<String, Any>(
                "networkId" to usuarioId,
                "rolEnRed" to ""
            )
            db.getReference("usuarios")
                .child(usuarioId)
                .updateChildren(updates)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Renombra una red comunitaria en Firebase.
     *
     * @param networkId ID de la red.
     * @param nuevoNombre Nuevo nombre asignado.
     * @return `true` en caso de éxito.
     */
    override suspend fun actualizarNombreRed(networkId: String, nuevoNombre: String): Boolean {
        return try {
            db.getReference("networks").child(networkId).child("nombre").setValue(nuevoNombre).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Consulta las alertas asociadas a una red comunitaria.
     *
     * @param networkId ID de la red.
     * @return Lista de alertas emitidas en la red.
     */
    override suspend fun obtenerAlertasDeRed(networkId: String): List<mx.edu.utng.cunasegura.domain.model.Alerta> {
        return try {
            val snapshot = db.getReference("alertas").get().await()
            val list = mutableListOf<mx.edu.utng.cunasegura.domain.model.Alerta>()
            for (child in snapshot.children) {
                val netId = child.child("networkId").getValue(String::class.java) ?: ""
                if (netId == networkId || networkId.isBlank()) {
                    val rawUsuarioId = child.child("usuarioId").value
                    val usuarioIdInt = when (rawUsuarioId) {
                        is Long -> rawUsuarioId.toInt()
                        is Int -> rawUsuarioId
                        is String -> rawUsuarioId.toIntOrNull() ?: 0
                        else -> 0
                    }
                    val alerta = mx.edu.utng.cunasegura.domain.model.Alerta(
                        id = child.child("id").getValue(Int::class.java) ?: 0,
                        usuarioId = usuarioIdInt,
                        nombreUsuario = child.child("nombreUsuario").getValue(String::class.java) ?: "Vecino",
                        estado = child.child("estado").getValue(String::class.java) ?: "",
                        latitud = child.child("latitud").getValue(Double::class.java) ?: 0.0,
                        longitud = child.child("longitud").getValue(Double::class.java) ?: 0.0,
                        fueAtendida = child.child("fueAtendida").getValue(Boolean::class.java) ?: false,
                        esFalsaAlarma = child.child("esFalsaAlarma").getValue(Boolean::class.java) ?: false,
                        creadoEn = child.child("creadoEn").getValue(Long::class.java) ?: 0L,
                        networkId = netId
                    )
                    list.add(alerta)
                }
            }
            list.sortedByDescending { it.creadoEn }
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Persiste los parámetros globales de la red vecinal en `/configuracion_global`.
     *
     * @param tipo Tipo de red (`Abierta` o `Cerrada`).
     * @param radio Radio en metros.
     * @param tiempoAntiFalsa Segundos de gracia antes de disparo definitivo.
     * @param checkVida Minutos de intervalo de verificación.
     * @param esperarDiasNuevos Días de restricción para nuevos ingresos.
     * @param tiempoVidaAlerta Minutos de vigencia de una alerta activa.
     */
    override suspend fun guardarConfiguracionGlobal(
        tipo: String,
        radio: Double,
        tiempoAntiFalsa: Double,
        checkVida: Double,
        esperarDiasNuevos: Int,
        tiempoVidaAlerta: Double
    ) {
        val map = mapOf(
            "tipo" to tipo,
            "radio" to radio,
            "tiempoAntiFalsa" to tiempoAntiFalsa,
            "checkVida" to checkVida,
            "esperarDiasNuevos" to esperarDiasNuevos,
            "tiempoVidaAlerta" to tiempoVidaAlerta
        )
        db.getReference("configuracion_global").setValue(map).await()
    }

    /**
     * Obtiene el mapa de configuraciones globales vigentes en Firebase Realtime Database.
     *
     * @return Mapa de clave y valor de configuración global.
     */
    override suspend fun obtenerConfiguracionGlobal(): Map<String, Any> {
        return try {
            val snapshot = db.getReference("configuracion_global").get().await()
            if (snapshot.exists()) {
                val map = mutableMapOf<String, Any>()
                snapshot.children.forEach { child ->
                    val k = child.key
                    val v = child.value
                    if (k != null && v != null) map[k] = v
                }
                map
            } else emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
```

---

## FASE 7: Arquitectura de Navegación y Punto de Entrada

### Paso 7.1: Rutas Selladas de Navegación (`Screen.kt`)

Define todas las pantallas y destinos de la aplicación móvil mediante una clase sellada (`sealed class`), evitando cadenas mágicas ("magic strings") y centralizando la construcción de argumentos dinámicos (como el `alertaId`).

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/navigation/Screen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.navigation

/**
 * Jerarquía sellada que define todas las rutas de navegación de la aplicación Cuna Segura.
 *
 * Previene el uso de cadenas mágicas y facilita el paso seguro de argumentos tipados en Jetpack Compose Navigation.
 *
 * @property route Cadena de plantilla que identifica la pantalla en el NavHost.
 */
sealed class Screen(val route: String) {
    /** Pantalla inicial de verificación de sesión y redirección. */
    object Splash : Screen("splash")
    /** Pantalla de inicio de sesión con correo y contraseña. */
    object Login : Screen("login")
    /** Pantalla principal del ciudadano con barra de navegación inferior. */
    object Home : Screen("home")
    /** Pantalla de emergencia activa en curso con cronómetro de gracia. */
    object EmergencyActive : Screen("emergency_active/{alertaId}") {
        /** Construye la ruta con el identificador concreto de la alerta. */
        fun createRoute(alertaId: Int) = "emergency_active/$alertaId"
    }
    /** Pantalla de gestión de contactos de emergencia. */
    object Contacts : Screen("contacts")
    /** Pantalla de vinculación y estado de SmartWatch y Smart TV. */
    object Devices : Screen("devices")
    /** Pantalla de mapeo de toques para reloj Wear OS. */
    object WatchConfig : Screen("watch_config")
    /** Pantalla de vinculación comunitaria de Smart TV mediante PIN o QR. */
    object TvConfig : Screen("tv_config")
    /** Pantalla de visualización de mapa comunitario en vivo. */
    object CommunityMap : Screen("community_map")
    /** Panel de control maestro para administradores. */
    object AdminPanel : Screen("admin_panel")
    /** Pantalla de registro de nuevos usuarios ciudadanos. */
    object Register : Screen("register")
    /** Pantalla de exploración y adhesión a redes vecinales. */
    object Networks : Screen("networks")
}
```

---

### Paso 7.2: Grafo de Navegación Composable (`NavGraph.kt`)

Gestiona el enrutador raíz `NavHost`, registrando cada pantalla composable, sus argumentos y callbacks de transición. Además, escucha eventos externos o notificaciones de emergencia entrantes (`startAlertaId`) para redirigir inmediatamente al usuario a la pantalla de auxilio activa.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/navigation/NavGraph.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import mx.edu.utng.cunasegura.presentation.admin.AdminPanelScreen
import mx.edu.utng.cunasegura.presentation.contacts.ContactsScreen
import mx.edu.utng.cunasegura.presentation.login.LoginScreen
import mx.edu.utng.cunasegura.presentation.splash.SplashScreen
import mx.edu.utng.cunasegura.presentation.home.HomeScreen
import mx.edu.utng.cunasegura.presentation.emergency.EmergencyActiveScreen
import mx.edu.utng.cunasegura.presentation.devices.DevicesScreen
import mx.edu.utng.cunasegura.presentation.watchconfig.WatchConfigScreen
import mx.edu.utng.cunasegura.presentation.tvconfig.TvConfigScreen
import mx.edu.utng.cunasegura.presentation.map.CommunityMapScreen
import mx.edu.utng.cunasegura.presentation.login.RegisterScreen

/**
 * Grafo de navegación principal de la aplicación móvil Cuna Segura.
 *
 * Configura el [NavHost] con todas las rutas composables de la aplicación y gestiona
 * el redireccionamiento instantáneo ante eventos de alerta SOS externos ([startAlertaId]).
 *
 * @param navController Controlador de navegación de Jetpack Compose.
 * @param startAlertaId Identificador de alerta SOS recibido por Notificación o Intent externo.
 * @param onAlertaHandled Callback invocado al consumir el ID de alerta entrante.
 */
@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    startAlertaId: Int? = null,
    onAlertaHandled: () -> Unit = {}
) {
    androidx.compose.runtime.LaunchedEffect(startAlertaId) {
        if (startAlertaId != null) {
            navController.navigate(Screen.EmergencyActive.createRoute(startAlertaId))
            onAlertaHandled()
        }
    }

    NavHost(navController = navController, startDestination = Screen.Splash.route) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Screen.AdminPanel.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onAdminSuccess = {
                    navController.navigate(Screen.AdminPanel.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
                onRegisterClick = {
                    navController.navigate(Screen.Register.route)
                }
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                onRegisterSuccess = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        // Panel de administración (solo admins)
        composable(Screen.AdminPanel.route) {
            mx.edu.utng.cunasegura.presentation.main.MainAdminScreen(rootNavController = navController)
        }

        // MainUserScreen handles Home, Contacts, Devices, and Map internally
        composable(Screen.Home.route) {
            mx.edu.utng.cunasegura.presentation.main.MainUserScreen(rootNavController = navController)
        }

        // EmergencyActiveScreen con argumento alertaId
        composable(
            route = Screen.EmergencyActive.route,
            arguments = listOf(
                navArgument("alertaId") {
                    type = NavType.IntType
                }
            )
        ) { backStackEntry ->
            val alertaId = backStackEntry.arguments?.getInt("alertaId") ?: 0
            EmergencyActiveScreen(
                alertaId = alertaId,
                onBackToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.WatchConfig.route) {
            WatchConfigScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.TvConfig.route) {
            TvConfigScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Networks.route) {
            mx.edu.utng.cunasegura.presentation.networks.NetworksScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

---

### Paso 7.3: Actividad Principal y Enrutamiento de Emergencia (`MainActivity.kt`)

Implementa la arquitectura Single Activity (`ComponentActivity`), gestiona el inicio Edge-to-Edge, la solicitud dinámica de permisos (GPS fino/aproximado, llamadas 911, envío de SMS y notificaciones push) e inicializa el servicio en primer plano `LocationTrackerService`.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/MainActivity.kt`:
```kotlin
package mx.edu.utng.cunasegura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import mx.edu.utng.cunasegura.presentation.navigation.NavGraph
import mx.edu.utng.cunasegura.ui.theme.CunaSeguraTheme

import mx.edu.utng.cunasegura.data.local.prefs.PreferencesManager
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

/**
 * Actividad principal de la aplicación Cuna Segura (Dolores Hidalgo).
 *
 * Responsabilidades:
 * - Servir como punto de entrada (Single Activity Architecture) para Jetpack Compose.
 * - Gestionar la solicitud en tiempo de ejecución de permisos críticos (Ubicación GPS precisa, llamada telefónica directa, envío de SMS y notificaciones).
 * - Procesar intents entrantes con identificadores de alerta activa ([startAlertaId]) provenientes de notificaciones push o servicios en segundo plano.
 * - Inicializar el servicio de rastreo GPS en primer plano ([mx.edu.utng.cunasegura.data.location.LocationTrackerService]).
 */
class MainActivity : ComponentActivity() {
    
    /**
     * Estado observable que almacena el ID de alerta activa entrante para navegación directa a la pantalla de emergencia.
     */
    private var startAlertaId by mutableStateOf<Int?>(null)

    /**
     * Inicializa la actividad, configura el diseño Edge-to-Edge, solicita permisos críticos,
     * inicia el servicio en primer plano e infla el grafo de navegación Compose.
     *
     * @param savedInstanceState Estado previamente guardado de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Solicitar permisos de llamada y SMS necesarios para Wear OS y alertas
        solicitarPermisosEmergencia()

        // Procesar intent inicial si fue lanzado por notificación o deep link
        val alertaId = intent.getIntExtra("EXTRA_ALERTA_ID", -1)
        if (alertaId != -1) {
            startAlertaId = alertaId
        }

        // Iniciar servicio de geolocalización y escucha de alertas en primer plano
        try {
            val serviceIntent = Intent(this, mx.edu.utng.cunasegura.data.location.LocationTrackerService::class.java)
            ContextCompat.startForegroundService(this, serviceIntent)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error al iniciar LocationTrackerService: ${e.message}")
        }

        setContent {
            CunaSeguraTheme {
                NavGraph(
                    startAlertaId = startAlertaId,
                    onAlertaHandled = { startAlertaId = null }
                )
            }
        }
    }

    /**
     * Recibe nuevos intents cuando la actividad ya se encuentra en ejecución (SingleTop / Re-launch).
     *
     * @param intent El nuevo intent recibido con posibles extras de alerta.
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val alertaId = intent.getIntExtra("EXTRA_ALERTA_ID", -1)
        if (alertaId != -1) {
            startAlertaId = alertaId
        }
    }

    /**
     * Verifica y solicita de manera agrupada los permisos de tiempo de ejecución indispensables
     * para el funcionamiento del sistema de alerta ciudadana (GPS, llamadas 911, SMS SOS y notificaciones).
     */
    private fun solicitarPermisosEmergencia() {
        val permisos = mutableListOf<String>()
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permisos.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permisos.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            permisos.add(Manifest.permission.CALL_PHONE)
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            permisos.add(Manifest.permission.SEND_SMS)
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permisos.add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }

        if (permisos.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permisos.toTypedArray(), 101)
        }
    }
}
```

---

## FASE 8: Flujo de Bienvenida y Autenticación

### Paso 8.1: Pantalla de Bienvenida (`SplashScreen.kt`)

Verifica la sesión activa en Firebase Auth y SQLite Room, evaluando si el usuario tiene rol `admin`, `usuario` o si su cuenta fue suspendida para redirigirlo adecuadamente con animación de pulso continuo.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/splash/SplashScreen.kt`:
```kotlin
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
```

---

### Paso 8.2: Inicio de Sesión (`LoginViewModel.kt`, `LoginScreen.kt`)

Valida credenciales contra Firebase Authentication y comprueba el estado de la cuenta en `usuarios/{uid}/estado`.

> 📋 **INSTRUCCIÓN:** Copia el ViewModel de `app/src/main/java/mx/edu/utng/cunasegura/presentation/login/LoginViewModel.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.login

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.usecase.GuardarUsuarioUseCase
import mx.edu.utng.cunasegura.domain.usecase.LimpiarSesionLocalUseCase

/**
 * Estado inmutable de la interfaz de usuario para la pantalla de inicio de sesión.
 *
 * @property correo Correo electrónico ingresado.
 * @property password Contraseña de acceso.
 * @property isLoading Bandera de progreso durante la autenticación remota.
 * @property errorMessage Mensaje de error de credenciales o red.
 * @property navigateToHome Bandera de redirección al flujo de vecino común.
 * @property navigateToAdmin Bandera de redirección al panel de administración.
 * @property navigateToRegister Bandera de navegación al formulario de registro.
 */
data class LoginUiState(
    val correo: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val navigateToHome: Boolean = false,
    val navigateToAdmin: Boolean = false,
    val navigateToRegister: Boolean = false
)

/**
 * ViewModel encargado del flujo de autenticación de usuarios y administradores.
 *
 * Valida credenciales contra Firebase Authentication, comprueba el estado de la cuenta en Realtime Database,
 * limpia la sesión local previa y persiste el usuario activo en SQLite Room.
 *
 * @property guardarUsuarioUseCase Caso de uso para persistencia local.
 * @property limpiarSesionLocalUseCase Caso de uso para saneamiento de sesión.
 * @property context Contexto de la aplicación.
 */
class LoginViewModel(
    private val guardarUsuarioUseCase: GuardarUsuarioUseCase,
    private val limpiarSesionLocalUseCase: LimpiarSesionLocalUseCase,
    private val context: Context
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance().apply {
        firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
    }
    private val db = FirebaseDatabase.getInstance()
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el campo de correo y limpia mensajes de error previos.
     */
    fun onCorreoChange(value: String) {
        _uiState.value = _uiState.value.copy(correo = value, errorMessage = null)
    }

    /**
     * Actualiza el campo de contraseña.
     */
    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    /**
     * Activa el evento de navegación hacia la pantalla de registro.
     */
    fun onNavigateToRegister() {
        _uiState.value = _uiState.value.copy(navigateToRegister = true)
    }
    
    /**
     * Restablece la bandera tras completar la navegación a registro.
     */
    fun onRegisterNavigated() {
        _uiState.value = _uiState.value.copy(navigateToRegister = false)
    }

    /**
     * Ejecuta el proceso de inicio de sesión con Firebase Auth y realiza el ruteo condicional por rol.
     */
    fun onLoginClick() {
        val correo = _uiState.value.correo.trim()
        val password = _uiState.value.password

        if (correo.isBlank() || !correo.contains("@")) {
            _uiState.value = _uiState.value.copy(errorMessage = "Ingresa un correo válido")
            return
        }
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Ingresa la contraseña")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val result = auth.signInWithEmailAndPassword(correo, password).await()
                val firebaseUser = result.user
                
                if (firebaseUser != null) {
                    // Fetch real user data from Realtime Database
                    val snapshot = db.getReference("usuarios").child(firebaseUser.uid).get().await()
                    val estadoDb = snapshot.child("estado").getValue(String::class.java) ?: "activo"
                    
                    if (estadoDb == "bloqueado" || estadoDb == "suspendido") {
                        auth.signOut()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Tu cuenta ha sido BLOQUEADA por el Administrador Global. Contacta a soporte."
                        )
                        return@launch
                    }

                    val nombre = snapshot.child("nombre").getValue(String::class.java) ?: firebaseUser.displayName ?: correo.substringBefore("@")
                    val telefono = snapshot.child("telefono").getValue(String::class.java) ?: ""
                    val rolDb = snapshot.child("rol").getValue(String::class.java) ?: "usuario"
                    val netId = snapshot.child("networkId").getValue(String::class.java) ?: ""
                    
                    val ADMIN_EMAIL = "admin@cunasegura.com"
                    val esAdmin = (rolDb == "admin_global") || (rolDb == "system_admin") || (firebaseUser.email == ADMIN_EMAIL)
                    val rolFinal = if (esAdmin) "admin" else "usuario"

                    // Clear previous session so Room LIMIT 1 works correctly for this new user
                    limpiarSesionLocalUseCase()

                    // Save user to local Room DB
                    val usuario = Usuario(
                        id = 0,
                        nombre = nombre,
                        telefono = telefono,
                        correo = correo,
                        password = "",
                        rol = rolFinal,
                        networkId = netId
                    )
                    guardarUsuarioUseCase(usuario)

                    if (esAdmin) {
                        _uiState.value = _uiState.value.copy(isLoading = false, navigateToAdmin = true)
                    } else {
                        _uiState.value = _uiState.value.copy(isLoading = false, navigateToHome = true)
                    }
                }
            } catch (e: FirebaseAuthInvalidUserException) {
                if (correo == "admin@cunasegura.com") {
                    try {
                        val createResult = auth.createUserWithEmailAndPassword("admin@cunasegura.com", "123456789").await()
                        val newUser = createResult.user
                        if (newUser != null) {
                            val adminMap = mapOf(
                                "nombre" to "Administrador Global",
                                "correo" to "admin@cunasegura.com",
                                "telefono" to "0000000000",
                                "rol" to "admin_global",
                                "estado" to "activo"
                            )
                            db.getReference("usuarios").child(newUser.uid).setValue(adminMap).await()
                            limpiarSesionLocalUseCase()
                            guardarUsuarioUseCase(Usuario(id = 0, nombre = "Administrador Global", telefono = "0000000000", correo = "admin@cunasegura.com", password = "", rol = "admin"))
                            _uiState.value = _uiState.value.copy(isLoading = false, navigateToAdmin = true)
                            return@launch
                        }
                    } catch (ex: Exception) {
                        Log.e("LoginViewModel", "Error auto-creando admin", ex)
                    }
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "No existe una cuenta con ese correo. ¿Quieres registrarte?"
                )
            } catch (e: FirebaseAuthInvalidCredentialsException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Contraseña incorrecta. Verifica tus datos."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error de conexión: ${e.localizedMessage ?: "Sin conexión a internet"}"
                )
            }
        }
    }
}

/**
 * Fábrica de ViewModel que inyecta los casos de uso correspondientes mediante [AppModule].
 */
class LoginViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(
                AppModule.provideGuardarUsuarioUseCase(context),
                AppModule.provideLimpiarSesionLocalUseCase(context),
                context
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
```

> 📋 **INSTRUCCIÓN:** Copia la UI composable de `app/src/main/java/mx/edu/utng/cunasegura/presentation/login/LoginScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.login

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

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onAdminSuccess: () -> Unit,
    onRegisterClick: () -> Unit
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
    LaunchedEffect(uiState.navigateToRegister) {
        if (uiState.navigateToRegister) {
            onRegisterClick()
            viewModel.onRegisterNavigated()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondary
                    )
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
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(androidx.compose.material3.MaterialTheme.colorScheme.onPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Logo Cuna Segura",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(52.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "CUNA SEGURA",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp
            )
            Text(
                text = "ALERTA CIUDADANA",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                fontSize = 13.sp,
                letterSpacing = 2.sp
            )
            Text(
                text = "DOLORES HIDALGO",
                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                fontSize = 12.sp,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            // ── Card de Login ──────────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Iniciar Sesión",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Campo correo
                    OutlinedTextField(
                        value = uiState.correo,
                        onValueChange = { viewModel.onCorreoChange(it) },
                        label = { Text("Correo electrónico") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                            Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
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
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(22.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Ingresar",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // ── Botón de Registro ──────────────────────────────────
                    Text(
                        text = "¿No tienes cuenta? Regístrate aquí",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { viewModel.onNavigateToRegister() }
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
```

---

### Paso 8.3: Registro de Ciudadanos (`RegisterViewModel.kt`, `RegisterScreen.kt`)

Crea la cuenta en Firebase Auth, inicializa el perfil en Realtime Database y almacena la sesión activa en SQLite Room.

> 📋 **INSTRUCCIÓN:** Copia el ViewModel de `app/src/main/java/mx/edu/utng/cunasegura/presentation/login/RegisterViewModel.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.usecase.GuardarUsuarioUseCase
import mx.edu.utng.cunasegura.domain.usecase.LimpiarSesionLocalUseCase

/**
 * Estado inmutable del formulario de registro de nuevos ciudadanos.
 *
 * @property nombre Nombre completo del usuario.
 * @property telefono Teléfono celular de 10 dígitos.
 * @property correo Correo electrónico para la cuenta.
 * @property password Contraseña elegida.
 * @property confirmPassword Confirmación de contraseña.
 * @property isLoading Bandera de progreso durante el alta en Firebase.
 * @property errorMessage Mensaje de validación o error de red.
 * @property registerSuccess Bandera que indica éxito en la creación de cuenta.
 */
data class RegisterUiState(
    val nombre: String = "",
    val telefono: String = "",
    val correo: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val registerSuccess: Boolean = false
)

/**
 * ViewModel encargado del registro de nuevos usuarios en el sistema.
 *
 * Crea la cuenta en Firebase Authentication, actualiza el perfil en Realtime Database y almacena
 * la sesión activa en el repositorio local.
 *
 * @property guardarUsuarioUseCase Caso de uso para persistir los datos del usuario.
 * @property limpiarSesionLocalUseCase Caso de uso para limpiar sesiones anteriores.
 */
class RegisterViewModel(
    private val guardarUsuarioUseCase: GuardarUsuarioUseCase,
    private val limpiarSesionLocalUseCase: LimpiarSesionLocalUseCase
) : ViewModel() {

    private val auth = FirebaseAuth.getInstance().apply {
        firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
    }
    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    /**
     * Actualiza el nombre ingresado.
     */
    fun onNombreChange(value: String) {
        _uiState.value = _uiState.value.copy(nombre = value, errorMessage = null)
    }

    /**
     * Actualiza y normaliza el número de teléfono limitándolo a 10 dígitos numéricos.
     */
    fun onTelefonoChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(10)
        _uiState.value = _uiState.value.copy(telefono = digitsOnly, errorMessage = null)
    }

    /**
     * Actualiza el correo electrónico ingresado.
     */
    fun onCorreoChange(value: String) {
        _uiState.value = _uiState.value.copy(correo = value, errorMessage = null)
    }

    /**
     * Actualiza la contraseña.
     */
    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, errorMessage = null)
    }

    /**
     * Actualiza el campo de confirmación de contraseña.
     */
    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, errorMessage = null)
    }

    /**
     * Valida los campos del formulario y ejecuta la creación de cuenta en Firebase y Room.
     */
    fun onRegisterClick() {
        val state = _uiState.value
        val nombre = state.nombre.trim()
        val telefono = state.telefono
        val correo = state.correo.trim()
        val password = state.password
        val confirm = state.confirmPassword

        if (nombre.isBlank()) {
            _uiState.value = state.copy(errorMessage = "Ingresa tu nombre completo")
            return
        }
        if (telefono.length < 10) {
            _uiState.value = state.copy(errorMessage = "Ingresa un número de teléfono de 10 dígitos")
            return
        }
        if (correo.isBlank() || !correo.contains("@")) {
            _uiState.value = state.copy(errorMessage = "Ingresa un correo válido")
            return
        }
        if (password.length < 6) {
            _uiState.value = state.copy(errorMessage = "La contraseña debe tener al menos 6 caracteres")
            return
        }
        if (password != confirm) {
            _uiState.value = state.copy(errorMessage = "Las contraseñas no coinciden")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            try {
                // 1. Create user in Firebase Auth
                val result = auth.createUserWithEmailAndPassword(correo, password).await()
                val firebaseUser = result.user
                
                if (firebaseUser != null) {
                    // 2. Set display name
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(nombre)
                        .build()
                    firebaseUser.updateProfile(profileUpdates).await()
                    
                    // 3. Save user data to Firebase Realtime Database
                    val db = FirebaseDatabase.getInstance()
                    val userData = mapOf(
                        "uid" to firebaseUser.uid,
                        "nombre" to nombre,
                        "telefono" to telefono,
                        "correo" to correo,
                        "rol" to "usuario",
                        "estado" to "activo",
                        "networkId" to firebaseUser.uid,
                        "creadoEn" to System.currentTimeMillis()
                    )
                    db.getReference("usuarios").child(firebaseUser.uid).setValue(userData).await()
                    
                    // 4. Save to local Room for offline access (clear previous session first)
                    limpiarSesionLocalUseCase()
                    val nuevoUsuario = Usuario(
                        id = 0,
                        nombre = nombre,
                        telefono = telefono,
                        correo = correo,
                        password = "",
                        rol = "usuario"
                    )
                    guardarUsuarioUseCase(nuevoUsuario)
                    
                    _uiState.value = _uiState.value.copy(isLoading = false, registerSuccess = true)
                }
            } catch (e: FirebaseAuthUserCollisionException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Ya existe una cuenta con ese correo. Inicia sesión."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error: ${e.localizedMessage ?: "No se pudo crear la cuenta"}"
                )
            }
        }
    }
}

/**
 * Fábrica para instanciar [RegisterViewModel] resolviendo casos de uso mediante [AppModule].
 */
class RegisterViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterViewModel(
                AppModule.provideGuardarUsuarioUseCase(context),
                AppModule.provideLimpiarSesionLocalUseCase(context)
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
```

> 📋 **INSTRUCCIÓN:** Copia la UI composable de `app/src/main/java/mx/edu/utng/cunasegura/presentation/login/RegisterScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
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

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: RegisterViewModel = viewModel(
        factory = RegisterViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()
    
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.registerSuccess) {
        if (uiState.registerSuccess) {
            onRegisterSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.colorScheme.secondary
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header with Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Crear Cuenta",
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(androidx.compose.material3.MaterialTheme.colorScheme.onPrimary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Logo Cuna Segura",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Card de Registro
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    OutlinedTextField(
                        value = uiState.nombre,
                        onValueChange = { viewModel.onNombreChange(it) },
                        label = { Text("Nombre Completo") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.telefono,
                        onValueChange = { viewModel.onTelefonoChange(it) },
                        label = { Text("Teléfono (10 dígitos)") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.correo,
                        onValueChange = { viewModel.onCorreoChange(it) },
                        label = { Text("Correo Electrónico") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.password,
                        onValueChange = { viewModel.onPasswordChange(it) },
                        label = { Text("Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = { viewModel.onConfirmPasswordChange(it) },
                        label = { Text("Confirmar Contraseña") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(
                                    if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

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

                    Button(
                        onClick = { viewModel.onRegisterClick() },
                        enabled = !uiState.isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                        } else {
                            Text(text = "Registrarse", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
```

---

## FASE 9: Módulos Funcionales del Ciudadano

### Paso 9.1: Panel Principal y Botón de Pánico (`HomeViewModel.kt`, `HomeScreen.kt`)

Presenta el botón de pánico SOS central, valida políticas de seguridad (`esperarDiasNuevos` y ventana anti-falsa alarma `tiempoAntiFalsa`), sincroniza el estado de la red comunitaria y activa el flujo de emergencia.

> 📋 **INSTRUCCIÓN:** Copia el ViewModel de `app/src/main/java/mx/edu/utng/cunasegura/presentation/home/HomeViewModel.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Alerta
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.usecase.ActivarAlertaUseCase
import mx.edu.utng.cunasegura.domain.usecase.ObtenerUsuarioActualUseCase

/**
 * Estado inmutable de la pantalla principal del ciudadano.
 *
 * @property usuario Usuario autenticado en sesión.
 * @property alertaActiva Alerta SOS emitida actualmente, si existe.
 * @property isLoading Bandera de procesamiento durante la activación o consulta.
 * @property errorMessage Mensaje de error o restricción de políticas de seguridad.
 * @property alertaIdGenerada Identificador de la alerta creada para redirección inmediata.
 * @property networkNombre Nombre de la red comunitaria a la que pertenece el vecino.
 * @property tiempoAntiFalsa Segundos configurados para la ventana de gracia anti-falsas alarmas.
 * @property esperarDiasNuevos Días de antigüedad requeridos antes de permitir emitir alertas.
 */
data class HomeUiState(
    val usuario: Usuario? = null,
    val alertaActiva: Alerta? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val alertaIdGenerada: Int? = null,
    val networkNombre: String = "Red Comunitaria",
    val tiempoAntiFalsa: Double = 5.0,
    val esperarDiasNuevos: Int = 0
)

/**
 * ViewModel del panel principal de usuario.
 *
 * Administra el botón de pánico central, valida políticas de seguridad como la antigüedad de la cuenta
 * ([esperarDiasNuevos]) y coordina la activación de alertas locales y remotas.
 *
 * @property obtenerUsuarioActualUseCase Caso de uso para obtener el usuario activo.
 * @property activarAlertaUseCase Caso de uso para emitir alertas de auxilio.
 * @property context Contexto de la aplicación.
 */
class HomeViewModel(
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val activarAlertaUseCase: ActivarAlertaUseCase,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargarUsuario()
    }

    /**
     * Carga el perfil del usuario local, sincroniza datos de Firebase y consulta parámetros de red.
     */
    fun cargarUsuario() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = obtenerUsuarioActualUseCase()
            if (user != null) {
                var netName = "Red Comunitaria"
                var antiFalsa = 5.0
                var diasNuevos = 0
                try {
                    val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        val db = com.google.firebase.database.FirebaseDatabase.getInstance()
                        val uSnap = db.getReference("usuarios").child(uid).get().await()
                        val netId = uSnap.child("networkId").getValue(String::class.java) ?: uid
                        
                        val netSnap = db.getReference("redes").child(netId).get().await()
                        if (netSnap.exists()) {
                            netName = netSnap.child("nombre").getValue(String::class.java) ?: netName
                            antiFalsa = netSnap.child("tiempoAntiFalsa").getValue(Double::class.java) ?: 5.0
                            diasNuevos = netSnap.child("esperarDiasNuevos").getValue(Int::class.java) ?: 0
                        }
                    }
                } catch (e: Exception) {
                    // Ignore and use defaults
                }

                _uiState.value = _uiState.value.copy(
                    usuario = user,
                    networkNombre = netName,
                    tiempoAntiFalsa = antiFalsa,
                    esperarDiasNuevos = diasNuevos,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Activa una alerta de pánico SOS tras validar las directivas de seguridad de la red.
     *
     * @param lat Latitud GPS actual.
     * @param lon Longitud GPS actual.
     * @param tipo Tipo de emergencia ("panico", "medica", "sos").
     */
    fun activarAlerta(lat: Double = 0.0, lon: Double = 0.0, tipo: String = "panico") {
        val user = _uiState.value.usuario ?: return
        
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                // Validación de días de antigüedad requeridos por la red
                if (_uiState.value.esperarDiasNuevos > 0) {
                    val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                    if (uid != null) {
                        val db = com.google.firebase.database.FirebaseDatabase.getInstance()
                        val uSnap = db.getReference("usuarios").child(uid).get().await()
                        val creadoEn = uSnap.child("creadoEn").getValue(Long::class.java) ?: System.currentTimeMillis()
                        val diasPasados = (System.currentTimeMillis() - creadoEn) / (1000 * 60 * 60 * 24)
                        if (diasPasados < _uiState.value.esperarDiasNuevos) {
                            val diasFaltantes = _uiState.value.esperarDiasNuevos - diasPasados
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                errorMessage = "Por seguridad comunitaria, debes esperar $diasFaltantes día(s) más para poder emitir alertas de pánico."
                            )
                            return@launch
                        }
                    }
                }

                val idGenerada = activarAlertaUseCase(
                    usuarioId = user.id,
                    tipo = tipo,
                    latitud = lat,
                    longitud = lon
                )
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    alertaIdGenerada = idGenerada
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al activar alerta: ${e.message}"
                )
            }
        }
    }

    /**
     * Limpia la referencia a la alerta generada tras navegar a la pantalla de emergencia activa.
     */
    fun resetAlertaGenerada() {
        _uiState.value = _uiState.value.copy(alertaIdGenerada = null)
    }

    /**
     * Limpia mensajes de error en la UI.
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
}

/**
 * Fábrica para instanciar [HomeViewModel] resolviendo dependencias desde [AppModule].
 */
class HomeViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                AppModule.provideObtenerUsuarioActualUseCase(context),
                AppModule.provideActivarAlertaUseCase(context),
                context
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
```

> 📋 **INSTRUCCIÓN:** Copia la UI composable de `app/src/main/java/mx/edu/utng/cunasegura/presentation/home/HomeScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun HomeScreen(
    onNavigateToEmergency: (Int) -> Unit,
    onNavigateToContacts: () -> Unit = {},
    onNavigateToNetworks: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: HomeViewModel = viewModel(
        factory = HomeViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
        viewModel.cargarUsuario()
    }

    // Redirección al activar alerta
    LaunchedEffect(uiState.alertaIdGenerada) {
        uiState.alertaIdGenerada?.let { id ->
            onNavigateToEmergency(id)
            viewModel.resetAlertaGenerada()
        }
    }

    // Animación de pulso continuo del botón de pánico
    val infiniteTransition = rememberInfiniteTransition(label = "SosPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ButtonPulse"
    )

    fun dispararAlerta(tipo: String = "panico") {
        var lat = 21.1561
        var lon = -100.9325
        if (hasLocationPermission) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (loc != null) {
                    lat = loc.latitude
                    lon = loc.longitude
                }
            } catch (e: SecurityException) {
                // Ignore
            }
        }
        viewModel.activarAlerta(lat, lon, tipo)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header de Bienvenida ──────────────────────────────────────────
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hola, ${uiState.usuario?.nombre ?: "Vecino"}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Group,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = uiState.networkNombre,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Botón Central de Pánico SOS ───────────────────────────────────
            Text(
                text = "Presiona para enviar auxilio inmediato a tu red",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .size(230.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Anillo exterior de onda expansiva
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f))
                )
                // Anillo intermedio
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.error.copy(alpha = 0.35f))
                )
                // Botón sólido interior
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.error,
                                    MaterialTheme.colorScheme.error.copy(alpha = 0.85f)
                                )
                            )
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !uiState.isLoading
                        ) {
                            dispararAlerta("panico")
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onError,
                            modifier = Modifier.size(42.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "SOS",
                                tint = MaterialTheme.colorScheme.onError,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "SOS",
                                color = MaterialTheme.colorScheme.onError,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 2.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            // ── Botones de Emergencia Temática ────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { dispararAlerta("medica") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(
                        text = "Médica",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Button(
                    onClick = { dispararAlerta("seguridad") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                ) {
                    Text(
                        text = "Seguridad",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            // ── Mensajes de Error o Alerta ────────────────────────────────────
            if (uiState.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
```

---

### Paso 9.2: Alerta Activa y Cuenta Regresiva (`EmergencyViewModel.kt`, `EmergencyActiveScreen.kt`)

Despliega el estado de la emergencia activa, contactos notificados y una cuenta regresiva de 8 segundos con opción a cancelar el evento de auxilio antes de difundir la alerta general a la comunidad.

> 📋 **INSTRUCCIÓN:** Copia el ViewModel de `app/src/main/java/mx/edu/utng/cunasegura/presentation/emergency/EmergencyViewModel.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.emergency

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Alerta
import mx.edu.utng.cunasegura.domain.model.ContactoEmergencia
import mx.edu.utng.cunasegura.domain.usecase.CancelarAlertaUseCase
import mx.edu.utng.cunasegura.domain.usecase.ObtenerContactosUseCase

/**
 * Estado inmutable de la pantalla de emergencia activa.
 *
 * @property segundosRestantes Segundos que restan en el cronómetro de gracia.
 * @property cuentaRegresivaActiva Indica si el cronómetro está corriendo.
 * @property alertaConfirmada Indica si se agotó el tiempo y la alerta fue ratificada.
 * @property alertaCancelada Indica si el usuario canceló la alerta a tiempo.
 * @property contactos Lista de contactos de confianza notificados.
 * @property isLoading Bandera de procesamiento.
 */
data class EmergencyUiState(
    val segundosRestantes: Int = 8,
    val cuentaRegresivaActiva: Boolean = true,
    val alertaConfirmada: Boolean = false,
    val alertaCancelada: Boolean = false,
    val contactos: List<ContactoEmergencia> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * ViewModel que gestiona la cuenta regresiva de cancelación y confirmación de la alerta SOS.
 *
 * @property alertaId Identificador único de la alerta activa.
 * @property cancelarAlertaUseCase Caso de uso para anular la alerta.
 * @property obtenerContactosUseCase Caso de uso para obtener los contactos notificados.
 */
class EmergencyViewModel(
    private val alertaId: Int,
    private val cancelarAlertaUseCase: CancelarAlertaUseCase,
    private val obtenerContactosUseCase: ObtenerContactosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null

    init {
        iniciarCuentaRegresiva()
        cargarContactos()
    }

    /**
     * Inicia un temporizador regresivo de 8 segundos permitiendo cancelar una falsa alarma.
     */
    private fun iniciarCuentaRegresiva() {
        countdownJob = viewModelScope.launch {
            for (i in 8 downTo 1) {
                _uiState.value = _uiState.value.copy(segundosRestantes = i)
                delay(1000)
            }
            _uiState.value = _uiState.value.copy(
                segundosRestantes = 0,
                cuentaRegresivaActiva = false,
                alertaConfirmada = true
            )
        }
    }

    /**
     * Consulta los contactos de emergencia asociados para mostrar el estatus del auxilio.
     */
    private fun cargarContactos() {
        viewModelScope.launch {
            obtenerContactosUseCase().collect { lista ->
                _uiState.value = _uiState.value.copy(contactos = lista)
            }
        }
    }

    /**
     * Cancela la alerta activa y detiene el temporizador de cuenta regresiva.
     */
    fun cancelarAlerta() {
        countdownJob?.cancel()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            cancelarAlertaUseCase(alertaId)
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                alertaCancelada = true,
                cuentaRegresivaActiva = false
            )
        }
    }
}

/**
 * Fábrica para instanciar [EmergencyViewModel] recibiendo el identificador de la alerta.
 */
class EmergencyViewModelFactory(
    private val alertaId: Int,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EmergencyViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EmergencyViewModel(
                alertaId = alertaId,
                cancelarAlertaUseCase = AppModule.provideCancelarAlertaUseCase(context),
                obtenerContactosUseCase = AppModule.provideObtenerContactosUseCase(context)
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
```

> 📋 **INSTRUCCIÓN:** Copia la UI composable de `app/src/main/java/mx/edu/utng/cunasegura/presentation/emergency/EmergencyActiveScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.emergency

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun EmergencyActiveScreen(
    alertaId: Int,
    onBackToHome: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: EmergencyViewModel = viewModel(
        factory = EmergencyViewModelFactory(alertaId, context)
    )
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.alertaCancelada) {
        if (uiState.alertaCancelada) {
            onBackToHome()
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "EmergencyPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "EmergencyScale"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono de Alerta Activa
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.error),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(64.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = if (uiState.cuentaRegresivaActiva) "ENVIANDO ALERTA SOS..." else "¡ALERTA ACTIVA ENVIADA!",
                color = MaterialTheme.colorScheme.onErrorContainer,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.cuentaRegresivaActiva) {
                Text(
                    text = "Tienes ${uiState.segundosRestantes} segundos para cancelar si fue una falsa alarma",
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "${uiState.segundosRestantes}",
                    fontSize = 54.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    text = "Tu ubicación GPS en vivo y solicitud de auxilio se transmiten a tus vecinos y contactos de emergencia.",
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.85f),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Botón Cancelar Alerta
            Button(
                onClick = { viewModel.cancelarAlerta() },
                enabled = !uiState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = null, tint = MaterialTheme.colorScheme.onError)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cancelar Alerta",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onError
                )
            }

            if (!uiState.cuentaRegresivaActiva) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = onBackToHome,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(
                        text = "Regresar al Inicio",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}
```

---

### Paso 9.3: Mapa Comunitario en Vivo (`MapViewModel.kt`, `CommunityMapScreen.kt`)

Integra el motor cartográfico **OpenStreetMap (osmdroid)** para renderizar sin costo de licencias la ubicación de vecinos conectados, el radio de cobertura comunitaria y los focos de alerta activos en Dolores Hidalgo.

> 📋 **INSTRUCCIÓN:** Copia el ViewModel de `app/src/main/java/mx/edu/utng/cunasegura/presentation/map/MapViewModel.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Alerta
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.usecase.ActivarAlertaUseCase
import mx.edu.utng.cunasegura.domain.usecase.ObtenerUsuarioActualUseCase

/**
 * Modelo de datos de un vecino conectado para representación en el mapa comunitario.
 *
 * @property uid Identificador único de Firebase.
 * @property nombre Nombre del vecino.
 * @property latitud Latitud GPS.
 * @property longitud Longitud GPS.
 * @property enAlerta Indica si el vecino tiene una emergencia activa.
 * @property ultimaActualizacion Marca temporal del último reporte de ubicación.
 */
data class VecinoLocation(
    val uid: String = "",
    val nombre: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val enAlerta: Boolean = false,
    val ultimaActualizacion: Long = 0L
)

/**
 * Estado inmutable de la pantalla de mapa comunitario.
 *
 * @property usuarioActual Perfil del usuario local.
 * @property vecinos Lista de vecinos conectados con coordenadas en tiempo real.
 * @property alertasActivas Historial de alertas vigentes dentro de la comunidad.
 * @property isLoading Bandera de sincronización con Firebase.
 * @property errorMessage Mensaje de error en caso de fallo.
 */
data class MapUiState(
    val usuarioActual: Usuario? = null,
    val vecinos: List<VecinoLocation> = emptyList(),
    val alertasActivas: List<Alerta> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

/**
 * ViewModel del mapa comunitario en vivo.
 *
 * Escucha las coordenadas de los vecinos de la red mediante ValueEventListeners en Realtime Database
 * y permite disparar alertas georreferenciadas.
 *
 * @property obtenerUsuarioActualUseCase Caso de uso para obtener el usuario activo.
 * @property activarAlertaUseCase Caso de uso para emitir alertas de emergencia.
 */
class MapViewModel(
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val activarAlertaUseCase: ActivarAlertaUseCase
) : ViewModel() {

    private val db = FirebaseDatabase.getInstance()
    private val _uiState = MutableStateFlow(MapUiState())
    val uiState: StateFlow<MapUiState> = _uiState.asStateFlow()

    private var vecinosListener: ValueEventListener? = null

    init {
        cargarDatos()
    }

    /**
     * Carga el usuario activo y suscribe la escucha en tiempo real de ubicaciones de vecinos en la red.
     */
    private fun cargarDatos() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = obtenerUsuarioActualUseCase()
            _uiState.value = _uiState.value.copy(usuarioActual = user)

            if (user != null) {
                // Obtener networkId de Firebase
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                var netId = uid
                if (uid.isNotEmpty()) {
                    try {
                        val snapshot = db.getReference("usuarios").child(uid).get().await()
                        netId = snapshot.child("networkId").getValue(String::class.java) ?: uid
                    } catch (e: Exception) {
                        // fallback to uid
                    }
                }

                // Escuchar ubicaciones en vivo de miembros de la red
                val vecinosRef = db.getReference("ubicaciones_red").child(netId)
                vecinosListener = object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val lista = mutableListOf<VecinoLocation>()
                        for (child in snapshot.children) {
                            val vUid = child.key ?: continue
                            if (vUid == uid) continue // No incluirse a sí mismo como vecino remoto

                            val nombre = child.child("nombre").getValue(String::class.java) ?: "Vecino"
                            val lat = child.child("lat").getValue(Double::class.java) ?: 0.0
                            val lon = child.child("lon").getValue(Double::class.java) ?: 0.0
                            val alerta = child.child("enAlerta").getValue(Boolean::class.java) ?: false
                            val time = child.child("timestamp").getValue(Long::class.java) ?: 0L

                            if (lat != 0.0 && lon != 0.0) {
                                lista.add(
                                    VecinoLocation(
                                        uid = vUid,
                                        nombre = nombre,
                                        latitud = lat,
                                        longitud = lon,
                                        enAlerta = alerta,
                                        ultimaActualizacion = time
                                    )
                                )
                            }
                        }
                        _uiState.value = _uiState.value.copy(vecinos = lista, isLoading = false)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = "Error al sincronizar vecinos: ${error.message}"
                        )
                    }
                }
                vecinosRef.addValueEventListener(vecinosListener!!)
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    /**
     * Dispara una alerta de pánico desde la interfaz del mapa comunitario.
     */
    fun dispararAlertaDesdeMapa(lat: Double, lon: Double, tipo: String = "panico") {
        val user = _uiState.value.usuarioActual ?: return
        viewModelScope.launch {
            try {
                activarAlertaUseCase(
                    usuarioId = user.id,
                    tipo = tipo,
                    latitud = lat,
                    longitud = lon
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error: ${e.message}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        vecinosListener?.let {
            db.getReference("ubicaciones_red").removeEventListener(it)
        }
    }
}

/**
 * Fábrica para instanciar [MapViewModel] inyectando casos de uso desde [AppModule].
 */
class MapViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapViewModel(
                AppModule.provideObtenerUsuarioActualUseCase(context),
                AppModule.provideActivarAlertaUseCase(context)
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
```

> 📋 **INSTRUCCIÓN:** Copia la UI composable de `app/src/main/java/mx/edu/utng/cunasegura/presentation/map/CommunityMapScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.map

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

@Composable
fun CommunityMapScreen(
    initialAlertLat: Double? = null,
    initialAlertLon: Double? = null,
    initialAlertUser: String? = null
) {
    val context = LocalContext.current
    val viewModel: MapViewModel = viewModel(
        factory = MapViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    // Configuración obligatoria de User Agent para osmdroid
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var locationOverlayRef by remember { mutableStateOf<MyLocationNewOverlay?>(null) }

    val doloresHidalgoCenter = GeoPoint(21.1561, -100.9325)

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(16.0)
                    controller.setCenter(
                        if (initialAlertLat != null && initialAlertLon != null)
                            GeoPoint(initialAlertLat, initialAlertLon)
                        else
                            doloresHidalgoCenter
                    )

                    val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(ctx), this)
                    locationOverlay.enableMyLocation()
                    overlays.add(locationOverlay)

                    mapViewRef = this
                    locationOverlayRef = locationOverlay
                }
            },
            update = { mapView ->
                // Actualizar marcadores de vecinos remotos
                val currentMarkers = mapView.overlays.filterIsInstance<Marker>()
                mapView.overlays.removeAll(currentMarkers)

                uiState.vecinos.forEach { vecino ->
                    val marker = Marker(mapView).apply {
                        position = GeoPoint(vecino.latitud, vecino.longitud)
                        title = vecino.nombre
                        snippet = if (vecino.enAlerta) "¡EMERGENCIA ACTIVA!" else "Vecino conectado"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mapView.overlays.add(marker)
                }

                // Marcador de alerta inicial entrante
                if (initialAlertLat != null && initialAlertLon != null) {
                    val alertMarker = Marker(mapView).apply {
                        position = GeoPoint(initialAlertLat, initialAlertLon)
                        title = "ALERTA: ${initialAlertUser ?: "Vecino"}"
                        snippet = "Foco de auxilio activo"
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    }
                    mapView.overlays.add(alertMarker)
                }
                mapView.invalidate()
            }
        )

        // ── Tarjeta Superior de Estado ────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
            ),
            elevation = CardDefaults.cardElevation(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mapa Comunitario",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${uiState.vecinos.size} vecinos conectados en tu red",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ── Controles de Zoom y Centrado ─────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 90.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    locationOverlayRef?.myLocation?.let { loc ->
                        mapViewRef?.controller?.animateTo(loc)
                    } ?: run {
                        mapViewRef?.controller?.animateTo(doloresHidalgoCenter)
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Mi Ubicación")
            }

            FloatingActionButton(
                onClick = { mapViewRef?.controller?.zoomIn() },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Acercar")
            }

            FloatingActionButton(
                onClick = { mapViewRef?.controller?.zoomOut() },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Alejar")
            }
        }
    }
}
```

---

### Paso 9.4: Directorio de Contactos de Confianza (`ContactsViewModel.kt`, `ContactsScreen.kt`)

Gestiona la agenda de hasta 5 contactos de auxilio con llamadas y SMS directos al 911 o familiares seleccionados.

> 📋 **INSTRUCCIÓN:** Copia el ViewModel de `app/src/main/java/mx/edu/utng/cunasegura/presentation/contacts/ContactsViewModel.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.contacts

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.ContactoEmergencia
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.usecase.AgregarContactoUseCase
import mx.edu.utng.cunasegura.domain.usecase.EliminarContactoUseCase
import mx.edu.utng.cunasegura.domain.usecase.ObtenerContactosUseCase
import mx.edu.utng.cunasegura.domain.usecase.ObtenerUsuarioActualUseCase

/**
 * Estado inmutable del directorio de contactos de auxilio.
 *
 * @property contactos Lista de contactos registrados (máximo 5).
 * @property usuario Usuario activo.
 * @property isLoading Bandera de carga.
 * @property errorMessage Mensaje de error.
 * @property successMessage Notificación de éxito al guardar o borrar.
 */
data class ContactsUiState(
    val contactos: List<ContactoEmergencia> = emptyList(),
    val usuario: Usuario? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * ViewModel encargado del CRUD del directorio de contactos de confianza.
 *
 * @property obtenerContactosUseCase Consulta contactos locales.
 * @property agregarContactoUseCase Inserta o actualiza un contacto.
 * @property eliminarContactoUseCase Elimina un contacto por ID.
 * @property obtenerUsuarioActualUseCase Recupera el usuario en sesión.
 */
class ContactsViewModel(
    private val obtenerContactosUseCase: ObtenerContactosUseCase,
    private val agregarContactoUseCase: AgregarContactoUseCase,
    private val eliminarContactoUseCase: EliminarContactoUseCase,
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        cargarContactos()
    }

    /**
     * Carga el usuario y se suscribe al flujo reactivo de contactos en Room.
     */
    private fun cargarContactos() {
        viewModelScope.launch {
            val user = obtenerUsuarioActualUseCase()
            _uiState.value = _uiState.value.copy(usuario = user)

            obtenerContactosUseCase().collect { lista ->
                _uiState.value = _uiState.value.copy(contactos = lista)
            }
        }
    }

    /**
     * Agrega un nuevo contacto de confianza validando el límite máximo de 5.
     */
    fun agregarContacto(nombre: String, telefono: String, relacion: String, prioridad: Int = 1) {
        val user = _uiState.value.usuario ?: return
        if (nombre.isBlank()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Ingresa el nombre del contacto")
            return
        }
        if (telefono.length < 10) {
            _uiState.value = _uiState.value.copy(errorMessage = "Ingresa un teléfono válido de 10 dígitos")
            return
        }
        if (_uiState.value.contactos.size >= 5) {
            _uiState.value = _uiState.value.copy(errorMessage = "Límite alcanzado: máximo 5 contactos de emergencia")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            try {
                val contacto = ContactoEmergencia(
                    id = 0,
                    usuarioId = user.id,
                    nombre = nombre.trim(),
                    telefono = telefono.trim(),
                    relacion = relacion.trim(),
                    prioridad = prioridad
                )
                agregarContactoUseCase(contacto)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Contacto guardado correctamente"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = "Error al guardar contacto: ${e.message}"
                )
            }
        }
    }

    /**
     * Elimina un contacto de la agenda de auxilio.
     */
    fun eliminarContacto(id: Int) {
        viewModelScope.launch {
            try {
                eliminarContactoUseCase(id)
                _uiState.value = _uiState.value.copy(successMessage = "Contacto eliminado")
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = "Error al eliminar: ${e.message}")
            }
        }
    }

    /**
     * Limpia mensajes informativos en la vista.
     */
    fun limpiarMensajes() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}

/**
 * Fábrica para instanciar [ContactsViewModel] mediante [AppModule].
 */
class ContactsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ContactsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ContactsViewModel(
                AppModule.provideObtenerContactosUseCase(context),
                AppModule.provideAgregarContactoUseCase(context),
                AppModule.provideEliminarContactoUseCase(context),
                AppModule.provideObtenerUsuarioActualUseCase(context)
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
```

> 📋 **INSTRUCCIÓN:** Copia la UI composable de `app/src/main/java/mx/edu/utng/cunasegura/presentation/contacts/ContactsScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.contacts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utng.cunasegura.domain.model.ContactoEmergencia

@Composable
fun ContactsScreen() {
    val context = LocalContext.current
    val viewModel: ContactsViewModel = viewModel(
        factory = ContactsViewModelFactory(context)
    )
    val uiState by viewModel.uiState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoTelefono by remember { mutableStateOf("") }
    var nuevaRelacion by remember { mutableStateOf("Familiar") }

    Scaffold(
        floatingActionButton = {
            if (uiState.contactos.size < 5) {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Agregar Contacto")
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Contactos de Emergencia",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${uiState.contactos.size} de 5 contactos registrados",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.contactos.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tienes contactos de auxilio guardados.\nPresiona + para registrar uno.",
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                    )
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.contactos) { contacto ->
                        ContactoCard(
                            contacto = contacto,
                            onCall = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${contacto.telefono}"))
                                context.startActivity(intent)
                            },
                            onDelete = { viewModel.eliminarContacto(contacto.id) }
                        )
                    }
                }
            }
        }

        // Modal para registrar nuevo contacto
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Nuevo Contacto de Auxilio") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = nuevoNombre,
                            onValueChange = { nuevoNombre = it },
                            label = { Text("Nombre") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = nuevoTelefono,
                            onValueChange = { nuevoTelefono = it.filter { c -> c.isDigit() }.take(10) },
                            label = { Text("Teléfono (10 dígitos)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = nuevaRelacion,
                            onValueChange = { nuevaRelacion = it },
                            label = { Text("Relación (Familiar, Vecino, etc.)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.agregarContacto(nuevoNombre, nuevoTelefono, nuevaRelacion)
                            nuevoNombre = ""
                            nuevoTelefono = ""
                            showDialog = false
                        }
                    ) {
                        Text("Guardar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}

@Composable
fun ContactoCard(
    contacto: ContactoEmergencia,
    onCall: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contacto.nombre,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${contacto.telefono} · ${contacto.relacion}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onCall) {
                Icon(Icons.Default.Call, contentDescription = "Llamar", tint = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
```

---

### Paso 9.5: Centro de Dispositivos Vinculados (`DevicesViewModel.kt`, `DevicesScreen.kt`)

Muestra el estado de enlace con el reloj Wear OS (Google Data Layer) y provee acceso al escáner de vinculación con Smart TVs comunitarias.

> 📋 **INSTRUCCIÓN:** Copia el ViewModel de `app/src/main/java/mx/edu/utng/cunasegura/presentation/devices/DevicesViewModel.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.devices

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.cunasegura.data.local.dao.ContactoDao
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.usecase.ObtenerUsuarioActualUseCase

/**
 * ViewModel para la pantalla de Dispositivos Vinculados (SmartWatch Wear OS y Smart TV comunitaria).
 *
 * Expone el estado del usuario activo y el conteo de contactos registrados.
 *
 * @property obtenerUsuarioActualUseCase Caso de uso para obtener el usuario autenticado.
 * @property contactoDao DAO para consultar contactos registrados.
 */
class DevicesViewModel(
    private val obtenerUsuarioActualUseCase: ObtenerUsuarioActualUseCase,
    private val contactoDao: ContactoDao
) : ViewModel() {

    private val _usuario = MutableStateFlow<Usuario?>(null)
    val usuario: StateFlow<Usuario?> = _usuario.asStateFlow()

    private val _contactCount = MutableStateFlow(0)
    val contactCount: StateFlow<Int> = _contactCount.asStateFlow()

    init {
        cargarDatos()
    }

    /**
     * Carga el usuario activo y consulta el número de contactos asociados.
     */
    private fun cargarDatos() {
        viewModelScope.launch {
            val user = obtenerUsuarioActualUseCase()
            _usuario.value = user
            if (user != null) {
                // Cuenta los contactos guardados en Room
                val contactos = contactoDao.obtenerPorUsuario(user.id)
                _contactCount.value = contactos.size
            }
        }
    }
}

/**
 * Fábrica para instanciar [DevicesViewModel] inyectando casos de uso desde [AppModule].
 */
class DevicesViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DevicesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DevicesViewModel(
                obtenerUsuarioActualUseCase = AppModule.provideObtenerUsuarioActualUseCase(context),
                contactoDao = AppModule.provideDatabase(context).contactoDao()
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
```

> 📋 **INSTRUCCIÓN:** Copia la UI composable de `app/src/main/java/mx/edu/utng/cunasegura/presentation/devices/DevicesScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DevicesScreen(
    onNavigateToWatchConfig: () -> Unit = {},
    onNavigateToTvConfig: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: DevicesViewModel = viewModel(
        factory = DevicesViewModelFactory(context)
    )
    val usuario by viewModel.usuario.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Text(
            text = "Dispositivos Vinculados",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Administra tu SmartWatch Wear OS y Smart TV comunitaria",
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ── Card SmartWatch Wear OS ────────────────────────────────────────
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Watch,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SmartWatch Wear OS",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Enlace Activo (Google Play Services)",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigateToWatchConfig,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Configurar Toques de Pánico")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Card Smart TV Comunitaria ──────────────────────────────────────
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Smart TV Vecinal",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "Difusión comunitaria en pantalla gigante",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onNavigateToTvConfig,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Vincular por Código QR / PIN")
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.Default.ChevronRight, contentDescription = null)
                }
            }
        }
    }
}
```

---

### Paso 9.6: Gestión de Redes Vecinales y Escaneo QR (`NetworksViewModel.kt`, `NetworksScreen.kt`)

Permite crear redes comunitarias, unirse por proximidad geográfica GPS o escanear códigos QR con la cámara de otros administradores de red.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/networks/NetworksViewModel.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.networks

import android.content.Context
import android.location.Location
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Network
import mx.edu.utng.cunasegura.domain.model.Usuario

/**
 * Estado inmutable de la pantalla de gestión de Redes Vecinales Comunitarias.
 *
 * @property usuarioActual Usuario en sesión.
 * @property redActual Red vecinal a la que pertenece el usuario.
 * @property miembrosRed Lista de vecinos afiliados a la red actual.
 * @property alertasRed Historial de alertas emitidas en la red.
 * @property esAdminDeRed Indica si el usuario es creador o moderador de la red actual.
 * @property redesCercanas Redes abiertas descubiertas por proximidad geográfica.
 * @property isLoading Bandera de progreso durante transacciones en Firebase.
 * @property mensaje Notificación de retroalimentación o error.
 */
data class NetworksUiState(
    val usuarioActual: Usuario? = null,
    val redActual: Network? = null,
    val miembrosRed: List<Usuario> = emptyList(),
    val alertasRed: List<mx.edu.utng.cunasegura.domain.model.Alerta> = emptyList(),
    val esAdminDeRed: Boolean = false,
    val redesCercanas: List<Pair<Network, Float>> = emptyList(), // Red vecinal y su distancia en metros
    val isLoading: Boolean = false,
    val mensaje: String? = null
)

/**
 * ViewModel que gestiona la afiliación, creación, descubrimiento por geolocalización y administración de Redes Vecinales.
 *
 * @param context Contexto de la aplicación para resolver dependencias desde [AppModule].
 */
class NetworksViewModel(context: Context) : ViewModel() {
    private val userRepo = AppModule.provideUsuarioRepository(context)
    private val netRepo = AppModule.provideNetworkRepository(context)

    private val _uiState = MutableStateFlow(NetworksUiState())
    val uiState: StateFlow<NetworksUiState> = _uiState.asStateFlow()

    init {
        cargarInformacion()
    }

    /**
     * Consulta la red actual del usuario, sus miembros, alertas e identifica si posee privilegios de moderación.
     */
    fun cargarInformacion() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, mensaje = null)
            try {
                val usuario = userRepo.obtenerUsuarioActual()
                if (usuario != null) {
                    // Obtener UID de Firebase Auth
                    val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
                    
                    if (uid.isNotEmpty()) {
                        // Obtener perfil detallado de usuarios
                        val userSnap = com.google.firebase.database.FirebaseDatabase.getInstance()
                            .getReference("usuarios").child(uid).get().await()
                        
                        val networkId = userSnap.child("networkId").getValue(String::class.java) ?: uid
                        val rolEnRed = userSnap.child("rolEnRed").getValue(String::class.java) ?: ""
                        val red = netRepo.obtenerNetworkPorId(networkId)
                        val miembros = if (red != null) netRepo.obtenerMiembrosDeRed(networkId) else emptyList()
                        val alertas = if (red != null) netRepo.obtenerAlertasDeRed(networkId) else emptyList()

                        val esAdmin = rolEnRed == "admin" || (red != null && red.id == uid)
                        
                        val currentDetalleUsuario = usuario.copy(
                            rol = userSnap.child("rol").getValue(String::class.java) ?: "usuario"
                        )

                        _uiState.value = _uiState.value.copy(
                            usuarioActual = currentDetalleUsuario,
                            redActual = red,
                            miembrosRed = miembros,
                            alertasRed = alertas,
                            esAdminDeRed = esAdmin,
                            isLoading = false
                        )
                    } else {
                        _uiState.value = _uiState.value.copy(
                            usuarioActual = usuario,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Sesión no iniciada")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error al cargar información: ${e.message}")
            }
        }
    }

    /**
     * Expulsa a un miembro de la red vecinal actual.
     *
     * @param uidMiembro UID del usuario a expulsar.
     */
    fun expulsarMiembro(uidMiembro: String) {
        viewModelScope.launch {
            try {
                val redId = _uiState.value.redActual?.id ?: return@launch
                _uiState.value = _uiState.value.copy(isLoading = true)
                val exito = netRepo.expulsarMiembro(uidMiembro, redId)
                if (exito) {
                    _uiState.value = _uiState.value.copy(mensaje = "Usuario expulsado de la red")
                    cargarInformacion()
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error al expulsar usuario")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error: ${e.message}")
            }
        }
    }

    /**
     * Renombra la red vecinal actual.
     *
     * @param nuevoNombre Nuevo nombre de la comunidad.
     */
    fun actualizarNombreRed(nuevoNombre: String) {
        viewModelScope.launch {
            val redId = _uiState.value.redActual?.id ?: return@launch
            if (nuevoNombre.isBlank()) {
                _uiState.value = _uiState.value.copy(mensaje = "El nombre de la red no puede estar vacío")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val exito = netRepo.actualizarNombreRed(redId, nuevoNombre.trim())
                if (exito) {
                    _uiState.value = _uiState.value.copy(mensaje = "Nombre de red actualizado a '$nuevoNombre'")
                    cargarInformacion()
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error al cambiar nombre")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error: ${e.message}")
            }
        }
    }

    /**
     * Crea y registra una nueva red vecinal comunitaria asignando al creador como moderador.
     */
    fun crearRedVecinal(nombre: String, tipo: String, lat: Double, lng: Double, radio: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                
                // Establecer el rol en la red vecinal como "admin" (sin sobreescribir el rol global del sistema)
                val userRef = com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("usuarios").child(uid)
                userRef.child("rolEnRed").setValue("admin").await()
                
                // Si anteriormente se sobreescribió como "administrador", restablecerlo a "usuario"
                val rolActual = userRef.child("rol").get().await().getValue(String::class.java)
                if (rolActual == "administrador" || rolActual == "admin") {
                    userRef.child("rol").setValue("usuario").await()
                }
                
                val nuevaRed = Network(
                    id = uid,
                    nombre = nombre,
                    tipo = tipo,
                    latitud = lat,
                    longitud = lng,
                    radio = radio,
                    miembros = mapOf(uid to true)
                )
                netRepo.crearNetwork(nuevaRed)
                netRepo.unirseARed(uid, uid)
                
                _uiState.value = _uiState.value.copy(mensaje = "¡Red vecinal '$nombre' creada con éxito!")
                cargarInformacion()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error al crear red: ${e.message}")
            }
        }
    }

    /**
     * Une al usuario a una red vecinal mediante el escaneo de un código QR.
     *
     * @param networkId Identificador extraído del código QR.
     */
    fun unirsePorQr(networkId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                
                val red = netRepo.obtenerNetworkPorId(networkId)
                if (red == null) {
                    _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Código QR inválido. Red no encontrada.")
                    return@launch
                }
                
                val exito = netRepo.unirseARed(uid, networkId)
                if (exito) {
                    _uiState.value = _uiState.value.copy(mensaje = "Te has unido a la red: ${red.nombre}")
                    cargarInformacion()
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error al unirse a la red.")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error: ${e.message}")
            }
        }
    }

    /**
     * Busca y filtra redes comunitarias abiertas por radio de cobertura GPS.
     *
     * @param lat Latitud GPS.
     * @param lng Longitud GPS.
     */
    fun buscarRedesAbiertasCercanas(lat: Double, lng: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val redes = netRepo.obtenerRedesAbiertas()
                val filtradas = mutableListOf<Pair<Network, Float>>()
                
                for (red in redes) {
                    val results = FloatArray(1)
                    Location.distanceBetween(lat, lng, red.latitud, red.longitud, results)
                    val distancia = results[0]
                    // Verificar si la distancia actual está dentro del radio de cobertura de la red
                    if (distancia <= red.radio) {
                        filtradas.add(Pair(red, distancia))
                    }
                }
                
                _uiState.value = _uiState.value.copy(
                    redesCercanas = filtradas.sortedBy { it.second },
                    isLoading = false,
                    mensaje = if (filtradas.isEmpty()) "No se encontraron redes abiertas dentro de tu cobertura." else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error al buscar: ${e.message}")
            }
        }
    }

    /**
     * Se une a una red pública abierta seleccionada por cercanía.
     */
    fun unirseARedAbierta(networkId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val exito = netRepo.unirseARed(uid, networkId)
                if (exito) {
                    _uiState.value = _uiState.value.copy(mensaje = "Te has unido con éxito")
                    cargarInformacion()
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error al unirse")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error: ${e.message}")
            }
        }
    }

    /**
     * Abandona la red vecinal actual restableciendo la red personal independiente del usuario.
     */
    fun salirDeRedActual() {
        viewModelScope.launch {
            try {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val redId = _uiState.value.redActual?.id ?: return@launch
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val exito = netRepo.salirDeRed(uid, redId)
                if (exito) {
                    _uiState.value = _uiState.value.copy(mensaje = "Saliste de la red vecinal")
                    cargarInformacion()
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error al salir de la red")
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, mensaje = "Error: ${e.message}")
            }
        }
    }

    /**
     * Limpia mensajes informativos o de alerta en la UI.
     */
    fun limpiarMensaje() {
        _uiState.value = _uiState.value.copy(mensaje = null)
    }
}

/**
 * Fábrica para instanciar [NetworksViewModel] pasando el contexto de la aplicación.
 */
class NetworksViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(NetworksViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return NetworksViewModel(context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/networks/NetworksScreen.kt`:
```kotlin
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
import androidx.compose.material.icons.filled.Edit
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
    var mostrarDialogoEditarNombre by remember { mutableStateOf(false) }
    var nuevoNombreRedInput by remember { mutableStateOf("") }

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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Shield, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(red.nombre, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = AzulCunaSegura)
                                if (uiState.esAdminDeRed) {
                                    IconButton(
                                        onClick = {
                                            nuevoNombreRedInput = red.nombre
                                             mostrarDialogoEditarNombre = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Edit,
                                            contentDescription = "Editar nombre de red",
                                            tint = AzulCunaSegura,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                            if (uiState.esAdminDeRed) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(AzulCunaSegura)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("Admin de Red", color = MaterialTheme.colorScheme.onPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
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
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(miembro.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(miembro.telefono, fontSize = 12.sp, color = Color.Gray)
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (miembro.rol == "administrador" || miembro.uid == red.id) AzulCunaSegura.copy(alpha = 0.2f) else Color.LightGray)
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (miembro.uid == red.id) "ADMIN RED" else miembro.rol.uppercase(),
                                            fontSize = 10.sp,
                                            color = if (miembro.rol == "administrador" || miembro.uid == red.id) AzulCunaSegura else Color.DarkGray,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    if (uiState.esAdminDeRed && miembro.uid != uiState.usuarioActual?.uid && miembro.uid.isNotEmpty()) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        TextButton(
                                            onClick = { viewModel.expulsarMiembro(miembro.uid) },
                                            colors = ButtonDefaults.textButtonColors(contentColor = RojoSOS)
                                        ) {
                                            Text("Expulsar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        // Alertas de la Red Vecinal
                        Text("Alertas de la Red Vecinal (${uiState.alertasRed.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AzulCunaSegura)
                        if (uiState.alertasRed.isEmpty()) {
                            Text("No hay alertas registradas en esta red.", fontSize = 12.sp, color = Color.Gray)
                        } else {
                            uiState.alertasRed.take(5).forEach { alerta ->
                                Card(
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (alerta.estado == "activa") RojoSOS.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(alerta.nombreUsuario, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Ubicación: ${alerta.latitud}, ${alerta.longitud}", fontSize = 11.sp, color = Color.Gray)
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (alerta.estado == "activa") RojoSOS else Color.Gray)
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = alerta.estado.uppercase(),
                                                fontSize = 10.sp,
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
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

    // Diálogo para editar el nombre de la red vecinal
    if (mostrarDialogoEditarNombre) {
        AlertDialog(
            onDismissRequest = { mostrarDialogoEditarNombre = false },
            title = { Text("Editar Nombre de la Red", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Ingresa el nuevo nombre para tu red vecinal:", fontSize = 14.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = nuevoNombreRedInput,
                        onValueChange = { nuevoNombreRedInput = it },
                        label = { Text("Nombre de la Red") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.actualizarNombreRed(nuevoNombreRedInput)
                        mostrarDialogoEditarNombre = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AzulCunaSegura)
                ) {
                    Text("Guardar")
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoEditarNombre = false }) {
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
```

---

### Paso 9.7: Perfil de Usuario y Seguridad (`UserProfileScreen.kt`)

Permite editar datos del ciudadano, consultar la red asignada, configurar dispositivos IoT y cerrar sesión de manera segura.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/profile/UserProfileScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Usuario

import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import android.widget.Toast
import kotlinx.coroutines.launch

import androidx.compose.material.icons.filled.Tv

private val AzulCunaSegura @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
private val RojoSOS @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onLogout: () -> Unit,
    onNavigateToNetworks: () -> Unit,
    onNavigateToWatchConfig: () -> Unit = {},
    onNavigateToTvConfig: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var usuario by remember { mutableStateOf<Usuario?>(null) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    var editNombre by remember { mutableStateOf("") }
    var editTelefono by remember { mutableStateOf("") }
    var editPassword by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }

    val cargarUsuario = {
        coroutineScope.launch {
            val uc = AppModule.provideObtenerUsuarioActualUseCase(context)
            usuario = uc()
        }
    }

    LaunchedEffect(Unit) {
        cargarUsuario()
    }

    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mi Perfil",
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = {
                        editNombre = usuario?.nombre ?: ""
                        editTelefono = usuario?.telefono ?: ""
                        editPassword = ""
                        showEditDialog = true
                    }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Perfil", tint = MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // Avatar
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(AzulCunaSegura),
                contentAlignment = Alignment.Center
            ) {
                val iniciales = usuario?.nombre
                    ?.trim()
                    ?.split(" ")
                    ?.filter { it.isNotBlank() }
                    ?.take(2)
                    ?.joinToString("") { it.first().uppercase() }
                    ?: "?"
                Text(
                    text = iniciales,
                    color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = usuario?.nombre ?: "Cargando...",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AzulCunaSegura
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(AzulCunaSegura.copy(alpha = 0.1f))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Vecino Verificado",
                    color = AzulCunaSegura,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Botón Editar Perfil principal
            OutlinedButton(
                onClick = {
                    editNombre = usuario?.nombre ?: ""
                    editTelefono = usuario?.telefono ?: ""
                    editPassword = ""
                    showEditDialog = true
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, tint = AzulCunaSegura)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Modificar Datos / Contraseña", fontWeight = FontWeight.Bold, color = AzulCunaSegura)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (!usuario?.correo.isNullOrBlank()) {
                        ProfileRow(icon = Icons.Default.Email, label = "Correo", value = usuario?.correo ?: "")
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                    }
                    if (!usuario?.telefono.isNullOrBlank()) {
                        ProfileRow(icon = Icons.Default.Call, label = "Teléfono", value = usuario?.telefono ?: "")
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                    }
                    ProfileRow(icon = Icons.Default.Shield, label = "Estado", value = usuario?.estado?.replaceFirstChar { it.uppercase() } ?: "Activo")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Configuración de Dispositivos completa
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "Dispositivos Vinculados y Toques",
                        fontWeight = FontWeight.Bold,
                        color = AzulCunaSegura,
                        fontSize = 14.sp
                    )
                    
                    // Item SmartWatch BLE y Toques
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Watch, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SmartWatch BLE", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Text("Configurar gestos y toques SOS", fontSize = 12.sp, color = Color.Gray)
                        }
                        TextButton(onClick = onNavigateToWatchConfig) {
                            Text("Configurar", fontWeight = FontWeight.Bold, color = AzulCunaSegura)
                        }
                    }

                    HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)

                    // Item Smart TV Vecinal
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Tv, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Smart TV Vecinal", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            Text(if (usuario?.tvVinculada == true) "TV Vinculada" else "Vincular TV de la Red", fontSize = 12.sp, color = Color.Gray)
                        }
                        TextButton(onClick = onNavigateToTvConfig) {
                            Text(if (usuario?.tvVinculada == true) "Ajustes" else "Vincular", fontWeight = FontWeight.Bold, color = AzulCunaSegura)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                onClick = onNavigateToNetworks,
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Red Vecinal", fontWeight = FontWeight.Bold, color = AzulCunaSegura, fontSize = 14.sp)
                        Text("Configurar, buscar o escanear QR", fontSize = 12.sp, color = Color.Gray)
                    }
                    Text("Configurar", fontSize = 12.sp, color = AzulCunaSegura, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout button
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RojoSOS)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { if (!isSaving) showEditDialog = false },
            title = { Text("Editar Perfil", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editNombre,
                        onValueChange = { editNombre = it },
                        label = { Text("Nombre Completo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editTelefono,
                        onValueChange = { editTelefono = it },
                        label = { Text("Teléfono de Contacto") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = editPassword,
                        onValueChange = { editPassword = it },
                        label = { Text("Nueva Contraseña (Opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editNombre.isBlank()) {
                            Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        isSaving = true
                        coroutineScope.launch {
                            val repo = AppModule.provideUsuarioRepository(context)
                            val res = repo.actualizarPerfilUsuario(
                                nombre = editNombre,
                                telefono = editTelefono,
                                nuevaPassword = editPassword.ifBlank { null }
                            )
                            isSaving = false
                            if (res.isSuccess) {
                                Toast.makeText(context, "¡Perfil actualizado con éxito!", Toast.LENGTH_SHORT).show()
                                showEditDialog = false
                                cargarUsuario()
                            } else {
                                Toast.makeText(context, "Error: ${res.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = AzulCunaSegura)
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text("Guardar Cambios")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }, enabled = !isSaving) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Cerrar Sesión", fontWeight = FontWeight.Bold) },
            text = { Text("¿Estás seguro que deseas cerrar sesión?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    val prefs = mx.edu.utng.cunasegura.data.local.prefs.PreferencesManager(context)
                    try {
                        com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    } catch (e: Exception) {
                        // If Firebase not initialized yet, ignore
                    }
                    onLogout()
                }) {
                    Text("Cerrar sesión", color = RojoSOS)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancelar", color = AzulCunaSegura)
                }
            }
        )
    }
}

@Composable
private fun ProfileRow(icon: ImageVector, label: String, value: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = label, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 11.sp, color = Color.Gray)
            Text(value, fontSize = 14.sp, color = Color.DarkGray, fontWeight = FontWeight.Medium)
        }
    }
}
```

---

### Paso 9.8: Configuración del Smartwatch Wear OS (`WatchConfigScreen.kt`)

Configura la cadencia de toques (1, 2, 3 o 4 pulsaciones consecutivas) y botones de pánico del reloj inteligente Wear OS con sincronización RPC instantánea.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/watchconfig/WatchConfigScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.watchconfig

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.data.local.entity.ConfiguracionToqueEntity
import mx.edu.utng.cunasegura.di.AppModule

private val ACCIONES_MAP = mapOf(
    "MENSAJE_SMS" to "Enviar mensaje de alerta",
    "UBICACION_TIEMPO_REAL" to "Compartir ubicación en tiempo real",
    "ALARMA_TV" to "Activar alarma en TV de vecinos",
    "LLAMAR_911" to "Llamar al 911"
)
private val ACCIONES_KEYS = ACCIONES_MAP.keys.toList()

data class WatchUiState(
    val toque1: String = ACCIONES_KEYS[0],
    val toque2: String = ACCIONES_KEYS[1],
    val toque3: String = ACCIONES_KEYS[2],
    val toque4: String = ACCIONES_KEYS[3],
    val watchVinculado: Boolean = false,
    val guardado: Boolean = false,
    val isLoading: Boolean = false
)

class WatchConfigViewModel(private val context: Context) : ViewModel() {

    private val db = AppModule.provideDatabase(context)
    private val toqueDao = db.configuracionToqueDao()
    private val obtenerUsuarioActualUseCase = AppModule.provideObtenerUsuarioActualUseCase(context)
    private val prefs = mx.edu.utng.cunasegura.data.local.prefs.PreferencesManager(context)

    private val _uiState = MutableStateFlow(WatchUiState(watchVinculado = prefs.isWatchLinked()))
    val uiState: StateFlow<WatchUiState> = _uiState.asStateFlow()

    private var usuarioId: Int = 1 // Fallback ID

    init {
        cargarConfiguracion()
    }

    private fun cargarConfiguracion() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val usuario = obtenerUsuarioActualUseCase()
            if (usuario != null) {
                usuarioId = usuario.id
            }

            val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                try {
                    val snap = com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("configuracion_toques")
                        .child(firebaseUser.uid)
                        .get()
                        .await()
                    if (snap.exists()) {
                        val t1 = snap.child("1").getValue(String::class.java) ?: ACCIONES_KEYS[0]
                        val t2 = snap.child("2").getValue(String::class.java) ?: ACCIONES_KEYS[1]
                        val t3 = snap.child("3").getValue(String::class.java) ?: ACCIONES_KEYS[2]
                        val t4 = snap.child("4").getValue(String::class.java) ?: ACCIONES_KEYS[3]

                        _uiState.value = _uiState.value.copy(
                            toque1 = t1,
                            toque2 = t2,
                            toque3 = t3,
                            toque4 = t4,
                            isLoading = false
                        )
                        return@launch
                    }
                } catch (e: Exception) {
                    // Fallback to local Room DB if offline
                }
            }

            val configs = toqueDao.obtenerPorUsuario(usuarioId)
            if (configs.isNotEmpty()) {
                val map = configs.associate { it.cantidadToques to it.tipoAccion }
                _uiState.value = _uiState.value.copy(
                    toque1 = map[1] ?: ACCIONES_KEYS[0],
                    toque2 = map[2] ?: ACCIONES_KEYS[1],
                    toque3 = map[3] ?: ACCIONES_KEYS[2],
                    toque4 = map[4] ?: ACCIONES_KEYS[3],
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onAccionChange(numeroToque: Int, accion: String) {
        _uiState.value = when (numeroToque) {
            1 -> _uiState.value.copy(toque1 = accion, guardado = false)
            2 -> _uiState.value.copy(toque2 = accion, guardado = false)
            3 -> _uiState.value.copy(toque3 = accion, guardado = false)
            4 -> _uiState.value.copy(toque4 = accion, guardado = false)
            else -> _uiState.value
        }
    }

    fun guardarConfiguracion() {
        viewModelScope.launch {
            val state = _uiState.value
            
            // 1. Guardar en nube Firebase Realtime Database
            val firebaseUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
            if (firebaseUser != null) {
                try {
                    val cloudMap = mapOf(
                        "1" to state.toque1,
                        "2" to state.toque2,
                        "3" to state.toque3,
                        "4" to state.toque4
                    )
                    com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("configuracion_toques")
                        .child(firebaseUser.uid)
                        .setValue(cloudMap)
                        .await()
                } catch (e: Exception) {
                    // Ignore offline
                }
            }

            // 2. Guardar en Room local
            val t1 = toqueDao.obtenerPorUsuarioYToque(usuarioId, 1)
            toqueDao.insertarOActualizar(ConfiguracionToqueEntity(id = t1?.id ?: 0, usuarioId = usuarioId, cantidadToques = 1, tipoAccion = state.toque1))

            val t2 = toqueDao.obtenerPorUsuarioYToque(usuarioId, 2)
            toqueDao.insertarOActualizar(ConfiguracionToqueEntity(id = t2?.id ?: 0, usuarioId = usuarioId, cantidadToques = 2, tipoAccion = state.toque2))

            val t3 = toqueDao.obtenerPorUsuarioYToque(usuarioId, 3)
            toqueDao.insertarOActualizar(ConfiguracionToqueEntity(id = t3?.id ?: 0, usuarioId = usuarioId, cantidadToques = 3, tipoAccion = state.toque3))

            val t4 = toqueDao.obtenerPorUsuarioYToque(usuarioId, 4)
            toqueDao.insertarOActualizar(ConfiguracionToqueEntity(id = t4?.id ?: 0, usuarioId = usuarioId, cantidadToques = 4, tipoAccion = state.toque4))

            _uiState.value = _uiState.value.copy(guardado = true)
            
            val payload = "${state.toque1}|${state.toque2}|${state.toque3}|${state.toque4}"
            viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val messageClient = com.google.android.gms.wearable.Wearable.getMessageClient(context)
                    val nodeClient = com.google.android.gms.wearable.Wearable.getNodeClient(context)
                    val nodes = com.google.android.gms.tasks.Tasks.await(nodeClient.connectedNodes)
                    val data = payload.toByteArray()
                    for (node in nodes) {
                        messageClient.sendMessage(node.id, "/cunasegura/config/update", data)
                    }
                    kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                        if (nodes.isNotEmpty()) {
                            android.widget.Toast.makeText(context, "Sincronizando con reloj...", android.widget.Toast.LENGTH_SHORT).show()
                        } else {
                            android.widget.Toast.makeText(context, "Guardado, pero no hay reloj conectado", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("WatchConfigViewModel", "Error enviando config", e)
                }
            }
        }
    }

    fun onSimularVinculacion() {
        val newState = !_uiState.value.watchVinculado
        _uiState.value = _uiState.value.copy(watchVinculado = newState)
        prefs.setWatchLinked(newState)
    }
}

class WatchConfigViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return WatchConfigViewModel(context) as T
    }
}

private val VerdeVinculado @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.secondary
private val NaranjaDesvinculado @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.tertiary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchConfigScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: WatchConfigViewModel = viewModel(factory = WatchConfigViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración SmartWatch", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estado de Vinculación BLE
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.watchVinculado) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.tertiaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(52.dp).clip(CircleShape).background(
                                if (uiState.watchVinculado) VerdeVinculado else NaranjaDesvinculado
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Watch, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                if (uiState.watchVinculado) "SmartWatch Vinculado" else "Sin SmartWatch vinculado",
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.watchVinculado) VerdeVinculado else NaranjaDesvinculado
                            )
                            Text(
                                if (uiState.watchVinculado) "Dispositivo BLE conectado ✓"
                                else "Activa Bluetooth y acerca tu watch",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Información BLE
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Requisitos de Vinculación", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Watch Wear OS o compatible con BLE\n• Celular con Bluetooth activo\n• Radio máximo ~10 metros\n• La app necesita permiso BLUETOOTH_CONNECT", fontSize = 12.sp, color = Color.DarkGray)
                        }
                    }
                }
            }

            // Configuración de Toques
            item {
                Text("Asignación de Acciones por Toque", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, fontSize = 15.sp)
                Text("Define qué hace cada toque del botón de pánico en tu watch:", fontSize = 12.sp, color = Color.Gray)
            }

            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        listOf(
                            Pair(1, uiState.toque1),
                            Pair(2, uiState.toque2),
                            Pair(3, uiState.toque3),
                            Pair(4, uiState.toque4)
                        ).forEach { (num, accion) ->
                            ToqueRow(
                                numero = num,
                                accionSeleccionada = accion,
                                onAccionChange = { viewModel.onAccionChange(num, it) }
                            )
                            if (num < 4) HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
                        }
                    }
                }
            }

            // Botón Guardar
            item {
                Button(
                    onClick = { viewModel.guardarConfiguracion() },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Guardar Configuración", fontWeight = FontWeight.Bold)
                }

                if (uiState.guardado) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "✅ Configuración guardada correctamente en el dispositivo",
                        color = VerdeVinculado,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToqueRow(
    numero: Int,
    accionSeleccionada: String,
    onAccionChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val emojis = mapOf(1 to "☝️", 2 to "✌️", 3 to "🤟", 4 to "🖐️")
    val iconos: Map<String, ImageVector> = mapOf(
        "MENSAJE_SMS" to Icons.Default.Message,
        "UBICACION_TIEMPO_REAL" to Icons.Default.LocationOn,
        "ALARMA_TV" to Icons.Default.Tv,
        "LLAMAR_911" to Icons.Default.Call
    )

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(emojis[numero] ?: "$numero", fontSize = 16.sp)
        }
        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${numero} toque${if (numero > 1) "s" else ""}",
                fontSize = 12.sp, color = Color.Gray
            )
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it }
            ) {
                OutlinedTextField(
                    value = ACCIONES_MAP[accionSeleccionada] ?: accionSeleccionada,
                    onValueChange = {},
                    readOnly = true,
                    leadingIcon = {
                        Icon(iconos[accionSeleccionada] ?: Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    ACCIONES_KEYS.forEach { opcion ->
                        DropdownMenuItem(
                            text = { Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(iconos[opcion] ?: Icons.Default.Star, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(ACCIONES_MAP[opcion] ?: opcion, fontSize = 13.sp)
                            }},
                            onClick = {
                                onAccionChange(opcion)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}
```

---

### Paso 9.9: Vinculación con Smart TV Comunitaria (`TvConfigScreen.kt`)

Escanea el código QR proyectado en la pantalla de la Smart TV y envía el payload de enlace mediante MQTT TLS y Firebase Realtime Database.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/tvconfig/TvConfigScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.tvconfig

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Usuario
import com.google.firebase.database.FirebaseDatabase
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import androidx.activity.compose.rememberLauncherForActivityResult

data class TvUiState(
    val usuario: Usuario? = null,
    val tvVinculada: Boolean = false,
    val isLoading: Boolean = false,
    val infoMessage: String? = null
)

class TvConfigViewModel(private val context: Context) : ViewModel() {

    private val obtenerUsuarioActualUseCase = AppModule.provideObtenerUsuarioActualUseCase(context)
    private val guardarUsuarioUseCase = AppModule.provideGuardarUsuarioUseCase(context)

    private val _uiState = MutableStateFlow(TvUiState())
    val uiState: StateFlow<TvUiState> = _uiState.asStateFlow()

    init {
        cargarUsuario()
    }

    private fun cargarUsuario() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val user = obtenerUsuarioActualUseCase()
            if (user != null) {
                _uiState.value = _uiState.value.copy(
                    usuario = user,
                    tvVinculada = user.tvVinculada,
                    isLoading = false
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun onToggleTvVinculada(vinculada: Boolean) {
        viewModelScope.launch {
            val user = _uiState.value.usuario ?: return@launch
            val updatedUser = user.copy(tvVinculada = vinculada)
            guardarUsuarioUseCase(updatedUser)
            _uiState.value = _uiState.value.copy(
                usuario = updatedUser,
                tvVinculada = vinculada,
                infoMessage = if (vinculada) "¡Smart TV vinculada correctamente!" else "Smart TV desvinculada"
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(infoMessage = null)
    }

    fun onQrScanned(contents: String) {
        val tvId = if (contents.contains("tvId=")) {
            contents.substringAfter("tvId=")
        } else {
            contents
        }
        
        viewModelScope.launch {
            val user = _uiState.value.usuario ?: return@launch
            val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
            
            // Usar la red vecinal real a la que pertenece el usuario (no sobreescribir su red con su UID)
            val effectiveNetworkId = user.networkId.ifBlank { uid }

            val dbRef = FirebaseDatabase.getInstance().getReference("tvs").child(tvId)
            val updates = mapOf(
                "networkId" to effectiveNetworkId,
                "linkedBy" to uid
            )
            dbRef.updateChildren(updates).addOnSuccessListener {
                if (user.networkId.isBlank()) {
                    FirebaseDatabase.getInstance().getReference("usuarios").child(uid).child("networkId").setValue(effectiveNetworkId)
                }
                mx.edu.utng.cunasegura.mqtt.MqttPublisher.publishTvVinculacion(tvId, effectiveNetworkId)
                onToggleTvVinculada(true)
            }.addOnFailureListener {
                _uiState.value = _uiState.value.copy(infoMessage = "Error al enlazar Smart TV")
            }
        }
    }
}

class TvConfigViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return TvConfigViewModel(context) as T
    }
}

private val AzulCunaSegura @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
private val VerdeVinculado @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.secondary
private val GrisDesvinculado @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.outline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TvConfigScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val viewModel: TvConfigViewModel = viewModel(factory = TvConfigViewModelFactory(context))
    val uiState by viewModel.uiState.collectAsState()

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            if (result.contents != null) {
                viewModel.onQrScanned(result.contents)
            }
        }
    )

    LaunchedEffect(uiState.infoMessage) {
        if (uiState.infoMessage != null) {
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración Smart TV", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card de Estado de Vinculación
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (uiState.tvVinculada) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(if (uiState.tvVinculada) VerdeVinculado else GrisDesvinculado),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Tv, contentDescription = null, tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            if (uiState.tvVinculada) "Smart TV Vinculada" else "Smart TV no vinculada",
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.tvVinculada) VerdeVinculado else Color.DarkGray
                        )
                        Text(
                            if (uiState.tvVinculada) "Central de Monitoreo activa ✓"
                            else "Escanea el código QR desde tu Smart TV",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                    Switch(
                        checked = uiState.tvVinculada,
                        onCheckedChange = { viewModel.onToggleTvVinculada(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = VerdeVinculado)
                    )
                }
            }

            // Lector QR en Teléfono
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().shadow(3.dp, RoundedCornerShape(20.dp))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "VINCULAR NUEVA PANTALLA",
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp,
                        letterSpacing = 1.5.sp,
                        color = AzulCunaSegura
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Abre la app de Cuna Segura en tu Smart TV y escanea el código QR que aparece en pantalla.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Button(
                        onClick = {
                            val options = ScanOptions()
                            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            options.setPrompt("Apunta al código QR en tu TV")
                            options.setBeepEnabled(true)
                            options.setBarcodeImageEnabled(false)
                            scanLauncher.launch(options)
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AzulCunaSegura),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Escanear QR de la TV", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "ID de Vinculación: CS-${uiState.usuario?.id ?: 0}-${uiState.usuario?.telefono?.takeLast(4) ?: "0000"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = AzulCunaSegura
                    )
                }
            }

            // Card de Info Leanback
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("Monitoreo en Smart TV", fontWeight = FontWeight.Bold, color = AzulCunaSegura, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "• Recibe alertas visuales en tu pantalla\n" +
                            "• Observa a tus vecinos en el mapa al activarse un SOS\n" +
                            "• Visualiza información de contacto del vecino en riesgo",
                            fontSize = 12.sp, color = Color.DarkGray
                        )
                    }
                }
            }

            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = AzulCunaSegura),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Listo", fontWeight = FontWeight.Bold)
            }
        }
    }
}
```

---

### Paso 9.10: Contenedor Principal del Vecino (`MainUserScreen.kt`)

Organiza la navegación con barra inferior (*Bottom Navigation Bar*) entre Inicio, Red Vecinal, Contactos, Mapa Comunitario y Perfil.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/main/MainUserScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.material.icons.filled.Shield
import mx.edu.utng.cunasegura.presentation.contacts.ContactsScreen
import mx.edu.utng.cunasegura.presentation.devices.DevicesScreen
import mx.edu.utng.cunasegura.presentation.home.HomeScreen
import mx.edu.utng.cunasegura.presentation.map.CommunityMapScreen
import mx.edu.utng.cunasegura.presentation.navigation.Screen
import mx.edu.utng.cunasegura.presentation.networks.NetworksScreen
import mx.edu.utng.cunasegura.presentation.profile.UserProfileScreen

@Composable
fun MainUserScreen(
    rootNavController: NavHostController,
    mainNavController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Screen.Home.route

    val AzulCunaSegura = androidx.compose.material3.MaterialTheme.colorScheme.primary

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentRoute == Screen.Home.route,
                    onClick = {
                        if (currentRoute != Screen.Home.route) {
                            mainNavController.navigate(Screen.Home.route) {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    label = { Text("Inicio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        selectedTextColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Networks.route,
                    onClick = {
                        if (currentRoute != Screen.Networks.route) {
                            mainNavController.navigate(Screen.Networks.route) {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Shield, contentDescription = "Red Vecinal") },
                    label = { Text("Red Vecinal") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        selectedTextColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.Contacts.route,
                    onClick = {
                        if (currentRoute != Screen.Contacts.route) {
                            mainNavController.navigate(Screen.Contacts.route) {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Call, contentDescription = "Contactos") },
                    label = { Text("Contactos") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        selectedTextColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == Screen.CommunityMap.route,
                    onClick = {
                        if (currentRoute != Screen.CommunityMap.route) {
                            mainNavController.navigate(Screen.CommunityMap.route) {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.LocationOn, contentDescription = "Mapa") },
                    label = { Text("Mapa") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        selectedTextColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "user_profile",
                    onClick = {
                        if (currentRoute != "user_profile") {
                            mainNavController.navigate("user_profile") {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        selectedTextColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = mainNavController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    onNavigateToEmergency = { alertaId ->
                        rootNavController.navigate(Screen.EmergencyActive.createRoute(alertaId))
                    }
                )
            }
            composable(Screen.Networks.route) {
                NetworksScreen(
                    onBack = {
                        mainNavController.navigate(Screen.Home.route) {
                            popUpTo(mainNavController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                )
            }
            composable(Screen.Contacts.route) {
                ContactsScreen()
            }
            composable(Screen.Devices.route) {
                DevicesScreen(
                    onNavigateToWatchConfig = {
                        rootNavController.navigate(Screen.WatchConfig.route)
                    },
                    onNavigateToTvConfig = {
                        rootNavController.navigate(Screen.TvConfig.route)
                    }
                )
            }
            composable(Screen.CommunityMap.route) {
                CommunityMapScreen()
            }
            composable("user_profile") {
                UserProfileScreen(
                    onLogout = {
                        rootNavController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    },
                    onNavigateToNetworks = {
                        mainNavController.navigate(Screen.Networks.route) {
                            launchSingleTop = true
                        }
                    },
                    onNavigateToWatchConfig = {
                        rootNavController.navigate(Screen.WatchConfig.route)
                    },
                    onNavigateToTvConfig = {
                        rootNavController.navigate(Screen.TvConfig.route)
                    }
                )
            }
        }
    }
}
```

---

## FASE 10: Consola y Panel de Administración Global

### Paso 10.1: ViewModel Administrativo Central (`AdminViewModel.kt`)

Maneja el padrón global de usuarios, monitoreo de alertas de todas las redes, cambios de estado de cuentas (`bloqueado`, `activo`) y guardado de parámetros globales del sistema (`configuracion_global`).

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/admin/AdminViewModel.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.admin

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import mx.edu.utng.cunasegura.di.AppModule
import mx.edu.utng.cunasegura.domain.model.Alerta
import mx.edu.utng.cunasegura.domain.model.Network
import mx.edu.utng.cunasegura.domain.model.Usuario
import mx.edu.utng.cunasegura.domain.repository.IAlertaRepository
import mx.edu.utng.cunasegura.domain.repository.INetworkRepository
import mx.edu.utng.cunasegura.domain.repository.IUsuarioRepository

/**
 * ViewModel maestro para la consola y paneles de control del Administrador Global.
 *
 * Administra el censo global de usuarios, monitoreo de alertas de todas las redes, cambios de estado de cuentas (`bloqueado`, `activo`)
 * y guardado de parámetros globales del sistema (`configuracion_global`).
 *
 * @property usuarioRepository Repositorio de usuarios.
 * @property alertaRepository Repositorio de alertas.
 * @property networkRepository Repositorio de redes comunitarias.
 */
class AdminViewModel(
    private val usuarioRepository: IUsuarioRepository,
    private val alertaRepository: IAlertaRepository,
    private val networkRepository: INetworkRepository
) : ViewModel() {

    private val _usuarios = MutableStateFlow<List<Usuario>>(emptyList())
    val usuarios: StateFlow<List<Usuario>> = _usuarios.asStateFlow()

    private val _totalUsuarios = MutableStateFlow(0)
    val totalUsuarios: StateFlow<Int> = _totalUsuarios.asStateFlow()

    private val _adminActual = MutableStateFlow<Usuario?>(null)
    val adminActual: StateFlow<Usuario?> = _adminActual.asStateFlow()

    private val _network = MutableStateFlow<Network?>(null)
    val network: StateFlow<Network?> = _network.asStateFlow()

    private val _alertas = MutableStateFlow<List<Alerta>>(emptyList())
    val alertas: StateFlow<List<Alerta>> = _alertas.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _tiempoVidaAlerta = MutableStateFlow<Double>(720.0)
    val tiempoVidaAlerta: StateFlow<Double> = _tiempoVidaAlerta.asStateFlow()

    init {
        cargarDatos()
    }

    /**
     * Consulta el catálogo total de usuarios, datos del administrador, red asignada, alertas y configuración global.
     */
    private fun cargarDatos() {
        viewModelScope.launch {
            try {
                val todos = usuarioRepository.obtenerTodosLosUsuarios()
                _usuarios.value = todos
                _totalUsuarios.value = todos.size
                
                val admin = usuarioRepository.obtenerUsuarioActual()
                _adminActual.value = admin
                
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid
                if (uid != null) {
                    val snapshot = com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("usuarios").child(uid).get().await()
                    val netId = snapshot.child("networkId").getValue(String::class.java) ?: uid
                    val net = networkRepository.obtenerNetworkPorId(netId)
                    _network.value = net
                }

                // Cargar todas las alertas para estadísticas
                val todasAlertas = alertaRepository.obtenerTodasLasAlertas()
                _alertas.value = todasAlertas

                // Cargar configuracion global
                val config = networkRepository.obtenerConfiguracionGlobal()
                val tiempoVidaStr = config["tiempoVidaAlerta"]?.toString() ?: "720.0"
                _tiempoVidaAlerta.value = tiempoVidaStr.toDoubleOrNull() ?: 720.0
            } catch (e: Exception) {
                _statusMessage.value = "Error al cargar datos: ${e.message}"
            }
        }
    }

    /**
     * Fuerza una recarga completa de todos los datos administrativos.
     */
    fun recargar() = cargarDatos()

    /**
     * Persiste los parámetros globales de red y directivas de seguridad en Firebase.
     */
    fun guardarRedConfig(
        tipo: String,
        radio: Double,
        tiempoAntiFalsa: Double,
        checkVida: Double,
        esperarDiasNuevos: Int,
        tiempoVidaAlerta: Double
    ) {
        viewModelScope.launch {
            try {
                val uid = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: return@launch
                val currentNet = _network.value
                val updatedNet = Network(
                    id = currentNet?.id ?: uid,
                    nombre = currentNet?.nombre ?: "Red Vecinal",
                    tipo = tipo,
                    latitud = currentNet?.latitud ?: 0.0,
                    longitud = currentNet?.longitud ?: 0.0,
                    radio = radio,
                    miembros = currentNet?.miembros ?: mapOf(uid to true),
                    tvId = currentNet?.tvId ?: "",
                    tiempoAntiFalsa = tiempoAntiFalsa,
                    checkVida = checkVida,
                    esperarDiasNuevos = esperarDiasNuevos
                )
                networkRepository.crearNetwork(updatedNet)
                networkRepository.guardarConfiguracionGlobal(tipo, radio, tiempoAntiFalsa, checkVida, esperarDiasNuevos, tiempoVidaAlerta)
                _network.value = updatedNet
                _tiempoVidaAlerta.value = tiempoVidaAlerta
                _statusMessage.value = "¡Configuración de la red guardada con éxito!"
            } catch (e: Exception) {
                _statusMessage.value = "Error al guardar configuración: ${e.message}"
            }
        }
    }

    /**
     * Modifica el estado operativo de una cuenta de usuario (`activo`, `bloqueado`).
     *
     * @param uid Identificador del usuario.
     * @param nuevoEstado Estado a establecer.
     */
    fun cambiarEstadoUsuario(uid: String, nuevoEstado: String) {
        viewModelScope.launch {
            try {
                com.google.firebase.database.FirebaseDatabase.getInstance()
                    .getReference("usuarios").child(uid).child("estado").setValue(nuevoEstado).await()
                _statusMessage.value = "Estado del usuario actualizado a '$nuevoEstado'"
                cargarDatos()
            } catch (e: Exception) {
                _statusMessage.value = "Error al actualizar estado: ${e.message}"
            }
        }
    }

    /**
     * Limpia el mensaje informativo de estado.
     */
    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}

/**
 * Fábrica para instanciar [AdminViewModel] resolviendo repositorios mediante [AppModule].
 */
class AdminViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AdminViewModel(
                AppModule.provideUsuarioRepository(context),
                AppModule.provideAlertaRepository(context),
                AppModule.provideNetworkRepository(context)
            ) as T
        }
        throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
    }
}
```

---

### Paso 10.2: Contenedor Administrativo Principal (`MainAdminScreen.kt`)

Provee navegación por tabs entre Dashboard, Padrón de Miembros, Parámetros Globales, Estadísticas y Perfil de Administrador.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/main/MainAdminScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.main

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import mx.edu.utng.cunasegura.presentation.admin.AdminConfigScreen
import mx.edu.utng.cunasegura.presentation.admin.AdminDashboardScreen
import mx.edu.utng.cunasegura.presentation.admin.AdminMembersScreen
import mx.edu.utng.cunasegura.presentation.admin.AdminProfileScreen
import mx.edu.utng.cunasegura.presentation.admin.AdminStatsScreen
import mx.edu.utng.cunasegura.presentation.navigation.Screen

@Composable
fun MainAdminScreen(
    rootNavController: NavHostController,
    mainNavController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by mainNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "admin_dashboard"

    val AzulCunaSegura = androidx.compose.material3.MaterialTheme.colorScheme.primary

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentRoute == "admin_dashboard",
                    onClick = {
                        if (currentRoute != "admin_dashboard") {
                            mainNavController.navigate("admin_dashboard") {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "admin_members",
                    onClick = {
                        if (currentRoute != "admin_members") {
                            mainNavController.navigate("admin_members") {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.People, contentDescription = "Miembros") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "admin_config",
                    onClick = {
                        if (currentRoute != "admin_config") {
                            mainNavController.navigate("admin_config") {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Configuración") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "admin_stats",
                    onClick = {
                        if (currentRoute != "admin_stats") {
                            mainNavController.navigate("admin_stats") {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.BarChart, contentDescription = "Estadísticas") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
                NavigationBarItem(
                    selected = currentRoute == "admin_profile",
                    onClick = {
                        if (currentRoute != "admin_profile") {
                            mainNavController.navigate("admin_profile") {
                                popUpTo(mainNavController.graph.startDestinationId)
                                launchSingleTop = true
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AzulCunaSegura,
                        indicatorColor = AzulCunaSegura.copy(alpha = 0.1f)
                    )
                )
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = mainNavController,
            startDestination = "admin_dashboard",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("admin_dashboard") {
                AdminDashboardScreen()
            }
            composable("admin_members") {
                AdminMembersScreen()
            }
            composable("admin_config") {
                AdminConfigScreen()
            }
            composable("admin_stats") {
                AdminStatsScreen()
            }
            composable("admin_profile") {
                AdminProfileScreen(
                    onLogout = {
                        rootNavController.navigate(Screen.Login.route) {
                            popUpTo(Screen.AdminPanel.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
```

---

### Paso 10.3: Panel Maestro de Control (`AdminPanelScreen.kt`)

Muestra métricas globales, accesos rápidos y panel de administración integral.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/admin/AdminPanelScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utng.cunasegura.domain.model.Usuario

private val AzulAdmin @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.onPrimaryContainer
private val AzulSecundario @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
private val DoradoAdmin @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.tertiary
private val VerdeAdmin @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.secondary
private val RojoAdmin @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(context))

    val usuarios by viewModel.usuarios.collectAsState()
    val totalUsuarios by viewModel.totalUsuarios.collectAsState()
    val adminActual by viewModel.adminActual.collectAsState()

    val vecinos = usuarios.filter { it.rol != "admin" }
    val vectinosActivos = vecinos.filter { it.estado == "activo" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = DoradoAdmin, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Panel de Administración", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Cuna Segura — Admin Global", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontSize = 11.sp)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.recargar() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar", tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión", tint = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulAdmin)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Bienvenida Admin
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(colors = listOf(AzulAdmin, AzulSecundario))
                            )
                            .padding(20.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(DoradoAdmin),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("BA", color = AzulAdmin, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Bienvenido,", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), fontSize = 13.sp)
                                Text(adminActual?.nombre ?: "Administrador", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DoradoAdmin))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Administrador Global", color = DoradoAdmin, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }

            // Estadísticas rápidas
            item {
                Text("Estadísticas de la Plataforma", fontWeight = FontWeight.Bold, color = AzulAdmin, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        titulo = "Vecinos",
                        valor = vecinos.size.toString(),
                        icon = Icons.Default.People,
                        color = AzulSecundario
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        titulo = "Activos",
                        valor = vectinosActivos.size.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = VerdeAdmin
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        titulo = "Total",
                        valor = totalUsuarios.toString(),
                        icon = Icons.Default.Group,
                        color = DoradoAdmin
                    )
                }
            }

            // Config global
            item {
                Text("Configuración Global de la Red", fontWeight = FontWeight.Bold, color = AzulAdmin, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminConfigRow(icon = Icons.Default.LocationOn, label = "Radio de detección automática", valor = "200 metros")
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        AdminConfigRow(icon = Icons.Default.Security, label = "Tipo de red", valor = "GPS Abierta + QR")
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        AdminConfigRow(icon = Icons.Default.Timer, label = "Tiempo anti-falsa alarma", valor = "5 segundos")
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        AdminConfigRow(icon = Icons.Default.Notifications, label = "Check de vida cada", valor = "2 minutos")
                    }
                }
            }

            // Lista de Miembros
            item {
                Text("Gestión de Miembros (${vecinos.size})", fontWeight = FontWeight.Bold, color = AzulAdmin, fontSize = 15.sp)
            }

            if (vecinos.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("👥", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Sin vecinos registrados", color = Color.Gray, textAlign = TextAlign.Center)
                                Text("Los vecinos aparecerán aquí cuando inicien sesión.", fontSize = 12.sp, color = Color.Gray, textAlign = TextAlign.Center)
                            }
                        }
                    }
                }
            } else {
                items(vecinos) { usuario ->
                    MiembroCard(usuario = usuario)
                }
            }

            // Módulos futuros
            item {
                Text("Módulos en Desarrollo", fontWeight = FontWeight.Bold, color = AzulAdmin, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModuloRow("🔥 Firebase Realtime DB", "Alertas en tiempo real entre vecinos", pendiente = true)
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        ModuloRow("📲 FCM Push", "Notificaciones push a celulares y TV", pendiente = true)
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        ModuloRow("📺 Smart TV App", "App de monitoreo en Android TV", pendiente = true)
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        ModuloRow("⌚ BLE Smartwatch", "Comunicación Bluetooth con watch", pendiente = true)
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        ModuloRow("🗺️ Mapa Comunitario", "Marcadores de vecinos en mapa", completado = true)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier, titulo: String, valor: String, icon: ImageVector, color: Color) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        modifier = modifier.shadow(2.dp, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = titulo, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(valor, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, color = color)
            Text(titulo, fontSize = 11.sp, color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun AdminConfigRow(icon: ImageVector, label: String, valor: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(icon, contentDescription = label, tint = AzulSecundario, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 13.sp, color = Color.DarkGray)
        }
        Text(valor, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = AzulSecundario)
    }
}

@Composable
private fun MiembroCard(usuario: Usuario) {
    val iniciales = usuario.nombre.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(AzulSecundario),
                contentAlignment = Alignment.Center
            ) {
                Text(iniciales, color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("📞 ${usuario.telefono}", fontSize = 12.sp, color = Color.Gray)
                if (usuario.correo.isNotBlank()) Text("✉️ ${usuario.correo}", fontSize = 11.sp, color = Color.Gray)
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(
                    if (usuario.estado == "activo") androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.errorContainer
                ).padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = usuario.estado.replaceFirstChar { it.uppercase() },
                    fontSize = 11.sp,
                    color = if (usuario.estado == "activo") VerdeAdmin else RojoAdmin,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ModuloRow(nombre: String, descripcion: String, pendiente: Boolean = false, completado: Boolean = false) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(nombre, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(descripcion, fontSize = 11.sp, color = Color.Gray)
        }
        Text(
            text = if (completado) "✅ Listo" else "🔧 Próx.",
            fontSize = 11.sp,
            color = if (completado) VerdeAdmin else androidx.compose.material3.MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Bold
        )
    }
}
```

---

### Paso 10.4: Dashboard y Métricas de Seguridad (`AdminDashboardScreen.kt`)

Visualiza tarjetas de resumen, usuarios activos, alertas totales y estado del sistema.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/admin/AdminDashboardScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val AzulCunaSegura @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
private val DoradoAdmin @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.tertiary
private val VerdeAdmin @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.secondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen() {
    val context = LocalContext.current
    val viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(context))

    val usuarios by viewModel.usuarios.collectAsState()
    val totalUsuarios by viewModel.totalUsuarios.collectAsState()
    val adminActual by viewModel.adminActual.collectAsState()

    val vecinos = usuarios.filter { it.rol != "admin" }
    val vectinosActivos = vecinos.filter { it.estado == "activo" }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumen de Estado", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(AzulCunaSegura.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            val initials = adminActual?.nombre?.split(" ")?.let {
                                if (it.size >= 2) "${it[0].first()}${it[1].first()}".uppercase()
                                else adminActual?.nombre?.take(2)?.uppercase()
                            } ?: "AD"
                            Text(initials, color = AzulCunaSegura, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("Bienvenido,", color = Color.Gray, fontSize = 13.sp)
                            Text(adminActual?.nombre ?: "Administrador", color = AzulCunaSegura, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(DoradoAdmin))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Administrador Global", color = DoradoAdmin, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }

            item {
                Text("Estadísticas de la Red", fontWeight = FontWeight.Bold, color = AzulCunaSegura, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f),
                        titulo = "Vecinos",
                        valor = vecinos.size.toString(),
                        icon = Icons.Default.People,
                        color = AzulCunaSegura
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        titulo = "Activos",
                        valor = vectinosActivos.size.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = VerdeAdmin
                    )
                    StatCard(
                        modifier = Modifier.weight(1f),
                        titulo = "Total",
                        valor = totalUsuarios.toString(),
                        icon = Icons.Default.Group,
                        color = DoradoAdmin
                    )
                }
            }

            item {
                Text("Módulos en Desarrollo", fontWeight = FontWeight.Bold, color = AzulCunaSegura, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        ModuloRow("🔥 Firebase Realtime DB", "Alertas en tiempo real entre vecinos", pendiente = true)
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        ModuloRow("📲 FCM Push", "Notificaciones push a celulares y TV", pendiente = true)
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        ModuloRow("📺 Smart TV App", "App de monitoreo en Android TV", pendiente = true)
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        ModuloRow("⌚ BLE Smartwatch", "Comunicación Bluetooth con watch", pendiente = true)
                        HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)
                        ModuloRow("🗺️ Mapa Comunitario", "Marcadores de vecinos en mapa", completado = true)
                    }
                }
            }
        }
    }
}
```

---

### Paso 10.5: Padrón y Moderación de Miembros (`AdminMembersScreen.kt`)

Permite buscar vecinos, cambiar roles y suspender o reactivar cuentas con sincronización a Firebase.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/admin/AdminMembersScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.edu.utng.cunasegura.domain.model.Usuario

private val AzulCunaSegura @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
private val VerdeAdmin @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.secondary
private val RojoAdmin @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMembersScreen() {
    val context = LocalContext.current
    val viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(context))

    val usuarios by viewModel.usuarios.collectAsState()
    val vecinos = usuarios.filter { it.rol != "admin" }
    var selectedTabIndex by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Miembros", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
                contentColor = AzulCunaSegura
            ) {
                Tab(
                    selected = selectedTabIndex == 0,
                    onClick = { selectedTabIndex = 0 },
                    text = { Text("Activos (${vecinos.size})") }
                )
                Tab(
                    selected = selectedTabIndex == 1,
                    onClick = { selectedTabIndex = 1 },
                    text = { Text("Solicitudes (0)") }
                )
            }

            if (selectedTabIndex == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (vecinos.isEmpty()) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("👥", fontSize = 32.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Sin vecinos registrados", color = Color.Gray, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    } else {
                        items(vecinos) { usuario ->
                            MiembroCard(
                                usuario = usuario,
                                onToggleEstado = { nuevoEstado ->
                                    viewModel.cambiarEstadoUsuario(usuario.uid, nuevoEstado)
                                }
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay solicitudes pendientes", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
private fun MiembroCard(
    usuario: Usuario,
    onToggleEstado: (String) -> Unit
) {
    val iniciales = usuario.nombre.trim().split(" ").filter { it.isNotBlank() }.take(2).joinToString("") { it.first().uppercase() }
    val esActivo = usuario.estado == "activo"

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(if (esActivo) AzulCunaSegura else Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                Text(iniciales, color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(usuario.nombre, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("📞 ${usuario.telefono}", fontSize = 12.sp, color = Color.Gray)
                if (usuario.correo.isNotBlank()) Text("✉️ ${usuario.correo}", fontSize = 11.sp, color = Color.Gray)
            }
            Column(horizontalAlignment = Alignment.End) {
                Box(
                    modifier = Modifier.clip(RoundedCornerShape(8.dp)).background(
                        if (esActivo) androidx.compose.material3.MaterialTheme.colorScheme.primaryContainer else androidx.compose.material3.MaterialTheme.colorScheme.errorContainer
                    ).padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = usuario.estado.replaceFirstChar { it.uppercase() },
                        fontSize = 11.sp,
                        color = if (esActivo) VerdeAdmin else RojoAdmin,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                TextButton(
                    onClick = { onToggleEstado(if (esActivo) "bloqueado" else "activo") },
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = if (esActivo) "Bloquear" else "Activar",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (esActivo) RojoAdmin else VerdeAdmin
                    )
                }
            }
        }
    }
}
```

---

### Paso 10.6: Parámetros Globales y Políticas del Sistema (`AdminConfigScreen.kt`)

Ajusta el radio de cobertura GPS, días de espera anti-abuso para nuevos miembros, tiempo de gracia anti-falsa alarma y vigencia temporal de alertas comunitarias.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/admin/AdminConfigScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

private val AzulCunaSegura @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminConfigScreen() {
    val context = LocalContext.current
    val viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(context))

    val network by viewModel.network.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()

    var radioMaximo by remember { mutableFloatStateOf(200f) }
    var tipoRed by remember { mutableStateOf("Abierta (GPS)") }
    var tiempoAntiFalsa by remember { mutableFloatStateOf(5f) }
    var checkVida by remember { mutableFloatStateOf(2f) }
    var esperarDiasNuevos by remember { mutableFloatStateOf(0f) }
    var tiempoVidaAlerta by remember { mutableFloatStateOf(720f) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(network) {
        network?.let {
            radioMaximo = it.radio.toFloat()
            tipoRed = if (it.tipo == "Abierta") "Abierta (GPS)" else "Cerrada (Solo QR)"
            tiempoAntiFalsa = it.tiempoAntiFalsa.toFloat()
            checkVida = it.checkVida.toFloat()
            esperarDiasNuevos = it.esperarDiasNuevos.toFloat()
        }
    }
    
    val globalVidaAlerta by viewModel.tiempoVidaAlerta.collectAsState()
    LaunchedEffect(globalVidaAlerta) {
        tiempoVidaAlerta = globalVidaAlerta.toFloat()
    }

    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            scope.launch {
                snackbarHostState.showSnackbar(it)
                viewModel.clearStatusMessage()
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Configuración de la Red", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Ajustes Globales y Políticas", fontWeight = FontWeight.Bold, color = AzulCunaSegura, fontSize = 15.sp)
            
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Tipo de Red
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Tipo de Red", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = tipoRed == "Abierta (GPS)",
                                onClick = { tipoRed = "Abierta (GPS)" },
                                colors = RadioButtonDefaults.colors(selectedColor = AzulCunaSegura)
                            )
                            Text("Abierta (GPS)", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(16.dp))
                            RadioButton(
                                selected = tipoRed == "Cerrada (Solo QR)",
                                onClick = { tipoRed = "Cerrada (Solo QR)" },
                                colors = RadioButtonDefaults.colors(selectedColor = AzulCunaSegura)
                            )
                            Text("Cerrada (Solo QR)", fontSize = 14.sp)
                        }
                    }

                    HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)

                    // Radio Máximo
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Radio Máximo de Cobertura", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                            Text("${radioMaximo.toInt()} m", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AzulCunaSegura)
                        }
                        Slider(
                            value = radioMaximo,
                            onValueChange = { radioMaximo = it },
                            valueRange = 50f..500f,
                            colors = SliderDefaults.colors(
                                thumbColor = AzulCunaSegura,
                                activeTrackColor = AzulCunaSegura,
                                inactiveTrackColor = AzulCunaSegura.copy(alpha = 0.2f)
                            )
                        )
                    }

                    HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)

                    // Tiempo Anti Falsas Alarmas
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Timer, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tiempo anti-falsas alarmas", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                            Text("${tiempoAntiFalsa.toInt()} s", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AzulCunaSegura)
                        }
                        Slider(
                            value = tiempoAntiFalsa,
                            onValueChange = { tiempoAntiFalsa = it },
                            valueRange = 3f..10f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = AzulCunaSegura,
                                activeTrackColor = AzulCunaSegura,
                                inactiveTrackColor = AzulCunaSegura.copy(alpha = 0.2f)
                            )
                        )
                    }
                    
                    HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)

                    // Check de vida
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Notifications, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Check de vida cada", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                            Text("${checkVida.toInt()} min", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AzulCunaSegura)
                        }
                        Slider(
                            value = checkVida,
                            onValueChange = { checkVida = it },
                            valueRange = 1f..5f,
                            steps = 3,
                            colors = SliderDefaults.colors(
                                thumbColor = AzulCunaSegura,
                                activeTrackColor = AzulCunaSegura,
                                inactiveTrackColor = AzulCunaSegura.copy(alpha = 0.2f)
                            )
                        )
                    }

                    HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)

                    // Esperar días para nuevos (Políticas de Abuso)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Espera para nuevos miembros", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                            Text("${esperarDiasNuevos.toInt()} días", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AzulCunaSegura)
                        }
                        Slider(
                            value = esperarDiasNuevos,
                            onValueChange = { esperarDiasNuevos = it },
                            valueRange = 0f..7f,
                            steps = 6,
                            colors = SliderDefaults.colors(
                                thumbColor = AzulCunaSegura,
                                activeTrackColor = AzulCunaSegura,
                                inactiveTrackColor = AzulCunaSegura.copy(alpha = 0.2f)
                            )
                        )
                        Text(
                            "Evita el envío inmediato de SOS a usuarios recién incorporados para prevenir el spam y abuso.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    
                    HorizontalDivider(color = androidx.compose.material3.MaterialTheme.colorScheme.outlineVariant)

                    // Tiempo de vida de la alerta
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassEmpty, contentDescription = null, tint = AzulCunaSegura, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tiempo vida de alerta (Global)", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                            Text("${tiempoVidaAlerta.toInt()} min", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AzulCunaSegura)
                        }
                        Slider(
                            value = tiempoVidaAlerta,
                            onValueChange = { tiempoVidaAlerta = it },
                            valueRange = 1f..1440f,
                            colors = SliderDefaults.colors(
                                thumbColor = AzulCunaSegura,
                                activeTrackColor = AzulCunaSegura,
                                inactiveTrackColor = AzulCunaSegura.copy(alpha = 0.2f)
                            )
                        )
                        Text(
                            "Oculta del mapa las alertas automáticamente una vez transcurrido este tiempo (1 a 1440 minutos).",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.guardarRedConfig(
                        tipo = if (tipoRed == "Abierta (GPS)") "Abierta" else "Cerrada",
                        radio = radioMaximo.toDouble(),
                        tiempoAntiFalsa = tiempoAntiFalsa.toDouble(),
                        checkVida = checkVida.toDouble(),
                        esperarDiasNuevos = esperarDiasNuevos.toInt(),
                        tiempoVidaAlerta = tiempoVidaAlerta.toDouble()
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulCunaSegura)
            ) {
                Text("Guardar Cambios", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}
```

---

### Paso 10.7: Estadísticas e Histórico de Alertas (`AdminStatsScreen.kt`)

Grafica tendencias de incidencias comunitarias y tiempos de respuesta mediante gráficos de barras reactivos (*Vico Charts*).

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/admin/AdminStatsScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.entry.entryOf
import mx.edu.utng.cunasegura.domain.model.Alerta
import java.text.SimpleDateFormat
import java.util.*

private val AzulCunaSegura @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
private val VerdeAdmin @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.secondary
private val RojoAdmin @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.error

data class DayStat(
    val dayName: String,
    val dateLabel: String,
    val realAlarms: Float,
    val falseAlarms: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStatsScreen() {
    val context = LocalContext.current
    val viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(context))

    val alertas by viewModel.alertas.collectAsState()

    // Calcular estadísticas de los últimos 7 días
    val dayStats = remember(alertas) {
        val calendar = Calendar.getInstance()
        (0..6).map { offset ->
            val cal = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -offset)
            }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfDay = cal.timeInMillis

            cal.set(Calendar.HOUR_OF_DAY, 23)
            cal.set(Calendar.MINUTE, 59)
            cal.set(Calendar.SECOND, 59)
            cal.set(Calendar.MILLISECOND, 999)
            val endOfDay = cal.timeInMillis

            val dayNameFormat = SimpleDateFormat("EEE", Locale.getDefault())
            val dateLabelFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
            val dayName = dayNameFormat.format(cal.time)
            val dateLabel = dateLabelFormat.format(cal.time)

            val dayAlerts = alertas.filter { it.creadoEn in startOfDay..endOfDay }
            val real = dayAlerts.count { !it.esFalsaAlarma }.toFloat()
            val falseAlarmsCount = dayAlerts.count { it.esFalsaAlarma }.toFloat()

            DayStat(dayName, dateLabel, real, falseAlarmsCount)
        }.reversed()
    }

    val chartEntryModel = remember(dayStats) {
        val entriesReal = dayStats.mapIndexed { index, stat -> entryOf(index, stat.realAlarms) }
        val entriesFalse = dayStats.mapIndexed { index, stat -> entryOf(index, stat.falseAlarms) }
        entryModelOf(entriesReal, entriesFalse)
    }

    val totalAlertas = alertas.size
    val alertasReales = alertas.count { !it.esFalsaAlarma }
    val alertasFalsas = alertas.count { it.esFalsaAlarma }
    val porcentajeFalsas = if (totalAlertas > 0) (alertasFalsas * 100) / totalAlertas else 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial y Estadísticas", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Métricas de Alerta Semanales", fontWeight = FontWeight.Bold, color = AzulCunaSegura, fontSize = 15.sp)

            // Tarjeta de Resumen con Gradiente Premium
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    AzulCunaSegura,
                                    MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Alertas Totales", color = Color.White.copy(alpha = 0.8f), fontSize = 14.sp)
                            Text(porcentajeFalsas.toString() + "% Falsas", color = Color.Yellow, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Text(totalAlertas.toString(), color = Color.White, fontSize = 36.sp, fontWeight = FontWeight.Black)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Reales", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                Text(alertasReales.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Falsas", color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                                Text(alertasFalsas.toString(), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Gráfico Vico
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(2.dp, RoundedCornerShape(16.dp))
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Distribución por Día", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.DarkGray)
                    Text("■ Reales   ■ Falsas", fontSize = 11.sp, color = Color.Gray, modifier = Modifier.padding(top = 4.dp))
                    
                    if (totalAlertas > 0) {
                        Chart(
                            chart = columnChart(),
                            model = chartEntryModel,
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis(
                                valueFormatter = { value, _ ->
                                    val idx = value.toInt()
                                    if (idx in dayStats.indices) {
                                        dayStats[idx].dayName
                                    } else {
                                        ""
                                    }
                                }
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .padding(top = 16.dp)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Sin datos de alertas para graficar esta semana", color = Color.Gray, textAlign = TextAlign.Center)
                        }
                    }
                }
            }

            // Historial de auditoría
            Text("Registro de Auditoría Reciente", fontWeight = FontWeight.Bold, color = AzulCunaSegura, fontSize = 15.sp)
            if (alertas.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No hay historial de alertas registrado", color = Color.Gray)
                    }
                }
            } else {
                alertas.sortedByDescending { it.creadoEn }.take(10).forEach { alerta ->
                    AlertaHistoryItem(alerta = alerta)
                }
            }
        }
    }
}

@Composable
fun AlertaHistoryItem(alerta: Alerta) {
    val date = Date(alerta.creadoEn)
    val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    val dateStr = format.format(date)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (alerta.esFalsaAlarma) RojoAdmin.copy(alpha = 0.1f) else VerdeAdmin.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (alerta.esFalsaAlarma) Icons.Default.Warning else Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = if (alerta.esFalsaAlarma) RojoAdmin else VerdeAdmin
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(alerta.nombreUsuario, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(dateStr, fontSize = 11.sp, color = Color.Gray)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (alerta.esFalsaAlarma) RojoAdmin.copy(alpha = 0.15f) else VerdeAdmin.copy(alpha = 0.15f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (alerta.esFalsaAlarma) "Falsa Alarma" else "Real/Atendida",
                    fontSize = 11.sp,
                    color = if (alerta.esFalsaAlarma) RojoAdmin else VerdeAdmin,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
```

---

### Paso 10.8: Perfil del Administrador Global (`AdminProfileScreen.kt`)

Muestra datos de la cuenta maestra de administración comunitaria y botón de cierre de sesión seguro.

> 📋 **INSTRUCCIÓN:** Copia el archivo `app/src/main/java/mx/edu/utng/cunasegura/presentation/admin/AdminProfileScreen.kt`:
```kotlin
package mx.edu.utng.cunasegura.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

private val AzulCunaSegura @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.primary
private val RojoSOS @androidx.compose.runtime.Composable get() = androidx.compose.material3.MaterialTheme.colorScheme.error

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: AdminViewModel = viewModel(factory = AdminViewModelFactory(context))
    val adminActual by viewModel.adminActual.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil de Administrador", color = androidx.compose.material3.MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AzulCunaSegura)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(androidx.compose.material3.MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(AzulCunaSegura.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "BA",
                    color = AzulCunaSegura,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = adminActual?.nombre ?: "Administrador Global",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AzulCunaSegura
            )
            Text(
                text = adminActual?.correo ?: "admin@cunasegura.com",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = RojoSOS)
            ) {
                Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Cerrar Sesión", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
```

---

## FASE 11: Compilación, Despliegue y Verificación Integral del Módulo Móvil

### Paso 11.1: Compilación del Módulo con Gradle

Para compilar el APK de depuración del módulo Smartphone:

```powershell
./gradlew :app:assembleDebug
```

El artefacto binario resultante se genera en:
`app/build/outputs/apk/debug/app-debug.apk`

---

### Paso 11.2: Instalación y Despliegue con ADB

Conecta tu dispositivo físico o inicia el emulador Android (API 34+) y ejecuta:

```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

### Paso 11.3: Matriz de Verificación E2E

| # | Flujo de Prueba | Acción Realizada | Resultado Esperado |
|---|---|---|---|
| 1 | **Splash & Autenticación** | Abrir la app por primera vez | Valida sesión en Firebase Auth y SQLite Room. Redirige a Login o Home según corresponda. |
| 2 | **Botón de Pánico Móvil** | Mantener pulsado el botón SOS 3s | Dispara cuenta regresiva, activa servicio GPS, sube alerta a Firebase y envía payload MQTT a Smart TV. |
| 3 | **Disparo desde Smartwatch** | Enviar evento de pánico desde Wear OS | `PhoneWearableService` intercepta mensaje `/cunasegura/alerta`, activa alerta móvil y envía SMS SOS a contactos. |
| 4 | **Sincronización Smart TV** | Escanear código QR de la pantalla TV | Vincula ID de TV a la red vecinal en Firebase y confirma enlace por MQTT. |
| 5 | **Consola Administrativa** | Iniciar sesión como `admin@cunasegura.com` | Redirige al panel maestro con métricas globales, padrón de miembros, ajuste de radio y estadísticas Vico. |

