@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.compose)
    alias(libs.plugins.kotlin.serialization)
}

group = "com.flyng.${project.name}"
version = "0.0.0"

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
            }
        }

        @Suppress("UNUSED_VARIABLE")
        val jsTest by getting

        @Suppress("UNUSED_VARIABLE")
        val androidMain by getting {
            dependencies {
                // androidx
                implementation(libs.bundles.androidx)

                // filament
                implementation(libs.google.filament.android)
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
        versionCode = 1
        versionName = project.version as String
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
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
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks["jsBrowserDevelopmentRun"].mustRunAfter(tasks["jsDevelopmentExecutableCompileSync"])
tasks["jsBrowserProductionRun"].mustRunAfter(tasks["jsProductionExecutableCompileSync"])