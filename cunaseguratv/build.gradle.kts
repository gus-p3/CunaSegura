import java.util.Properties

// cunaseguratv/build.gradle.kts
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