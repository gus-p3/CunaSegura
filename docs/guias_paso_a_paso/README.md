# 📚 Hub de Guías de Construcción e Implementación Paso a Paso

Bienvenido al centro oficial de **Guías de Construcción e Implementación Paso a Paso** para el ecosistema **Cuna Segura - Dolores Hidalgo**.

Cada documento en este directorio detalla de principio a fin, con código fuente completo e íntegro (sin omisiones ni fragmentos incompletos), la configuración de dependencias, capa de datos, arquitectura MVVM/Clean Architecture, sincronización en la nube (Firebase), mensajería IoT de baja latencia (MQTT) y capa de presentación para cada uno de los dispositivos que componen la plataforma.

---

## 📑 Índice de Módulos

| Plataforma / Módulo | Archivo de Guía | Estado | Tecnologías Principales |
|---|---|---|---|
| 📱 **Smartphone (Móvil)** | [**`MOVIL_PASO.md`**](MOVIL_PASO.md) | ✅ **11 Fases Completas** | Jetpack Compose M3, Room SQLite, Fused Location, ZXing QR Scanner, Vico Charts, Paho MQTT, Firebase Auth/RTDB, Admin Console. |
| 📺 **Smart TV (Central Monitoreo)** | [**`TV_PASO.md`**](TV_PASO.md) | ✅ **9 Fases Completas** | Compose for TV, Leanback, D-Pad Focus, OSMDroid (OpenStreetMap), Paho MQTT over SSL, Firebase RTDB, MediaPlayer Alarma. |
| ⌚ **Wear OS (Reloj Inteligente)** | [**`WEAROS_PASO.md`**](WEAROS_PASO.md) | ✅ **8 Fases Completas** | Compose for Wear OS, Wearable Data Layer API, Room Cache Offline, Detección Háptica de Toques, GPS Tracker, Life Check. |

---

## 🎯 Estándar de Estructura de las Guías

Todas las guías en este repositorio respetan un formato estandarizado para facilitar su lectura y réplica técnica:

1. **Objetivos de Aprendizaje Claros**: 5 conceptos fundamentales por plataforma.
2. **Fases Secuenciales de Construcción**: De la configuración de Gradle y Manifiesto hasta la capa de interfaz y ViewModel.
3. **Bloques de Código Verbatim y Completos**: Cada archivo se presenta con su paquete, imports completos y comentarios KDoc exhaustivos.
4. **Instrucciones Claras**: Directivas `> 📋 **INSTRUCCIÓN:**`, `> **CONCEPTO CLAVE:**`, `> 💡 **BUENA PRÁCTICA:**` y `> ⚠️ **ADVERTENCIA:**`.
5. **Verificación y Pruebas**: Instrucciones de compilación (`gradlew`), instalación (`adb`) y matrices de pruebas funcionales end-to-end.
