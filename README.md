# 🛡️ Cuna Segura — Documentación Técnica y Manual de Arquitectura de Software

> **Cuna Segura** es un ecosistema integral y distribuido de seguridad vecinal (IoT + Mobile) diseñado nativamente para el entorno Android. A través de la orquestación simultánea de teléfonos móviles (Hubs), relojes inteligentes (Wear OS - Disparadores) y Smart TVs (Centrales de Monitoreo), el sistema permite la emisión, enrutamiento y recepción de alertas de pánico de manera ultrarrápida, discreta y altamente resiliente. 
> 
> *Este documento es el Manual Arquitectónico Oficial, estructurado para proveer toda la información técnica, lógica y operativa requerida por desarrolladores, ingenieros de QA, arquitectos de software y administradores de despliegue.*

---

## 🚀 Documentación por Ecosistema

Explora la documentación detallada de cada módulo, con capturas de pantalla, explicaciones arquitectónicas y guías de uso paso a paso:

| 📱 Módulo Móvil | 📺 Módulo Smart TV | ⌚ Módulo Wear OS |
| :---: | :---: | :---: |
| [**Manual de Smartphone**](docs/SMARTPHONE.md) | [**Manual de Smart TV**](docs/TV.md) | [**Manual de Reloj Inteligente**](docs/WEAROS.md) |
| *El centro de operaciones y configuración del sistema.* | *La estación de monitoreo y despliegue del hogar.* | *El disparador físico y sensor táctico.* |

> 👉 **[📸 Acceder a la Galería Interactiva de Capturas (Wear OS)](docs/CAPTURAS_WEAROS.md)**
>
---
## Aprobacion del Proyecto

| 📄 Documento Adicional | 🎥 Video Demostrativo | 🎥 Video de Aprobación |
| :---: | :---: | :---: |
| [**Carta Validación (PDF)**](docs/CartaValidacion_Beneficiario_DDI_UTNG.pdf) | [**Ver Video en YouTube**](https://youtu.be/1BSrhtfrpDU?si=mwk655zXUkczbK46) | [**Ver Video en YouTube**](https://youtu.be/1AcKAkqf2QM) |
| *Carta de Validación* | *Video de demostración Proyecto* | *Video del Aprobación de Proyecto* | 

---

## 📑 Índice General

1. [Resumen Ejecutivo y Motivación](#1-resumen-ejecutivo-y-motivación)
    - 1.1 [El Problema de la Seguridad Tradicional](#11-el-problema-de-la-seguridad-tradicional)
    - 1.2 [Propuesta de Valor Técnica y Justificación Arquitectónica](#12-propuesta-de-valor-técnica-y-justificación-arquitectónica)
    - 1.3 [Impacto Funcional y Satisfacción del Beneficiario](#13-impacto-funcional-y-satisfacción-del-beneficiario)
    - 1.4 [Datos del Proyecto Académico](#14-datos-del-proyecto-académico)
2. [Arquitectura de Software y Modelado del Sistema](#2-arquitectura-de-software-y-modelado-del-sistema)
    - 2.1 [Topología Descentralizada Híbrida](#21-topología-descentralizada-híbrida)
    - 2.2 [Flujo de Comunicación de Extremo a Extremo (End-to-End)](#22-flujo-de-comunicación-de-extremo-a-extremo-end-to-end)
    - 2.3 [Diagrama de Contexto del Sistema (Mermaid)](#23-diagrama-de-contexto-del-sistema-mermaid)
    - 2.4 [Patrones de Diseño Utilizados](#24-patrones-de-diseño-utilizados)
3. [Infraestructura de Comunicaciones y Protocolos Core](#3-infraestructura-de-comunicaciones-y-protocolos-core)
    - 3.1 [Firebase Realtime Database (FRDB) — La Fuente de la Verdad](#31-firebase-realtime-database-frdb--la-fuente-de-la-verdad)
    - 3.2 [Eclipse Paho MQTT — Mensajería Push Ultrarrápida](#32-eclipse-paho-mqtt--mensajería-push-ultrarrápida)
    - 3.3 [Bluetooth Low Energy (BLE) y Wearable Data Layer](#33-bluetooth-low-energy-ble-y-wearable-data-layer)
4. [Análisis Profundo de los Módulos Nativos](#4-análisis-profundo-de-los-módulos-nativos)
    - 4.1 [Módulo Móvil (`:app`) — El Centro de Operaciones](#41-módulo-móvil-app--el-centro-de-operaciones)
    - 4.2 [Módulo Wear OS (`:cunasegurawear`) — El Disparador de Hardware](#42-módulo-wear-os-cunasegurawear--el-disparador-de-hardware)
    - 4.3 [Módulo Smart TV (`:cunaseguratv`) — La Estación de Monitoreo](#43-módulo-smart-tv-cunaseguratv--la-estación-de-monitoreo)
5. [Mecanismos Anti-Falsas Alarmas y Protocolos de Seguridad](#5-mecanismos-anti-falsas-alarmas-y-protocolos-de-seguridad)
    - 5.1 [Retardo de Buffer (Countdown Cancelable)](#51-retardo-de-buffer-countdown-cancelable)
    - 5.2 [Life Check (Glassmorphism Interactivo)](#52-life-check-glassmorphism-interactivo)
    - 5.3 [Roles Administrativos y Persistencia de Bloqueos](#53-roles-administrativos-y-persistencia-de-bloqueos)
6. [Estructura del Proyecto y Clean Architecture](#6-estructura-del-proyecto-y-clean-architecture)
    - 6.1 [Árbol de Directorios Base](#61-árbol-de-directorios-base)
    - 6.2 [Módulo `app` Detallado](#62-módulo-app-detallado)
    - 6.3 [Inyección de Dependencias con Hilt](#63-inyección-de-dependencias-con-hilt)
7. [Tecnologías, Dependencias y SDKs](#7-tecnologías-dependencias-y-sdks)
    - 7.1 [Matriz Tecnológica Core](#71-matriz-tecnológica-core)
    - 7.2 [Manejo de Tareas Asíncronas (Coroutines & Flow)](#72-manejo-de-tareas-asíncronas-coroutines--flow)
8. [Guía Maestra de Interfaz de Usuario (UI/UX)](#8-guía-maestra-de-interfaz-de-usuario-uiux)
    - 8.1 [Psicología del Color para Emergencias](#81-psicología-del-color-para-emergencias)
    - 8.2 [Componentes Semánticos y Efectos Visuales](#82-componentes-semánticos-y-efectos-visuales)
    - 8.3 [Accesibilidad y Adaptabilidad](#83-accesibilidad-y-adaptabilidad)
9. [🛠️ Guía de Desarrollo Paso a Paso](#9-guía-de-desarrollo-paso-a-paso)
    - 9.1 [Fase 1 — Configuración del Entorno de Desarrollo](#91-fase-1--configuración-del-entorno-de-desarrollo)
    - 9.2 [Fase 2 — Aprovisionamiento de Servicios Cloud](#92-fase-2--aprovisionamiento-de-servicios-cloud)
    - 9.3 [Fase 3 — Clonación y Configuración del Repositorio](#93-fase-3--clonación-y-configuración-del-repositorio)
    - 9.4 [Fase 4 — Compilación y Generación de APKs](#94-fase-4--compilación-y-generación-de-apks)
    - 9.5 [Fase 5 — Despliegue en Dispositivos y Emuladores](#95-fase-5--despliegue-en-dispositivos-y-emuladores)
    - 9.6 [Fase 6 — Verificación Funcional End-to-End](#96-fase-6--verificación-funcional-end-to-end)
10. [Guía de Configuración, Compilación y Despliegue (Referencia Rápida)](#10-guía-de-configuración-compilación-y-despliegue-referencia-rápida)
    - 10.1 [Requisitos del Entorno de Desarrollo](#101-requisitos-del-entorno-de-desarrollo)
    - 10.2 [Aprovisionamiento de Claves de API (Google y HiveMQ)](#102-aprovisionamiento-de-claves-de-api-google-y-hivemq)
    - 10.3 [Instrucciones de Gradle por Línea de Comandos](#103-instrucciones-de-gradle-por-línea-de-comandos)
11. [Casos de Uso y Flujos de Pantalla (User Journeys)](#11-casos-de-uso-y-flujos-de-pantalla-user-journeys)
    - 11.1 [Flujo: Creación y Administración de Red](#111-flujo-creación-y-administración-de-red)
    - 11.2 [Flujo: Disparo de Alarma P2P](#112-flujo-disparo-de-alarma-p2p)
12. [Glosario de Términos](#12-glosario-de-términos)
13. [Anexos (Documentación Externa y Visual)](#13-anexos-documentación-externa-y-visual)

---

## 1. Resumen Ejecutivo y Motivación

### 1.1 El Problema de la Seguridad Tradicional
En situaciones de extremo peligro físico (asaltos, violencia doméstica, robos a casa habitación), la víctima promedio rara vez tiene el tiempo, la agilidad motriz, o la oportunidad táctica para:
1. Extraer su teléfono móvil del bolsillo o bolso.
2. Desbloquear la pantalla.
3. Abrir una aplicación de seguridad.
4. Presionar botones táctiles en una interfaz visual.

Las soluciones tradicionales de software de emergencias fallan sistemáticamente en el paso crítico de la "iniciación", volviéndolas inútiles en escenarios de confrontación inminente. Por otro lado, la monitorización de estas emergencias suele quedar relegada al interior del móvil del receptor, perdiendo tracción si los familiares o vecinos no tienen su celular a la mano.

### 1.2 Propuesta de Valor Técnica y Justificación Arquitectónica

Cuna Segura resuelve el problema de latencia de iniciación redistribuyendo las responsabilidades de hardware entre tres clases de dispositivos Android con **roles arquitectónicos diferenciados y bien acotados**:

| Dispositivo | Rol Técnico | Tecnología Central |
|---|---|---|
| **Wear OS (Smartwatch)** | Disparador de eventos físicos + sensor táctico | `onKeyDown` / Acelerómetro → State Machine → BLE |
| **Smartphone (Android)** | Hub de enrutamiento, autenticación y geocodificación | Firebase Auth + FRDB + MQTT Publisher + FusedLocation |
| **Android TV** | Estación de monitoreo pasiva Always-On | MQTT Subscriber + Compose for TV + Google Maps |

A nivel de protocolo, la decisión de combinar **Firebase Realtime Database (estado persistente)** con **MQTT via Eclipse Paho (eventos efímeros críticos)** es deliberada y no arbitraria:

* **Firebase WebSockets** son óptimos para sincronizar estructuras de datos duraderas (perfiles, redes, configuración) pero incurren en *overhead* de reconexión que resulta inaceptable para una alerta de pánico.
* **MQTT sobre TCP** mantiene sesiones persistentes con `keep-alive` de bajo costo en dispositivos de baja potencia (Smart TVs con RAM limitada). El broker `HiveMQ` sobre el puerto `8883` con TLS garantiza confidencialidad en producción sin sacrificar latencia de entrega (sub-200ms en condiciones LTE).
* **BLE + Wearable Data Layer** cubre el gap local cuando no existe conectividad a internet, garantizando que la señal del reloj llegue al teléfono mediante radio de corto alcance.

Esta **arquitectura de triple canal** garantiza que ningún fallo de una infraestructura deje al sistema sin capacidad de transmitir la alerta.

### 1.3 Impacto Funcional y Satisfacción del Beneficiario

Más allá de la arquitectura, la validación con el beneficiario final (vecinos y familias de zonas residenciales de alta incidencia delictiva) confirmó las siguientes métricas de satisfacción funcional:

* **Reducción de pasos de iniciación de emergencia:** De 4 interacciones táctiles (desbloquear, abrir app, navegar, presionar) a **1 pulsación física ciega** en el reloj. Esto es crítico para víctimas bajo estrés agudo con visión de túnel cognitiva.
* **Cobertura doméstica garantizada:** Al redirigir la alerta al Smart TV del hogar, se elimina el problema de "nadie vio el WhatsApp". La TV actúa como altavoz visual irrechazable en el entorno familiar.
* **Resiliencia ante pérdida del teléfono:** Si el agresor sustrae el teléfono, el reloj puede operar en modo Standalone (con Wi-Fi o LTE propio) para continuar emitiendo el SOS sin depender del hub móvil.
* **Carta de validación del beneficiario:** El proyecto fue revisado y aprobado formalmente. Ver [Carta de Validación (PDF)](docs/CartaValidacion_Beneficiario_DDI_UTNG.pdf).

El sistema no es una solución genérica de botón de pánico: es un **protocolo de respuesta vecinal distribuido**, donde cada dispositivo del ecosistema Android asume un rol preciso en la cadena de respuesta, reduciendo la dependencia de un único punto de falla.

### 1.4 Datos del Proyecto Académico
* **Identificador del Proyecto:** Cuna Segura (CunaSegura-Android)
* **Institución Educativa / Materia:** Desarrollo de Aplicaciones para Dispositivos Inteligentes.
* **Grupo:** GIDS6092.
* **Equipo de Ingeniería y Desarrollo:**
    * Brandon Gustavo Mendoza Amaro
    * Karen Anahí Padrón Martínez
    * Lizeth Ramírez Ramírez
* **Versión Actual:** 3.0.0 (Release Candidate)

---

## 2. Arquitectura de Software y Modelado del Sistema

La arquitectura general está diseñada priorizando tres factores de calidad de software: **Baja Latencia, Resiliencia ante desconexiones, y Extensibilidad del Ecosistema.**

### 2.1 Topología Descentralizada Híbrida
En lugar de depender exclusivamente de servidores HTTP/REST tradicionales, Cuna Segura implementa un modelo **Real-Time Event-Driven** (Arquitectura Orientada a Eventos en Tiempo Real). 
* **State Management (Firebase):** Todos los estados persistentes que no son críticos de un milisegundo a otro (como la lista de miembros de una red, el nombre del usuario, la configuración de toques) se administran en Firebase. Firebase ofrece sincronización reactiva, pero sufre de retrasos o desconexiones forzadas por el sistema operativo si la aplicación pasa a segundo plano.
* **Event Streaming (MQTT):** Todo evento crítico que represente peligro inminente, o la vinculación instantánea de una Smart TV, viaja mediante el protocolo IoT por excelencia: MQTT. Esto permite mantener conexiones TCP ligeras abiertas (keep-alive) en la Smart TV para recibir *push notifications* de forma pura.

### 2.2 Flujo de Comunicación de Extremo a Extremo (End-to-End)
1. **Detección Táctil:** El usuario realiza una pulsación física repetida en su Smartwatch. El módulo `cunasegurawear` captura el evento `onKeyDown` o `onSensorChanged`.
2. **Interpretación y Conteo:** Una máquina de estados en el reloj cuenta el número de toques e inicia el *Countdown* de 5 segundos.
3. **Resolución de Reglas de Negocio:** El reloj consulta su base de datos local (Room DB) para verificar qué acción corresponde a "X" toques (por ejemplo, 3 toques = Activar Alarma en TVs).
4. **Enlace BLE:** Si la acción requiere conectividad externa, el reloj transmite el evento de emergencia vía Bluetooth Low Energy (o Wear Data Layer) al teléfono emparejado.
5. **Geocodificación Local:** El teléfono (o el reloj en modo Standalone) intercepta el Location Services del sistema operativo, recupera latitud y longitud, e invoca a Google Geocoder para traducir a una dirección física.
6. **Publicación IoT:** El módulo móvil instancia a `MqttPublisher` y envía un Payload codificado en JSON al servidor MQTT, especificando el `networkId` y las coordenadas.
7. **Recepción en Pantalla:** Todas las Smart TVs en esa red, ejecutando el módulo `cunaseguratv`, tienen a `MqttTvSubscriber` consumiendo eventos. Al detectar el JSON, sobreescriben la UI en pantalla completa disparando un mapa 2D y una sirena.

### 2.3 Diagrama de Contexto del Sistema (Mermaid)

```mermaid
graph TD
    subgraph Capa Física (Hardware del Usuario)
        W[Smartwatch Wear OS\nBotones Físicos y Acelerómetro]
        M[Teléfono Móvil Android\nGPS y Antenas LTE/Wi-Fi]
        TV[Android TV / Smart TV\nPantalla Principal Hogar]
    end
    
    subgraph Capa de Red e Infraestructura Cloud
        FRDB[(Firebase Real-Time DB\nSync Reactivo NoSQL)]
        MQTT((Broker MQTT HiveMQ\nGestión de Tópicos TCP))
        GMAPS[Google Maps API\nRenderizado de Tiles]
    end
    
    W -- "BLE (Data Layer API) / Config Toques" --> M
    W -- "Actualización de Estado Standalone (Wi-Fi/LTE)" --> FRDB
    M -- "Lee/Escribe Perfiles, Redes, Dispositivos" --> FRDB
    M -- "Publica Alerta de Emergencia (JSON Payload)" --> MQTT
    MQTT -- "Emite Broadcast Push / Notificación sin Polling" --> TV
    FRDB -- "Actualiza marcadores de mapa reactivamente" --> TV
    M -- "Renderiza Mapas en Pantalla Móvil" --> GMAPS
    TV -- "Renderiza Mapas a 1080p/4K" --> GMAPS
```

### 2.4 Patrones de Diseño Utilizados
* **MVVM (Model-View-ViewModel):** Empleado rigurosamente en todos los módulos de Compose. Las interfaces (Views) son componentes declarativos sin estado, que reaccionan a objetos `StateFlow` expuestos por ViewModels inyectados.
* **Repository Pattern:** Oculta las fuentes de datos (Room o Firebase) detrás de interfaces puras (e.g., `INetworkRepository`), permitiendo hacer testing y cambiar implementaciones sin tocar la UI.
* **Singleton (Patrón de diseño creacional):** Utilizado para inyectar clientes costosos de instanciar, como clientes MQTT o conectores de Firebase Auth a lo largo del ciclo de vida (vía Hilt `@Singleton`).
* **State Machine:** Aplicado para el ciclo de vida de la alarma en WearOS (Reposo -> Contando -> Activa -> Resolviendo -> Cancelada).

---

## 3. Infraestructura de Comunicaciones y Protocolos Core

Esta sección detalla de manera minuciosa cómo interactúan las APIs subyacentes para hacer funcionar el sistema.

### 3.1 Firebase Realtime Database (FRDB) — La Fuente de la Verdad
Firebase no solo almacena la información, sino que sincroniza el árbol de datos en milisegundos mediante WebSockets.

**Esquema de Base de Datos Estricto (JSON):**

```json
{
  "usuarios": {
    "UID_FIREBASE_ALFANUMERICO_1": {
      "nombre": "Brandon Mendoza",
      "telefono": "+524621234567",
      "correo": "brandon.mendoza@alumnos.utng.edu.mx",
      "rol": "usuario",
      "estado": "activo",
      "networkId": "net_9876xyz",
      "tvVinculada": true,
      "fechaIngreso": 1720893452000
    },
    "UID_FIREBASE_ALFANUMERICO_2": {
      "nombre": "Usuario Problemático",
      "estado": "bloqueado",
      "networkId": "UID_FIREBASE_ALFANUMERICO_2" 
    }
  },
  "networks": {
    "net_9876xyz": {
      "id": "net_9876xyz",
      "nombre": "Vecinos Unidos Sector 4",
      "latitud": 21.1685,
      "longitud": -101.6732,
      "radio": 500.0,
      "tipo": "Cerrada",
      "adminId": "UID_FIREBASE_ALFANUMERICO_1",
      "tvId": "smart_tv_samsung_01",
      "miembros": {
        "UID_FIREBASE_ALFANUMERICO_1": true,
        "UID_OTRO_VECINO": true
      }
    }
  },
  "configuracion_toques": {
    "UID_FIREBASE_ALFANUMERICO_1": {
      "1": "Enviar Mensaje",
      "2": "Compartir GPS",
      "3": "Activar Alarma",
      "4": "Llamar 911"
    }
  },
  "alertas": {
    "alerta_uuid_hjh765": {
      "usuarioId": 1,
      "uidReal": "UID_FIREBASE_ALFANUMERICO_1",
      "nombreUsuario": "Brandon Mendoza",
      "latitud": 21.1680,
      "longitud": -101.6730,
      "estado": "activa",
      "nivelAlerta": 3,
      "networkId": "net_9876xyz",
      "timestamp": 1720893500000
    }
  },
  "configuracion_global": {
    "checkVida": 1.0,
    "esperarDiasNuevos": 7,
    "radio": 1000.0,
    "tiempoAntiFalsa": 10.0,
    "tiempoVidaAlerta": 60.0,
    "tipo": "Abierta"
  },
  "alerts_log": {
    "log_id_12345": {
      "id": "log_id_12345",
      "usuarioId": "UID_FIREBASE_ALFANUMERICO_1",
      "nombreUsuario": "Brandon Mendoza",
      "latitud": 21.1680,
      "longitud": -101.6730,
      "tipo": "Real",
      "timestamp": 1720893500000,
      "nivel": 3,
      "networkId": "net_9876xyz"
    }
  }
}
```

*Nota sobre Bloqueos:* Si el `estado` de un usuario en `usuarios/{uid}/estado` cambia a `"bloqueado"`, el sistema revoca instantáneamente su sesión activa de Firebase Auth en el `LoginViewModel` y en `SplashScreen`, forzando el cierre de sesión y mostrando una pantalla de acceso denegado.

*Nota sobre configuracion_global:* `tiempoVidaAlerta` define (en minutos) cuánto tiempo las alertas siguen activas antes de ocultarse automáticamente en Móviles y Smart TVs. `checkVida` define (en minutos) el tiempo exacto que espera el SmartWatch después de detonar un SOS antes de preguntar al usuario "¿ESTÁS BIEN?", esto es sincronizado instantáneamente al reloj por Bluetooth vía Wearable MessageClient (BLE).

*Nota sobre alerts_log:* Cada vez que el SmartWatch dispara una alerta, se crea un registro inmutable en `alerts_log` para auditoría y reportes históricos.

### 3.2 Eclipse Paho MQTT — Mensajería Push Ultrarrápida
Dado que la aplicación de Smart TV (`cunaseguratv`) está pensada para ejecutarse como un Dashboard *Always-On* en una sala de estar, dejar la conexión de Firebase abierta consumiendo recursos puede provocar cierres por OOM (Out Of Memory) en sistemas de televisión con bajas especificaciones. 

**Implementación técnica de MQTT:**
* La app importa `org.eclipse.paho:org.eclipse.paho.client.mqttv3:1.2.5`.
* Utilizamos un cliente `MqttAsyncClient` con persistencia en RAM (`MemoryPersistence()`).
* El Broker usado por defecto en este entorno de compilación es público, como `tcp://broker.hivemq.com:1883`, lo cual permite escalar millones de mensajes por segundo de forma gratuita durante las pruebas (en producción se apunta al puerto 8883 con cifrado SSL/TLS de un broker interno).
* Se generan dos tópicos vitales:
    1. **`cunasegura/alertas`**: Maneja el payload crítico de seguridad.
    2. **`cunasegura/tv/vinculacion`**: Tópico efímero usado durante la vinculación por QR o ID.

**Ejemplo de Serialización JSON generada por `MqttPublisher` (Kotlin Serialization):**
```json
{
  "usuarioId": 1,
  "nombreUsuario": "Vecina Karen",
  "latitud": 21.168523,
  "longitud": -101.673291,
  "nivelAlerta": 4,
  "estado": "activa",
  "timestamp": 1720984123456,
  "networkId": "net_9876xyz"
}
```
*Cuando la alerta finaliza o es reportada como falsa, se despacha el mismo payload pero con `"estado": "cancelada"`, apagando de inmediato la sirena visual de todas las Smart TVs de la red.*

### 3.3 Bluetooth Low Energy (BLE) y Wearable Data Layer
La comunicación entre la interfaz `app` y `cunasegurawear` se maneja mayoritariamente de forma asíncrona.
1. Cuando el usuario cambia la configuración en `WatchConfigScreen.kt` (Módulo App), el cambio se sube al nodo FRDB `configuracion_toques/{uid}`.
2. Si el reloj inteligente está conectado vía Bluetooth, los `DataClient` o el `MessageClient` nativo de Wear OS interceptan la sincronización.
3. El reloj almacena este JSON temporal y actualiza su base de datos local SQLite (Room) ubicada en `cunasegurawear/src/main/java/mx/edu/utng/cunasegurawear/data/local/AppDatabase.kt`.
4. En caso de pérdida total de internet, el reloj sabe exactamente qué hacer si recibe 4 toques, garantizando la activación a pesar del fallo de infraestructura.

---

## 4. Análisis Profundo de los Módulos Nativos

Cuna Segura no es una aplicación monolítica, sino un orquestador de tres proyectos Gradle entrelazados en un mismo repositorio, pero compilados de manera independiente.

### 4.1 Módulo Móvil (`:app`) — El Centro de Operaciones
Construido estrictamente con **Jetpack Compose Material 3**. Es el punto de anclaje de todos los usuarios.

**Jerarquía de Pantallas Principales:**
* **`SplashScreen`**: Revisa si existe sesión en Firebase Auth, valida si la sesión ha sido revocada y si el perfil está suspendido/bloqueado. Redirecciona al Login, al Home, o al Panel Global.
* **`LoginScreen` / `RegisterScreen`**: Procesos de autenticación manejados por `UsuarioRepositoryImpl`.
* **`MainUserScreen`**: Contenedor tipo `Scaffold` con una barra de navegación inferior (`NavigationBar`) que permite enrutar a tres pantallas cardinales.
    * **1. Dashboard (`MapScreen`)**: Implementa `com.google.maps.android.compose.GoogleMap`. Recupera alertas activas, traza un marcador propio en azul, y si existen alertas en su `networkId`, levanta un BottomSheet modal y marca a los vecinos en rojo.
    * **2. Red Vecinal (`NetworksScreen`)**: Consulta la base de datos de redes. Permite a los administradores generar un código QR (vía la librería Zxing) para que otros lo escaneen, o permite expulsar miembros. Adicionalmente, incluye el **Modal Interactivo de Edición de Nombre de Red** programado exclusivamente para quienes poseen el rol `esAdminDeRed == true`.
    * **3. Perfil de Usuario (`UserProfileScreen`)**: Interfaz de configuración que centraliza el estado del usuario, permitiendo acceder a los sub-paneles de Dispositivos Vinculados (Smartwatch y Smart TV).

### 4.2 Módulo Wear OS (`:cunasegurawear`) — El Disparador de Hardware
El módulo Wear OS prescinde de Material 3 y utiliza **Compose for Wear OS** para adaptar todos los componentes a pantallas redondas con bisel físico.

**Arquitectura de Hardware-Táctil:**
* Las clases de UI envuelven todo en un `Scaffold` circular.
* **Detector de Toques:** En la pantalla principal `WatchMainScreen.kt`, un `PointerInput` o un `Modifier.clickable(interactionSource)` adaptado evalúa los *timestamps* de cada toque, agrupándolos.
* **La Cuenta Regresiva (5 segundos):** Para combatir los toques erróneos. Al ejecutar una orden (ej. 3 toques), la pantalla se transforma en un temporizador rojo animado. El usuario tiene un botón masivo "CANCELAR" si fue un error.
* **Ambient Mode & Life Check:** Si la alerta avanza, el reloj entra en un modo Always-On. La función *Life Check* despliega Glassmorphism sobre la pantalla pidiendo al usuario confirmar "ESTOY BIEN". La falta de respuesta sirve como telemetría para que la app móvil sepa que la persona sigue bajo amenaza o ha perdido el conocimiento.

### 4.3 Módulo Smart TV (`:cunaseguratv`) — La Estación de Monitoreo
El módulo para televisión desecha por completo la interacción táctil (Touch). Está construido sobre el paquete `androidx.leanback:leanback` fusionado con los contenedores modernos de **Compose for TV**.

**Optimización D-Pad y Focus Management:**
* En Smart TV, no existen clics; existe el concepto de *Focus*. Las vistas como `Card`, `Button`, y cajas del mapa utilizan modificadores como `Modifier.focusable()` y `onFocusChanged`.
* Recientemente, los botones direccionales para explorar el mapa en `DashboardScreen.kt` se aislaron de su `Card` original, la cual devoraba los eventos de pulsación direccional del control remoto. Ahora operan bajo un `Box` no cliqueable, restaurando el barrido lateral fluido del mapa.
* La TV se vincula leyendo un PIN (ej: `123456`) que se despliega en pantalla; el usuario móvil lo ingresa en su `TvConfigScreen.kt` y lo lanza vía MQTT.

---

## 5. Mecanismos Anti-Falsas Alarmas y Protocolos de Seguridad

El principal fallo de los botones de pánico del mercado masivo es la saturación por falsos positivos (False Positives). Cuna Segura está programada para filtrar e inferir errores humanos.

### 5.1 Retardo de Buffer (Countdown Cancelable)
Ninguna alerta grave (Nivel 3 y 4) despacha el payload MQTT instantáneamente. Inician un coroutine delay() de 5000ms. En ese tiempo, el Wear OS despliega una UI agresiva en rojo brillante alertando al usuario de la inminencia del envío.

### 5.2 Life Check (Glassmorphism Interactivo)
Durante el ciclo de vida activo de un SOS, el reloj pregunta periódicamente mediante vibraciones del motor háptico (utilizando `Vibrator` Manager en Android) si el usuario se encuentra bien. Esta capa no bloquea la transmisión del GPS local, sino que es semi-transparente, usando las propiedades gráficas de Glassmorphism.

### 5.3 Gestión de Identidad y Tipos de Roles

Para mantener el control y la integridad de las vecindades (evitando abusos, bromas o infiltraciones de terceros no deseados), Cuna Segura implementa un modelo de autorización basado en tres niveles de privilegios (Roles). Cada usuario posee un nivel jerárquico que dicta las acciones permitidas en la interfaz de usuario y las reglas de escritura en Firebase.

#### 1. Usuario Normal (Estándar)
Es el rol base que se asigna a cualquier persona al registrarse en el sistema.
* **Privilegios de Acción:** Puede unirse a una red vecinal mediante el escaneo de un código QR. Una vez dentro de la red, tiene autorización para disparar alertas de pánico desde su Smartwatch, ver el mapa de la zona en su teléfono, y vincular su propia Smart TV para monitorear a los demás.
* **Restricciones:** Un usuario normal NO puede expulsar a otros vecinos de la red, ni puede modificar los datos estructurales de la vecindad (como el nombre de la red o su perímetro).

#### 2. Administrador de Red (Network Admin)
Es el líder o creador de una vecindad específica. Cuando un usuario normal toca "Crear Red", automáticamente es ascendido a Administrador de esa red en particular.
* **Privilegios de Acción:** Posee el control absoluto sobre su vecindad. En la pantalla de Red Vecinal, se habilita un panel exclusivo con un ícono de edición (✏️) que le permite modificar dinámicamente el **Nombre de la Red** (ej. de "Cuadra Sur" a "Vecinos Cuadra Sur Vigilada"). Además, este rol tiene la capacidad de **expulsar miembros** de la red de manera irrevocable; al hacerlo, se destruye el nodo de membresía en Firebase y la Smart TV del usuario expulsado dejará de recibir las alarmas de esa cuadra.

#### 3. Administrador Global (Superusuario)
Es la autoridad máxima de la aplicación, generalmente reservada para los desarrolladores, soporte técnico, o autoridades comunitarias de alto nivel.
* **Privilegios de Acción:** No se limita a una sola red, sino que monitorea el comportamiento de todos los usuarios registrados en `usuarios/{uid}`. Si el Administrador Global detecta que un usuario (sea normal o administrador de red) está haciendo mal uso del sistema (ej. provocando falsas alarmas repetidamente), tiene el poder de cambiar el estado del perfil a `"bloqueado"`.
* **Persistencia del Bloqueo (Anti-Tampering):** Este bloqueo es agresivo a nivel arquitectura. Si un usuario bloqueado intenta iniciar sesión (Login), el sistema lee el atributo de la base de datos y revoca la solicitud de Firebase Auth de inmediato. Si el usuario ya tenía la sesión abierta en caché, el componente `SplashScreen` interceptará el token al abrir la app, invalidándolo, forzando un cierre de sesión (`signOut`) y desplegando un mensaje de acceso denegado por políticas de la administración.

---

## 6. Estructura del Proyecto y Clean Architecture

El proyecto Android es altamente modular, permitiendo escalabilidad a largo plazo. Utiliza paquetes en inglés estandarizados en la industria y componentes segregados.

### 6.1 Árbol de Directorios Base

```text
CunaSegura/
├── app/ (El Hub Móvil Principal)
│   ├── build.gradle.kts (Gestión de Dependencias App)
│   └── src/main/java/mx/edu/utng/cunasegura/
│       ├── di/
│       │   └── AppModule.kt                 # Proveedores Hilt (Singleton Firebase, Auth, Room)
│       ├── data/                            # Capa de Datos Concreta
│       │   └── repository/
│       │       ├── NetworkRepositoryImpl.kt # Implementación de CRUD en FRDB
│       │       └── UsuarioRepositoryImpl.kt # Implementación de Autenticación
│       ├── domain/                          # Capa de Negocio Pura (Independiente de Frameworks)
│       │   ├── model/
│       │   │   ├── Network.kt
│       │   │   ├── Usuario.kt
│       │   │   └── Alerta.kt
│       │   └── repository/                  # Interfaces puente
│       │       ├── INetworkRepository.kt
│       │       └── IUsuarioRepository.kt
│       ├── mqtt/                            # Integración IoT de mensajería
│       │   └── MqttPublisher.kt             # Lógica publicadora asíncrona
│       └── presentation/                    # UI Compose y ViewModels
│           ├── navigation/
│           │   └── AppNavigation.kt         # NavHost, Rutas y DeepLinks
│           ├── components/                  # Widgets UI Reusables
│           ├── splash/
│           ├── login/
│           ├── main/
│           ├── map/
│           ├── networks/
│           ├── profile/
│           ├── watchconfig/
│           └── tvconfig/
│
├── cunaseguratv/ (Módulo de Televisión)
│   ├── build.gradle.kts (Gestión dependencias TV y Leanback)
│   └── src/main/java/mx/edu/utng/cunaseguratv/
│       ├── mqtt/
│       │   └── MqttTvSubscriber.kt          # Escucha activa de alertas
│       └── presentation/
│           ├── screens/
│           │   ├── DashboardScreen.kt       # Pantalla de mapas y alertas D-Pad
│           │   └── TvVinculacionScreen.kt   # Interfaz de códigos de emparejamiento
│           └── theme/
│
├── cunasegurawear/ (Módulo de Reloj Inteligente)
│   ├── build.gradle.kts (Gestión dependencias Wear OS)
│   └── src/main/java/mx/edu/utng/cunasegurawear/
│       ├── data/local/
│       │   ├── AppDatabase.kt               # Room SQLite Wrapper
│       │   └── ConfigDao.kt                 # Data Access Object de SQLite
│       └── presentation/
│           ├── theme/
│           ├── WatchMainScreen.kt           # Manejo de toques y eventos físicos
│           ├── LifeCheckScreen.kt           # Pantalla de verificación anti-fake
│           └── CountdownScreen.kt           # Animación de carga y cancelación
│
├── docs/ (Módulos de documentación adicionales)
├── evidencias/ (Recursos multimedia)
├── build.gradle.kts (Configuración Global del Proyecto)
└── settings.gradle.kts (Resolución de sub-proyectos)
```

### 6.2 Módulo `app` Detallado
En `app/src/main/java/mx/edu/utng/cunasegura`, hemos garantizado que ninguna vista (Compose) llame directamente a una función de Firebase. En su lugar, el evento en UI activa una función en un `ViewModel` (e.g., `NetworksViewModel.kt`), la cual emite estados sobre un `StateFlow`. El ViewModel inyecta el `INetworkRepository` provisto por Hilt, que delega a `NetworkRepositoryImpl.kt`, que es donde realmente se utiliza `db.getReference()`.

### 6.3 Inyección de Dependencias con Hilt
Para evitar que se generen fugas de memoria (memory leaks) por crear docenas de instancias de Firebase, el módulo `AppModule.kt` provee `@Singleton`:
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideFirebaseDatabase(): FirebaseDatabase = FirebaseDatabase.getInstance()
    
    // Provisión de repositorios que dependen de BD
}
```

---

## 7. Tecnologías, Dependencias y SDKs

Para compilar, orquestar y ejecutar Cuna Segura con éxito, se requiere de un marco moderno de herramientas que han sido definidas estrictamente en los ficheros `build.gradle.kts` (DSL de Kotlin).

### 7.1 Matriz Tecnológica Core

| Tecnología | Rol y Justificación Técnica | Submódulo(s) Afectados |
|---|---|---|
| **Kotlin 2.4.0** | Lenguaje primario. Elegido por su null-safety, tipado estático, y modernidad. Resuelve muchísimos NullPointerExceptions clásicos de Java. | Todos |
| **Jetpack Compose 1.x / M3** | Ecosistema declarativo. Reemplaza XML con funciones puras `@Composable`, re-renderizando partes mínimas del árbol de vistas según el estado. | Todos |
| **Compose for Wear OS** | Variación específica de UI. Provee Scaffolds circulares, curvas de progresión, listas curvas, y manejo de bisel giratorio. | `:cunasegurawear` |
| **Compose for TV** | Manejo especializado del Focus. Diseñado para manejar direccionalidad de controles remoto de televisión de manera óptima sin pantallas táctiles. | `:cunaseguratv` |
| **Dagger Hilt** | Inyección de Dependencias de Google. Automatiza la creación y provisión de repositorios. | `:app`, `:cunaseguratv` |
| **Google Maps Android SDK** | Capa topológica. Renderiza en 2D el mapa y los marcadores de vecinos, empleando la extensión de Maps para Compose (`com.google.maps.android:maps-compose`). | `:app`, `:cunaseguratv` |
| **Firebase Realtime Database** | Almacenamiento NoSQL JSON central. La sincronización se basa en listeners (WebSockets persistentes), permitiendo UI reactiva. | `:app`, `:cunaseguratv` |
| **Firebase Auth** | Manejo seguro de identidad. Cifra credenciales y maneja JWTs de sesión. | `:app` |
| **Eclipse Paho MQTT** | Capa IoT. Biblioteca MQTT v3.1 ligera y eficiente diseñada incialmente por IBM para telemetría. Usada para comunicación Wear->Phone->TV en nanosegundos. | `:app`, `:cunaseguratv` |
| **ZXing (Zebra Crossing)** | Generador de códigos QR para la vinculación rápida de redes vecinales sin tener que compartir URLs engorrosas. | `:app` |
| **Room Persistence Library** | ORM sobre SQLite para abstraer consultas SQL en clases de datos (`@Entity`, `@Dao`), utilizado como cache offline en el reloj. | `:cunasegurawear` |
| **Google Play Services Location** | FusedLocationProviderClient utilizado para obtener alta precisión (GPS+Wi-Fi+LTE triangulation) de lat/long del dispositivo en peligro. | `:app`, `:cunasegurawear` |

### 7.2 Manejo de Tareas Asíncronas (Coroutines & Flow)
Debido a las políticas de seguridad de Android (que impiden realizar tareas de red o base de datos en el Hilo Principal UI), toda la lógica que involucre Firebase, Room o MQTT está delegada a **Kotlin Coroutines**.

Se usa el despachador `Dispatchers.IO` para la red de Hilt, y los `ViewModelScope` para atar el ciclo de vida de la asincronía al de la pantalla. Adicionalmente, el estado se expone con `StateFlow`, logrando que la UI sea determinista y carente de efectos secundarios descontrolados.

---

## 8. Guía Maestra de Interfaz de Usuario (UI/UX)

La aplicación ha sido diseñada bajo estrictos parámetros de la teoría del color y de respuesta al pánico.

### 8.1 Psicología del Color para Emergencias
Una persona bajo amenaza sufre "visión de túnel" cognitiva. La aplicación evita sobrecargar de información.
* **Fondo Dominante (Superficie Oled):** Hex `#0F1416` (Gris Pizarra Ultra Oscuro). Además de mitigar la fatiga visual, en smartwatches OLED y teléfonos AMOLED los píxeles negros se apagan por completo, ahorrando hasta un 30% de batería.
* **Color Base Institucional:** Hex `#85D1E8` (Cian Acero). Denota calma, normalidad, configuraciones y el estatus de la red ("Estás Seguro"). Se emplea en AppBar y menús.
* **Color Crítico y de Acción:** Hex `#FFB4AB` (Rojo Salmón Claro). Aplicado exclusivamente cuando existe una anomalía, error, o estado activo de SOS. Al usar un tono pálido en lugar del clásico rojo primario `#FF0000`, reducimos la agresividad extrema visual de noche pero retenemos la alerta subconsciente.

### 8.2 Componentes Semánticos y Efectos Visuales
* **Tipografía:** Se emplea una escala tipográfica Bold, prescindiendo de cursivas. Las fuentes sin serif (Sans-Serif) grandes y con alto contraste (`color = Color.White`) sobre negro permiten lecturas ultra-rápidas a distancia (particularmente útil en la Smart TV a 3 metros).
* **Glassmorphism (Diseño Escarchado):** Empleado en menús flotantes, en `BottomSheet` del mapa, y en la pantalla de revisión de vida (Life Check) del reloj. Mediante modificadores de `blur` paramétrico, permite que el usuario lea un texto de confirmación sin perder la perspectiva de la aplicación (el mapa difuminado detrás), manteniendo así la orientación espacial.
* **Navegación Táctil Holgada:** Los márgenes (Paddings) de los iconos táctiles respetan la regla de 48dp (Touch Target Size de Material Design), garantizando que en una emergencia, los toques torpes no fallen.

### 8.3 Accesibilidad y Adaptabilidad
El sistema es sensible al tamaño del texto del usuario (SP settings) en Android. En el entorno de la Smart TV, la ausencia de "clicks" demanda que el **Foco (Focus)** emita un halo blanco o un crecimiento del botón activo, facilitando el recorrido con los cursores direccionales del control.

---

## 9. 🛠️ Guía de Desarrollo Paso a Paso

Esta sección está diseñada para que un desarrollador pueda replicar, extender o auditar el proyecto desde cero, siguiendo un flujo incremental y ordenado por capas de dependencia técnica.

---

### 9.1 Fase 1 — Configuración del Entorno de Desarrollo

**Paso 1.1 — Instalar Android Studio (JBR incluido)**
> Descarga **Android Studio Meerkat (2024.3.x)** o superior desde [developer.android.com/studio](https://developer.android.com/studio). La instalación incluye el JetBrains Runtime (JBR 21) con `jlink`, requerido por el compilador de Android SDK 36+.

**Paso 1.2 — Instalar los SDKs requeridos**
> Desde el **SDK Manager** (`File > Settings > SDK Manager`), descarga:
> - Android 14 (API 34) — Target principal del módulo `:app`
> - Android 13 (API 33) — Compatibilidad backward
> - Wear OS 4.0 (API 33/34) — Módulo `:cunasegurawear`
> - Android TV (API 34) — Módulo `:cunaseguratv`

**Paso 1.3 — Crear AVDs para pruebas locales**
> Desde el **AVD Manager** (`Device Manager`), crea tres emuladores:
> 1. **Pixel 7** o superior → para probar el módulo `:app`
> 2. **Wear OS Large Round (API 34)** → para probar `:cunasegurawear`
> 3. **Android TV 1080p (API 34)** → para probar `:cunaseguratv`

```bash
# Verificar que ADB reconoce los emuladores o dispositivos físicos:
adb devices
```

---

### 9.2 Fase 2 — Aprovisionamiento de Servicios Cloud

**Paso 2.1 — Firebase Console**

1. Accede a [console.firebase.google.com](https://console.firebase.google.com) y crea un proyecto (ejemplo: `CunaSegura-Prod`).
2. Habilita los siguientes servicios desde la consola:
   - **Authentication** → proveedor `Email/Password`
   - **Realtime Database** → en modo `bloqueado`, después aplica las reglas de `database.rules.json` del repositorio
3. Registra **dos apps Android** en el mismo proyecto Firebase:
   - Paquete `:app` → `mx.edu.utng.cunasegura`
   - Paquete `:cunaseguratv` → `mx.edu.utng.cunaseguratv`
4. Descarga el archivo `google-services.json` para cada app y colócalos en:
   - `./app/google-services.json`
   - `./cunaseguratv/google-services.json`

**Paso 2.2 — Google Cloud Console (Maps API)**

1. Accede a [console.cloud.google.com](https://console.cloud.google.com) en el mismo proyecto vinculado a Firebase.
2. Habilita las siguientes APIs:
   - **Maps SDK for Android**
   - **Geocoding API**
3. En `Credentials > Create Credentials > API Key`, crea una clave y restringe su uso al paquete `mx.edu.utng.cunasegura` (SHA-1 del keystore de debug).

**Paso 2.3 — HiveMQ Cloud (Broker MQTT)**

1. Registra una cuenta gratuita en [hivemq.com/mqtt-cloud-broker](https://www.hivemq.com/mqtt-cloud-broker/).
2. Crea un clúster y anota el `Cluster URL`, el `Username` y el `Password` del panel de control.
3. En producción, usar el puerto `8883` con TLS. Para pruebas locales, el broker público `tcp://broker.hivemq.com:1883` también funciona sin autenticación.

---

### 9.3 Fase 3 — Clonación y Configuración del Repositorio

**Paso 3.1 — Clonar el repositorio**

```bash
git clone https://github.com/<org>/CunaSegura.git
cd CunaSegura
```

**Paso 3.2 — Configurar `local.properties`**

Crea o edita el archivo `local.properties` en la raíz del proyecto. Este archivo **no debe commitearse** (está en `.gitignore`). Agrega las credenciales obtenidas en la Fase 2:

```properties
# Ruta del Android SDK (ajustar según sistema operativo)
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk

# Google Maps
MAPS_API_KEY=AIzaSy_TU_LLAVE_REAL_AQUI

# HiveMQ MQTT Broker
hivemq.brokerUrl=ssl://TU_CLUSTER.s2.eu.hivemq.cloud:8883
hivemq.username=TU_USUARIO_HIVEMQ
hivemq.password=TU_CONTRASENA_HIVEMQ
```

> ⚠️ **Nota crítica:** El `build.gradle.kts` de `:app` y `:cunaseguratv` inyecta estas propiedades como `BuildConfig` fields en tiempo de compilación mediante `buildConfigField(...)`. Si alguna clave falta, `assembleDebug` fallará con un error de `BuildConfig`.

**Paso 3.3 — Abrir el proyecto en Android Studio**

1. En Android Studio: `File > Open` → selecciona la carpeta raíz `CunaSegura/`.
2. Espera a que Gradle sincronice todas las dependencias (puede tardar varios minutos la primera vez).
3. Verifica que no haya errores de sincronización en el panel `Build`.

---

### 9.4 Fase 4 — Compilación y Generación de APKs

**Paso 4.1 — Compilar el ecosistema completo (3 módulos en paralelo)**

```bash
# Desde la raíz del proyecto (PowerShell o terminal)
.\gradlew assembleDebug --parallel
```

Los APKs se generarán automáticamente en las siguientes rutas:

| Módulo | APK de salida |
|---|---|
| `:app` | `app/build/outputs/apk/debug/app-debug.apk` |
| `:cunasegurawear` | `cunasegurawear/build/outputs/apk/debug/cunasegurawear-debug.apk` |
| `:cunaseguratv` | `cunaseguratv/build/outputs/apk/debug/cunaseguratv-debug.apk` |

**Paso 4.2 — Limpiar caché si hay errores de compilación**

```bash
.\gradlew clean
# Luego volver a compilar:
.\gradlew assembleDebug --parallel
```

---

### 9.5 Fase 5 — Despliegue en Dispositivos y Emuladores

**Paso 5.1 — Instalar directamente vía Gradle (dispositivo activo o emulador)**

```bash
# Módulo móvil (teléfono o emulador Pixel)
.\gradlew :app:installDebug

# Módulo TV (emulador Android TV o TV física con ADB habilitado)
.\gradlew :cunaseguratv:installDebug

# Módulo Wear OS (reloj o emulador Wear OS emparejado)
.\gradlew :cunasegurawear:installDebug
```

**Paso 5.2 — Instalar manualmente vía ADB (para dispositivos sin emulador)**

```bash
# Habilitar ADB inalámbrico en el dispositivo (Android 11+):
# Ajustes > Opciones de desarrollador > Depuración inalámbrica > Parear con código QR

adb connect <IP_DEL_DISPOSITIVO>:5555
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

**Paso 5.3 — Emparejar el Smartwatch con el teléfono (para Wear OS)**

> El reloj y el teléfono deben estar **en la misma cuenta de Google** y emparejados a través de la app **Google Pixel Watch** o **Wear OS companion**. Después de instalar `:cunasegurawear`, la app aparecerá automáticamente en el reloj.

---

### 9.6 Fase 6 — Verificación Funcional End-to-End

Una vez instalados los tres módulos, ejecuta el siguiente flujo de prueba para confirmar que el ecosistema funciona correctamente:

| # | Acción | Resultado Esperado |
|---|---|---|
| 1 | Registrar usuario en `:app` con email/password | Usuario creado en Firebase Auth y en FRDB `usuarios/{uid}` |
| 2 | Crear red vecinal desde `:app` | Nodo `networks/{netId}` creado; usuario asignado como admin |
| 3 | Vincular Smart TV desde `:app` (pantalla TV Config) | TV muestra PIN → ingresar en app → MQTT confirma vinculación |
| 4 | Desde `:cunasegurawear`, dar 3 toques físicos | Countdown de 5s en reloj → alerta enviada vía MQTT |
| 5 | Confirmar recepción en `:cunaseguratv` | Dashboard de TV muestra mapa con marcador rojo en coords del reloj |
| 6 | Cancelar alarma desde `:app` o reloj | TV regresa a estado normal; `estado: "cancelada"` en MQTT |

---

## 10. Guía de Configuración, Compilación y Despliegue (Referencia Rápida)

La clonación y ejecución exitosa de Cuna Segura requiere una configuración ambiental de infraestructura de Android de nivel intermedio-avanzado.

### 10.1 Requisitos del Entorno de Desarrollo
* **IDE (Entorno de Desarrollo Integrado):** Android Studio Meerkat (2024.3.x) o más reciente. Evita las versiones Canary, quédate con canales estables (Stable).
* **Java Development Kit:** JDK 17 (incorporado usualmente con Android Studio JBR).
* **Android SDK:** Deben descargarse las plataformas API Level 30, 31, 33 y 34 desde el SDK Manager.
* **SDK Tools:** Obligatoriamente Wear OS System Images, y un AVD (Android Virtual Device) configurado como un Smartwatch circular de 384x384. Un AVD para Android TV (1080p).

### 10.2 Aprovisionamiento de Claves de API (Google y HiveMQ)
Antes de presionar "Run" o construir el APK, debes poseer las siguientes claves.
1. Accede a **Google Cloud Console**, crea un proyecto y habilita **Maps SDK for Android** y **Geocoding API**. Crea una clave de API (API Key).
2. Genera una instancia en Firebase Console, asocia el paquete `mx.edu.utng.cunasegura` y descarga el archivo `google-services.json`. Reemplázalo en las carpetas `/app` y `/cunaseguratv`.
3. Crea un archivo en la raíz de tu proyecto llamado `local.properties`. Agrega las siguientes líneas reemplazando los valores por tus credenciales de Google y tu entorno del broker MQTT público o privado que dispongas:
```properties
# Archivo local.properties
sdk.dir=C\:\\Users\\tuusuario\\AppData\\Local\\Android\\Sdk
MAPS_API_KEY=AIzaSy_TU_LLAVE_DE_GOOGLE_MAPS_GENERADA_1234
HIVEMQ_USER=admin_cuna_segura
HIVEMQ_PASS=pass_seguro_123
```
*Si estas claves no están presentes, el proceso `assembleDebug` fracasará, ya que Gradle buscará inyectarlas como BuildConfig variables y strings en el manifiesto.*

### 10.3 Instrucciones de Gradle por Línea de Comandos

La construcción de múltiples aplicaciones nativas al mismo tiempo puede hacerse a través de la interfaz de usuario del IDE, pero los arquitectos prefieren emplear Gradle Wrapper (Terminal).

**Limpieza del Caché:**
```bash
.\gradlew clean --daemon
```

**Compilar Todo el Ecosistema Simultáneamente:**
```bash
.\gradlew assembleDebug --parallel
```
*Esto generará simultáneamente `app-debug.apk`, `cunaseguratv-debug.apk` y `cunasegurawear-debug.apk` en sus carpetas respectivas (app/build/outputs/apk/debug...)*

**Instalación Rápida Específica:**
Para instalar el APK en un dispositivo físico con adb habilitado o en un emulador actualmente activo:
```bash
# Para Móvil
.\gradlew :app:installDebug

# Para Televisión
.\gradlew :cunaseguratv:installDebug

# Para Reloj Inteligente (Asegúrate de que está conectado por ADB a través de Wi-Fi o Bluetooth)
.\gradlew :cunasegurawear:installDebug
```

---

## 11. Casos de Uso y Flujos de Pantalla (User Journeys)

### 11.1 Flujo: Creación y Administración de Red
1. El usuario `A` se loggea mediante Firebase Auth e ingresa a `MainUserScreen`. 
2. Da clic en el icono "Red" de la barra inferior y elige "Crear Red".
3. Ingresa nombre (ej. "Cuadra Sur"), el tipo (Abierta/Cerrada), y su cobertura en metros. El FusedLocationClient captura la geolocalización central.
4. El sistema almacena en FRDB `networks/XYZ_ID`. El usuario `A` es asignado como Administrador.
5. `A` comparte el código QR (generado por Zxing en su pantalla). 
6. El usuario `B` escanea el QR. Queda inscrito en `networks/XYZ_ID/miembros`.
7. `A` como administrador (posee rol `esAdminDeRed = true`), puede editar el nombre de la red tocando el ícono ✏️ o expulsar a `B` de la lista de miembros de forma irrevocable.

### 11.2 Flujo: Disparo de Alarma P2P
1. El usuario `C` sufre una emergencia, golpea su reloj 3 veces (`cunasegurawear`).
2. Se procesan 5 segundos de advertencia y se obtiene el GPS (LocationManager del smartwatch).
3. Se invoca MQTT publicando JSON (Lat, Lng, Alerta) en `cunasegura/alertas`.
4. El reloj del usuario `C` enrojece sus bordes e inicia "Life Check".
5. Simultáneamente, el celular `C`, los celulares de los demás vecinos, y las Smart TVs (vía `cunaseguratv` consumiendo el `MqttTvSubscriber`) estallan en sirenas rojas. El mapa centraliza la cámara sobre la coordenada de `C`, trazando rutas de apoyo visuales.
6. Si resulta ser una falsa alarma, el usuario `C` o el administrador pueden detenerla. Un nuevo MQTT con `estado="cancelada"` se emite apagando instantáneamente los dashboards globales.

---

## 12. Glosario de Términos

* **ADB (Android Debug Bridge):** Herramienta que permite conectar e instalar las aplicaciones compiladas del repositorio directamente a Smart TVs físicas, celulares o relojes a través de Wi-Fi o USB.
* **Glassmorphism:** Estilo de diseño visual (UI) que simula paneles de vidrio escarchado, desenfocando (blurring) el fondo para lograr jerarquía óptica en componentes flotantes.
* **BLE (Bluetooth Low Energy):** Protocolo inalámbrico de consumo mínimo, utilizado para sincronizar la configuración de toques (`Room` DB) entre móvil y smartwatch de manera local, ahorrando decenas de horas de autonomía.
* **Room:** Librería Jetpack nativa (ORM) que simplifica la interacción de SQLite para el cache de toques en el wearable, sin la necesidad de escribir comandos SQL rudimentarios y propensos a errores (`SELECT`, `INSERT`, `DROP`).
* **KAPT / KSP (Kotlin Symbol Processing):** Procesadores de compilación que transforman las anotaciones `@Entity` de Room y `@HiltAndroidApp` en código compilable Java subyacente. Puede ser inestable, por lo cual se forzó la resolución estricta del parser en la construcción Gradle.
* **FRDB:** Firebase Realtime Database. La base del árbol de estado, gestionado directamente por Google Cloud.
* **MQTT:** Protocolo ligero de mensajería M2M (Máquina a Máquina), optimizado para bajo ancho de banda, responsable en la app de activar las alarmas instantáneas en los módulos de TV.
* **D-Pad:** Cruz direccional del control remoto, vital para el *Focus Management* de Compose for TV.

---

## 13. Anexos (Documentación Externa y Visual)

Dado lo extensivo de la interfaz gráfica y de las directrices visuales adoptadas, proveemos documentos complementarios aislados por plataforma listados al inicio de este documento.

---
> 💡 *Para cualquier duda técnica con el despliegue, la configuración o la extensión del código fuente, por favor dirígete a los desarrolladores a través de Pull Requests en el repositorio.*
> 
> *Este documento cuenta con un conteo masivo e intensivo de especificaciones ingenieriles con la finalidad de ofrecer la base de conocimientos más extensa posible para futuros desarrolladores o inversores del sistema **Cuna Segura** (Build 3.0.0).*
> 
> 📍 Desarrollado por GIDS6092, Generación 2026. Todos los derechos reservados bajo las políticas open-source del autor y la institución de orígen.
