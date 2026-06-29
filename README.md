# 🛡️ Cuna Segura

> Red de seguridad vecinal interconectada para Android — Alerta de emergencia rápida y discreta desde cualquier dispositivo del ecosistema.

---

## 📋 Datos del Proyecto

| Campo | Detalle |
|---|---|
| **Nombre del Proyecto** | Cuna Segura |
| **Materia** | Desarrollo de Aplicaciones para Dispositivos Inteligentes |
| **Grupo** | GIDS6092 |

---

## 👥 Integrantes

| Nombre |
|---|
| Brandon Gustavo Mendoza Amaro |
| Karen Anahí Padrón Martínez |
| Lizeth Ramírez Ramírez |

---

## 🎯 Objetivo

El objetivo de **Cuna Segura** es crear una red de seguridad vecinal interconectada para Android que permita a los usuarios activar alertas de emergencia de forma **rápida y discreta**. El sistema busca cerrar la brecha de respuesta ante situaciones de peligro, coordinando el uso de tres dispositivos (móvil, smartwatch y Smart TV) para asegurar que el usuario siempre tenga una forma de pedir auxilio, gestionando la lógica de forma **local** en los dispositivos, priorizando la privacidad.

---

## ✨ Descripción de Funcionalidades

El ecosistema de Cuna Segura se basa en **tres pilares interconectados** que operan de manera local:

### 📱 Hub Central (Teléfono)
- Gestión de contactos de emergencia personalizados.
- Configuración de alertas y acciones por número de toques.
- Vinculación local vía **Bluetooth Low Energy (BLE)** con el smartwatch.
- Visualización de mapa con marcadores diferenciados (azul = usuario, rojo = vecino en peligro).
- Panel de administración (ícono de escudo) para gestión interna y configuración de red.

### ⌚ Botón de Pánico (Smartwatch — Wear OS)
- Activación de alertas discretas mediante un sistema configurable de **1 a 4 toques** físicos.
- Base de datos Room local con configuración precargada de acciones por toque:
  - **1 toque:** Enviar SMS de ayuda
  - **2 toques:** Compartir ubicación GPS
  - **3 toques:** Encender bocina de vecino
  - **4 toques:** Llamada de emergencia al 911
- Rastreo GPS en tiempo real con geocodificación de dirección física.
- Pantalla de cuenta regresiva de **5 segundos** (cancelable) anti-falsas alarmas.
- Interfaz de **Chequeo de Vida** con efecto glassmorphism ("¿ESTÁS BIEN?").
- Simulación segura de toques desde la pantalla de estado.

### 📺 Central de Monitoreo (Smart TV)
- Recibe y muestra notificaciones de alerta de vecinos mediante comunicación directa entre dispositivos.
- Permite visualizar la ubicación de quien activó la alerta.

### 🔧 Sistema de Acciones
Cuatro acciones personalizables asociadas al número de toques:
1. Enviar mensaje de auxilio
2. Compartir ubicación GPS
3. Activar alarma en TV del vecino
4. Llamar al 911

### 🛡️ Anti-Falsas Alarmas (3 capas de seguridad)
1. **Confirmación:** Ventana de 5 segundos para cancelar la alerta.
2. **Verificación de Vida:** Chequeo automático cada 2 minutos durante SOS activo.
3. **Reporte Vecinal:** Los vecinos pueden reportar falsa alarma desde su dispositivo.

### 🌐 Red Vecinal
- Conexiones mutuas directas mediante **GPS** (radio de 200 m) o escaneo de **código QR**.
- Arquitectura peer-to-peer para garantizar privacidad y control local sin servidores externos.

---

## 🛠️ Tecnologías Utilizadas

| Categoría | Tecnología |
|---|---|
| **Lenguaje** | Kotlin 2.4.0 |
| **UI Móvil** | Jetpack Compose |
| **UI Wear OS** | Jetpack Compose for Wear OS |
| **UI Smart TV** | Leanback Library |
| **Almacenamiento Local** | Jetpack DataStore / Room |
| **Mapas** | Google Maps SDK |
| **Conectividad** | Android BLE API (Bluetooth Low Energy) |
| **Gestión Asíncrona** | Coroutines + Flow |
| **Inyección de Dependencias** | Hilt |
| **Arquitectura** | MVVM + Repository Pattern |
| **Geolocalización** | LocationManager + Geocoder |

---

## 🚀 Instrucciones para Ejecutar el Proyecto

### Requisitos Previos
- Android Studio **Meerkat** (2024.3) o superior.
- JDK 17.
- Android SDK con API Level **30+** (para el módulo móvil).
- Wear OS SDK (API Level **30+**) para el módulo del reloj.
- Un emulador Wear OS configurado en AVD Manager, o un reloj físico con Wear OS conectado via ADB.

### Clonar el Repositorio
```bash
git clone https://github.com/gus-p3/CunaSegura.git
cd CunaSegura
```

### Abrir en Android Studio
1. Abrir Android Studio.
2. Seleccionar **File → Open** y navegar a la carpeta clonada.
3. Esperar a que Gradle sincronice las dependencias.

### Ejecutar el Módulo Móvil (`:app`)
1. En la barra superior de Android Studio, seleccionar la configuración **`app`**.
2. Seleccionar un emulador o dispositivo Android físico (API 30+).
3. Presionar **▶ Run**.

O desde la terminal (PowerShell):
```powershell
.\gradlew :app:installDebug
```

### Ejecutar el Módulo Wear OS (`:cunasegurawear`)
1. En Android Studio, cambiar la configuración de ejecución a **`cunasegurawear`**.
2. Seleccionar un emulador Wear OS o reloj físico conectado.
3. Presionar **▶ Run**.

O desde la terminal:
```powershell
# Compilar APK de debug
.\gradlew :cunasegurawear:assembleDebug

# Instalar directamente en emulador/reloj conectado
.\gradlew :cunasegurawear:installDebug
```

### Compilar todos los módulos
```powershell
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

## 📸 Capturas de Pantalla

> Las capturas de pantalla de la aplicación en ejecución se encuentran en la carpeta [`/evidencias`](./evidencias/).

---

## 📁 Estructura del Repositorio

```
CunaSegura/
├── app/                        # Módulo principal (Android Móvil)
│   └── src/main/
│       ├── java/               # Código fuente Kotlin (MVVM + Hilt)
│       └── res/                # Recursos (layouts, drawables, strings)
├── cunasegurawear/             # Módulo Wear OS (Smartwatch)
│   └── src/main/
│       └── java/               # Código fuente Compose for Wear OS
├── evidencias/                 # Capturas de pantalla de la aplicación
├── apk/                        # APKs generados de la aplicación
├── build.gradle.kts            # Configuración raíz de Gradle
├── settings.gradle.kts         # Configuración de módulos
└── README.md                   # Este archivo
```

---

## 📦 APK

El archivo APK generado se encuentra en la carpeta [`/apk`](./apk/).

---

## 🎨 Paleta de Colores

| Rol | Color | Hex |
|---|---|---|
| **Primario (Cian)** | Estado Seguro / Títulos | `#85D1E8` |
| **Fondo / Superficie** | Gris grafito profundo (OLED) | `#0F1416` |
| **Alerta / Error** | Rojo suave (SOS / Advertencias) | `#FFB4AB` |

---

## ⌚ Módulo Wear OS (`:cunasegurawear`)

Este módulo contiene la aplicación diseñada para relojes inteligentes con **Wear OS**. Ofrece una interfaz premium, rápida y accesible para activar alarmas silenciosas y alertas de emergencia directamente desde la muñeca.

### Características Principales

1. **Base de Datos Local (Room)**
   - Almacena la configuración de acciones asociadas al número de toques físicos en el reloj (`TouchConfig`).
   - Se precarga automáticamente en el primer inicio de la app con las siguientes configuraciones por defecto:
     - **1 toque:** Enviar SMS de ayuda
     - **2 toques:** Compartir ubicación GPS
     - **3 toques:** Encender bocina de vecino
     - **4 toques:** Llamada de emergencia al 911
   - Conexión lista para ser editada y sincronizada por la aplicación móvil.

2. **Rastreo GPS en Tiempo Real**
   - Utiliza el sensor `GPS` nativo a través de `LocationManager` para rastrear las coordenadas del usuario durante un SOS activo.
   - Traduce las coordenadas de latitud/longitud a una dirección física legible (calle, número) de forma asíncrona usando `Geocoder` sobre `Dispatchers.IO`.
   - Renderiza un mapa estilizado en modo oscuro con la dirección física actual en tiempo real en la pantalla `AlertActiveScreen`.

3. **Simulación de Toques Física y Segura**
   - Cuenta con botones dedicados de simulación de "3 Toques" y "4 Toques" en la pantalla de estado (`StatusScreen`), evitando falsos positivos al tocar accidentalmente la pantalla general.
   - Cuenta con una pantalla de cuenta regresiva de 5 segundos (`CountdownScreen`) con progreso circular dinámico que permite cancelar la alerta antes de que se notifique a los contactos.

4. **Chequeo de Vida (Life Check)**
   - Interfaz con efecto *glassmorphism* que le pregunta al usuario "¿ESTÁS BIEN?" ante sospechas de caídas o incidentes, permitiendo responder de forma ergonómica ("SÍ" o "NO") en la pantalla táctil.

5. **Diseño Visual Unificado (Material Theme)**
   - Utiliza la misma paleta de colores corporativa (modo oscuro) de la aplicación móvil de Android:
     - **Primario (Cian):** `#85D1E8` (Estado Seguro / Títulos de localización)
     - **Fondo / Superficie:** `#0F1416` (Gris grafito profundo para optimizar consumo de batería OLED)
     - **Alerta / Error:** `#FFB4AB` (Rojo suave y legible para estados SOS y advertencias)

---

### Solución a Conflictos del Compilador (Kotlin 2.4.0)

El proyecto utiliza Kotlin `2.4.0`. Para evitar que el procesador de anotaciones de Room (`kapt`) falle debido a la incompatibilidad con la versión de metadatos del bytecode, se forzó la versión del parser de metadatos en Gradle:

```kotlin
// cunasegurawear/build.gradle.kts
configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlin:kotlin-metadata-jvm:2.4.0")
    }
}
```

---

### Compilación e Instalación

Para compilar y empaquetar el módulo de Wear OS de forma independiente, ejecuta el siguiente comando en la terminal:

```powershell
# Compilar APK de desarrollo para el reloj
.\gradlew :cunasegurawear:assembleDebug
```

Para instalar el APK directamente en un emulador o reloj inteligente Wear OS conectado mediante ADB:

```powershell
# Instalar APK en el dispositivo
.\gradlew :cunasegurawear:installDebug
```
