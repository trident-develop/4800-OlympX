import org.gradle.kotlin.dsl.implementation
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.lsparanoid)
}

lsparanoid {
    seed = 62
    classFilter = { it.startsWith("com.sofish.soeasymahjong") }
    includeDependencies = false
    variantFilter = { true }
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.orbit.viewmodel)
            implementation(libs.orbit.compose)
            implementation(libs.arrow.core)
            implementation(libs.arrow.fx.coroutines)
            implementation(libs.okhttp)
            implementation(libs.installreferrer)
            implementation(libs.datastore.preferences)
            implementation(libs.androidx.fragment.ktx)
            implementation("io.ktor:ktor-client-okhttp:3.1.3")
            implementation(libs.androidx.splashscreen)
            implementation(libs.play.services.ads)
            implementation(libs.firebase.messaging)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.analytics)
            implementation(libs.firebase.crashlytics)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.core.ktx)
            implementation(libs.androidx.camera.core)
            implementation(libs.androidx.camera.camera2)
            implementation(libs.androidx.camera.lifecycle)
            implementation(libs.androidx.camera.view)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.uiBackhandler)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation("io.ktor:ktor-client-core:3.1.3")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        iosMain.dependencies {
            implementation("io.ktor:ktor-client-darwin:3.1.3")
        }
    }
}

android {
    namespace = "com.sofish.soeasymahjong"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.sofish.soeasymahjong"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 3
        versionName = "1.2"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

afterEvaluate {
    tasks.named("uploadCrashlyticsMappingFileRelease")
        .configure { enabled = false }
}

afterEvaluate {
    tasks.named("bundleRelease").configure {
        finalizedBy("removeProguardMap")
    }
}

tasks.register("removeProguardMap") {
    notCompatibleWithConfigurationCache("Uses ZipFile/ZipOutputStream from script scope")
    doLast {
        val generatedAabPath = "${projectDir}/release"
        val aabFile = file("${generatedAabPath}/composeApp-release.aab")

        val zipFile = file("${generatedAabPath}/composeApp-release.zip")
        val savedProguardMapFile = file("${generatedAabPath}/proguard.map")
        val tempZipFilePath = file("${generatedAabPath}/composeApp-release-temp.zip")
        val targetFilePath = "BUNDLE-METADATA/com.android.tools.build.obfuscation/proguard.map"

        aabFile.renameTo(zipFile)

        val zf = ZipFile(zipFile)
        val zos = ZipOutputStream(tempZipFilePath.outputStream())
        try {
            val entries = zf.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement() as ZipEntry
                if (entry.name != targetFilePath) {
                    zos.putNextEntry(ZipEntry(entry.name))
                    zf.getInputStream(entry).use { it.copyTo(zos) }
                    zos.closeEntry()
                } else {
                    zf.getInputStream(entry).use { input ->
                        savedProguardMapFile.outputStream().use { input.copyTo(it) }
                    }
                }
            }
        } finally {
            zos.close()
            zf.close()
        }

        zipFile.delete()
        tempZipFilePath.renameTo(aabFile)
    }
}