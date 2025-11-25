plugins {
    id("com.android.application")
    id("kotlin-android")
    id("dev.flutter.flutter-gradle-plugin")
}

android {
    namespace = "org.e4h.asset"
    compileSdk = 35
    ndkVersion = "27.0.12077973"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }

    defaultConfig {
        applicationId = "org.e4h.asset"
        minSdk = flutter.minSdkVersion
        targetSdk = flutter.targetSdkVersion
        versionCode = flutter.versionCode()
        versionName = flutter.versionName()
        manifestPlaceholders += mapOf(
            "applicationName" to "android.app.Application"
        )
        manifestPlaceholders["ANDROID_API_KEY"] =
            (project.findProperty("ANDROID_API_KEY") as String?) ?: "PLACEHOLDER"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    packagingOptions {
        jniLibs {
            useLegacyPackaging = true
        }
        resources {
            excludes += setOf(
                "**/AndroidManifest.xml",
                "META-INF/*.kotlin_module"
            )
        }
    }
}

flutter {
    source = "../.."
}
