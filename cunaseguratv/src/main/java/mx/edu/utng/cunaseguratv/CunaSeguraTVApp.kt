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

