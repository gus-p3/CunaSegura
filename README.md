# Cuna Segura - Sistema de Monitoreo Vecinal

Cuna Segura es una solución integral orientada a la seguridad ciudadana y la coordinación vecinal. El proyecto se compone de múltiples interfaces y módulos que interactúan en tiempo real mediante MQTT y Firebase para garantizar tiempos de respuesta rápidos y comunicación confiable.

## 📱 Módulo Móvil (Mobile App)
La aplicación principal de teléfono está diseñada para ser la herramienta que el vecino llevará siempre consigo.
- **Reporte Rápido**: Cuenta con botones de Pánico/SOS para emitir alertas inmediatas.
- **Geolocalización en Tiempo Real**: Visualización en mapas interactivos para localizar la alerta y coordinar ayuda.
- **Red Vecinal**: Gestión de redes de vecinos, en donde se asignan roles y permisos.
- **Modo Oscuro Integrado**: Tematización responsiva usando Material Design 3 en tonos azul medianoche.

![Módulo Móvil](assets/mobile_app_mockup.png)

## 📺 Módulo TV (Smart TV Dashboard)
Diseñado específicamente para centros de monitoreo, casetas de vigilancia o simplemente como una central de visualización en pantallas grandes (1080p y 4K).
- **Vista Cinemática (Dashboard)**: Interfaz estructurada para ser legible a la distancia con colores contrastantes y responsividad total.
- **Panel Lateral Dinámico**: Ocupa el 40% de la pantalla para mostrar las alertas recientes y estados críticos. Completamente colapsable y animado para expandir el mapa al 100%.
- **Mapas de OSMDroid Optimizados**: El mapa interactivo ha sido calibrado con ciclos de vida y User-Agents específicos para Android TV, permitiendo visualizar los incidentes al instante.
- **Alertas Claras**: Tarjetas grises/azules (`surfaceVariant`) que resaltan contra el fondo para evitar confusiones y no perder de vista los eventos de la base de datos (MQTT/Firebase).

![Módulo TV](assets/tv_app_mockup.png)

## ⌚ Módulo Wear OS (Smartwatch)
Este módulo contiene la aplicación diseñada para relojes inteligentes con **Wear OS**. Ofrece una interfaz premium, rápida y accesible para activar alarmas silenciosas y alertas de emergencia directamente desde la muñeca.

1. **Rastreo GPS en Tiempo Real**: Utiliza el sensor `GPS` nativo para rastrear coordenadas durante un SOS activo y traduce a direcciones mediante `Geocoder`.
2. **Simulación de Toques Segura**: Cuenta con una pantalla de cuenta regresiva de 5 segundos con progreso circular que permite cancelar la alerta.
3. **Chequeo de Vida (Life Check)**: Interfaz con efecto *glassmorphism* que le pregunta al usuario "¿ESTÁS BIEN?" ante sospechas de caídas o incidentes.

---
### Arquitectura y Tecnologías
* **Frontend**: Jetpack Compose (Compose for TV, Compose for Wear OS, Material 3).
* **Backend**: Firebase Realtime Database para la sincronización de nodos y cuentas de usuarios.
* **Comunicaciones**: MQTT protocol (ideal para IoT y microcontroladores tipo ESP32).
* **Tematización**: Tema unificado implementado a lo largo de todos los módulos usando esquemas semánticos de `MaterialTheme.colorScheme`.
* **Inyección de Dependencias**: Hilt
* **Almacenamiento Local**: Jetpack DataStore / Room

---

## 🚀 Instrucciones para Ejecutar el Proyecto

### Requisitos Previos
- Android Studio **Meerkat** (2024.3) o superior.
- JDK 17.
- Android SDK con API Level **30+** (para el módulo móvil).
- Wear OS SDK (API Level **30+**) para el módulo del reloj.
- Un emulador Wear OS configurado en AVD Manager, o un reloj físico con Wear OS conectado via ADB.

### Ejecutar el Módulo Móvil (`:app`)
1. En la barra superior de Android Studio, seleccionar la configuración **`app`**.
2. Seleccionar un emulador o dispositivo Android físico (API 30+).
3. Presionar **▶ Run**.

O desde la terminal (PowerShell):
```powershell
.\gradlew :app:installDebug
```

### Ejecutar el Módulo TV (`:cunaseguratv`)
1. Cambiar la configuración de ejecución a **`cunaseguratv`**.
2. Seleccionar un emulador Android TV (1080p).
3. Presionar **▶ Run**.

### Ejecutar el Módulo Wear OS (`:cunasegurawear`)
1. Cambiar la configuración a **`cunasegurawear`**.
2. Seleccionar un emulador Wear OS o reloj físico conectado.
3. Presionar **▶ Run**.

```powershell
# Compilar y empacar todos los módulos
.\gradlew assembleDebug
```

### Nota sobre configuración Kotlin / Room
El proyecto usa Kotlin `2.4.0`. Para evitar incompatibilidades del compilador de anotaciones de Room (`kapt`), se forzó la versión del parser de metadatos en el módulo Wear:
```kotlin
// cunasegurawear/build.gradle.kts
configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-metadata-jvm:2.4.0")
    }
}
```

---

## 📁 Estructura del Repositorio

```
CunaSegura/
├── app/                        # Módulo principal (Android Móvil)
│   └── src/main/
│       ├── java/               # Código fuente Kotlin (MVVM + Hilt)
│       └── res/                # Recursos (layouts, drawables, strings)
├── cunaseguratv/               # Módulo Smart TV
│   └── src/main/
│       └── java/               # Código fuente Compose for TV
├── cunasegurawear/             # Módulo Wear OS (Smartwatch)
│   └── src/main/
│       └── java/               # Código fuente Compose for Wear OS
├── evidencias/                 # Capturas de pantalla originales
├── assets/                     # Mockups generados
├── apk/                        # APKs generados de la aplicación
├── build.gradle.kts            # Configuración raíz de Gradle
├── settings.gradle.kts         # Configuración de módulos
└── README.md                   # Este archivo
```

---

*Cuna Segura - Cuidando a los nuestros, siempre conectados.*
