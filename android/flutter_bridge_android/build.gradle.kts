plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
//    id("dev.flutter.flutter-gradle-plugin")
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

//flutter {
//    source = "../.."
//}

dependencies {

    implementation(project(":flutter_bridge_core"))

//    implementation("androidx.core:core-ktx:1.16.0")
//    implementation(platform("org.jetbrains.kotlin:kotlin-bom:1.8.0"))
//    implementation("androidx.appcompat:appcompat:1.7.1")
//    implementation("com.google.android.material:material:1.12.0")
//    testImplementation("junit:junit:4.13.2")
//    androidTestImplementation("androidx.test.ext:junit:1.2.1")
//    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
//    compileOnly(project(":flutter"))
}