import com.android.aaptcompiler.resolvePackage

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.flyng.${project.name}"
version = "0.1.1"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

kotlin {
    js(IR) {
        binaries.executable()
        browser()
    }
    android()
    sourceSets {
        @Suppress("UNUSED_VARIABLE")
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.jetbrains.kotlin.reflect)
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val commonTest by getting {
            dependencies {
                implementation(libs.jetbrains.kotlin.test)
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val jsMain by getting {
            dependencies {
                // compose
                implementation(compose.web.core)
                implementation(compose.runtime)

                // filament
                implementation(npm("filament", "=${libs.versions.filament.get()}"))
                implementation(npm("path-browserify", "=1.0.1"))
                implementation(npm("fs-extra", "=11.1.0"))
                implementation(npm("crypto-browserify", "=3.12.0"))
                implementation(npm("stream-browserify", "=3.0.0"))
                implementation(npm("buffer", "=6.0.3"))
                implementation(npm("util", "=0.12.5"))
                implementation(npm("assert", "=2.0.0"))
                implementation(npm("constants-browserify", "=1.0.0"))

                // gl-matrix
                implementation(npm("gl-matrix", "=2.8.1"))
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val jsTest by getting

        @Suppress("UNUSED_VARIABLE")
        val androidMain by getting {
            dependencies {
                // androidx
                implementation(libs.androidx.core.ktx)
                // Integration with activities
                implementation(libs.androidx.activity.compose)
                // Integration with ViewModels
                implementation(libs.androidx.viewmodel.compose)

                // Material Design 3 and 2
                implementation(libs.androidx.compose.material3)
                implementation(libs.androidx.compose.material)
                // Preview support
                implementation(libs.androidx.compose.ui.preview)
                // Integration with livedata
                implementation(libs.androidx.compose.runtime.livedata)
                // Full set of material icons
                implementation(libs.androidx.compose.material.icons)

                // filament
                implementation(libs.google.filament.android)

                // coil
                implementation(libs.coil.svg)
                implementation(libs.coil.compose)
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val androidTest by getting {
            dependencies {
                dependsOn(sourceSets["androidAndroidTestRelease"])
                dependsOn(sourceSets["androidTestFixtures"])
                dependsOn(sourceSets["androidTestFixturesDebug"])
                dependsOn(sourceSets["androidTestFixturesRelease"])
            }
        }
    }
}

@Suppress("UnstableApiUsage")
android {
    compileSdk = (findProperty("android.compileSdk") as String).toInt()
    sourceSets["main"].apply {
        manifest.srcFile("src/androidMain/AndroidManifest.xml")
        kotlin.srcDirs("src/androidMain/kotlin")
        res.srcDirs("src/androidMain/res")
    }
    sourceSets["test"].apply {
        kotlin.srcDirs("src/androidTest/kotlin")
        res.srcDirs("src/androidTest/resources")
    }

    defaultConfig {
        applicationId = project.group as String
        minSdk = (findProperty("android.minSdk") as String).toInt()
        targetSdk = (findProperty("android.targetSdk") as String).toInt()
        versionCode = (project.version as String).filter { it != '.' }.toInt()
        versionName = project.version as String
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    signingConfigs {
        register("release") {
            storeFile = file("${rootDir}/src/androidMain/release.keystore")
            storePassword = "1475963Kalculus#"
            keyAlias = "release-key"
            keyPassword = "1475963Kalculus#"
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            signingConfig = signingConfigs["release"]
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
        }
        getByName("debug") {
            isJniDebuggable = true
        }
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.compose.compiler.get()
    }

    androidResources {
        noCompress("filamat", "ktx")
    }

    namespace = project.group as String
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks["jsBrowserDevelopmentRun"].mustRunAfter(tasks["jsDevelopmentExecutableCompileSync"])
tasks["jsBrowserProductionRun"].mustRunAfter(tasks["jsProductionExecutableCompileSync"])