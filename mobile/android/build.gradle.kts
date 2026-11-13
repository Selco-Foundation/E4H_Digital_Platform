import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.tasks.ProcessLibraryManifest

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.9.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }

    // Keep plugin native binaries compatible with Android's 16 KB pages.
    // Preserve the existing Dart camera/scanner APIs and the offline ML model.
    configurations.configureEach {
        resolutionStrategy.eachDependency {
            if (requested.group == "androidx.camera") {
                useVersion("1.4.2")
                because("Use one CameraX family with 16 KB-compatible native libraries")
            }
            if (requested.group == "com.google.mlkit" && requested.name == "barcode-scanning") {
                useVersion("17.3.0")
                because("Bundled barcode model must support 16 KB memory pages")
            }
        }
    }
}

val newBuildDir: Directory = rootProject.layout.buildDirectory.dir("../../build").get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
    if (path != ":app") {
        project.evaluationDependsOn(":app")
    }
}

subprojects {
    plugins.withType(BasePlugin::class.java) {
        extensions.findByType(LibraryExtension::class.java)?.apply {
            if (namespace.isNullOrBlank()) {
                namespace = "com.example.${project.name}"
            }
        }
    }
}

subprojects {
    tasks.withType(ProcessLibraryManifest::class.java).configureEach {
        doFirst {
            val manifest = project.file("src/main/AndroidManifest.xml")
            if (manifest.exists()) {
                val fixed = manifest.readText()
                    .replace(Regex("""<manifest\b([^>]*?)\bpackage="[^"]+"([^>]*?)>"""), "<manifest\$1\$2>")
                manifest.writeText(fixed)
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
