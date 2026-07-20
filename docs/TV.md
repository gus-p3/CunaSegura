# 📺 Módulo TV (Smart TV Dashboard)

Diseñado específicamente para centros de monitoreo, casetas de vigilancia o simplemente como una central de visualización en pantallas grandes (1080p y 4K).

## Características Principales
- **Vista Cinemática (Dashboard)**: Interfaz estructurada para ser legible a la distancia con colores contrastantes y responsividad total mediante Leanback y Jetpack Compose for TV.
- **Panel Lateral Dinámico**: Ocupa el 40% de la pantalla para mostrar las alertas recientes y estados críticos. Completamente colapsable y animado para expandir el mapa al 100%.
- **Mapas de OSMDroid Optimizados**: El mapa interactivo ha sido calibrado con ciclos de vida y User-Agents específicos para Android TV, permitiendo visualizar los incidentes al instante sin cortes de red.
- **Alertas Claras**: Tarjetas grises/azules (`surfaceVariant`) que resaltan contra el fondo profundo para evitar confusiones y no perder de vista los eventos de la base de datos (MQTT/Firebase).
- **Control remoto nativo**: Todos los elementos soportan navegación mediante D-Pad.

## Interfaz
![Módulo TV](../assets/tv_app_mockup.png)

## Ejecución
Para compilar y ejecutar este módulo desde Android Studio:
1. Cambia la configuración de ejecución a **`cunaseguratv`**.
2. Selecciona un emulador de Android TV (1080p).
3. Presiona **▶ Run**.
