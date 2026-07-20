package mx.edu.utng.cunaseguratv

import android.app.Application
import java.io.File

class CunaSeguraTVApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val osmConfig = org.osmdroid.config.Configuration.getInstance()
        osmConfig.userAgentValue = packageName
        
        val basePath = File(cacheDir, "osmdroid")
        osmConfig.osmdroidBasePath = basePath
        osmConfig.osmdroidTileCache = File(basePath, "tiles")
    }
}
