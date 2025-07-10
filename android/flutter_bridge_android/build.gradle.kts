import java.nio.file.Paths
import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.reader(Charsets.UTF_8).use { reader ->
            load(reader)
        }
    }
}
val flutterRoot: String = localProperties.getProperty("flutter.sdk")
val engineVersion = Paths.get(flutterRoot, "bin", "internal", "engine.version")
    .toFile().readText().trim()

val storageUrl = System.getenv("FLUTTER_STORAGE_BASE_URL") ?: "https://storage.googleapis.com"

var engineRealm = Paths.get(flutterRoot, "bin", "internal", "engine.realm")
    .toFile().readText().trim()
if (engineRealm.isNotEmpty()) {
    engineRealm = "$engineRealm/"
}

android {
    namespace = "com.galaxy.flutter.bridge"
    compileSdk = 34

    defaultConfig {
        minSdk = 19

        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

repositories {
    maven {
        url = uri("$storageUrl/${engineRealm}download.flutter.io")
    }
}

dependencies {
    // flutter_embedding.jar，参考 flutter sdk 'resolve_dependencies.gradle'
    compileOnly("io.flutter:flutter_embedding_debug:1.0.0-$engineVersion")

    implementation(project(":flutter_bridge_core"))

//    implementation("androidx.core:core-ktx:1.16.0")
//    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
//    implementation("androidx.appcompat:appcompat:1.7.1")
//    implementation("com.google.android.material:material:1.12.0")
}