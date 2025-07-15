import com.android.build.gradle.LibraryExtension
import com.android.build.gradle.BasePlugin
import com.android.build.gradle.tasks.ProcessLibraryManifest

buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.0")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

val newBuildDir: Directory = rootProject.layout.buildDirectory.dir("../../build").get()
rootProject.layout.buildDirectory.value(newBuildDir)

subprojects {
    val newSubprojectBuildDir: Directory = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
}
subprojects {
    val newSubprojectBuildDir = newBuildDir.dir(project.name)
    project.layout.buildDirectory.value(newSubprojectBuildDir)
    project.evaluationDependsOn(":app")
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

// 2) **Strip** any `package="…"` attribute from library manifests
subprojects {
    // this task type runs before merging the library manifest
    tasks.withType(ProcessLibraryManifest::class.java).configureEach {
        doFirst {
            val manifest = project.file("src/main/AndroidManifest.xml")
            if (manifest.exists()) {
                val fixed = manifest.readText()
                    // remove any package="…" attribute on the <manifest> tag
                    .replace(Regex("""<manifest\b([^>]*?)\bpackage="[^"]+"([^>]*?)>"""), "<manifest\$1\$2>")
                manifest.writeText(fixed)
            }
        }
    }
}

tasks.register<Delete>("clean") {
    delete(rootProject.layout.buildDirectory)
}
