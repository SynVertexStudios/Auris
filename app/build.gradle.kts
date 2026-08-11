plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.ksp)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dagger.hilt.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.baselineprofile)
    id("kotlin-parcelize")
    id("kotlin-kapt")          // necessário para outras libs (Room não, pq usa ksp, mas deixe)
    id("com.google.gms.google-services")
}

val enableAbiSplits = providers.gradleProperty("Auris.enableAbiSplits")
    .orElse("true")
    .map(String::toBoolean)
    .get()

val enableComposeCompilerReports = providers.gradleProperty("Auris.enableComposeCompilerReports")
    .orElse("false")
    .map(String::toBoolean)
    .get()

// 🔥 NOVA VARIÁVEL ADICIONADA AQUI
val appVersionCode = (project.findProperty("APP_VERSION_CODE") as String).toInt()

android {
    namespace = "com.goldensystem.auris"
    compileSdk = 35

    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }

    sourceSets {
        getByName("androidTest") {
            assets.srcDir("$projectDir/schemas")
        }
    }

    androidResources {
        noCompress.add("tflite")
    }

    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
            excludes += "/META-INF/io.netty.versions.properties"
            pickFirsts.add("META-INF/LICENSE.md")
            pickFirsts.add("META-INF/LICENSE.txt")
            excludes.add("META-INF/CONTRIBUTORS.md")
            excludes.add("META-INF/NOTICE.txt")
            excludes.add("META-INF/NOTICE.md")
        }
    }

    defaultConfig {
        applicationId = "com.goldensystem.auris"
        minSdk = 29
        targetSdk = 35
        // 🔥 TROCADO PARA USAR A VARIÁVEL
        versionCode = appVersionCode
        versionName = project.findProperty("APP_VERSION_NAME") as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // 🔥 CONFIGURAÇÃO DE DEBUG AGORA USA O MESMO KEYSTORE DO RELEASE EM CI
        create("debug") {
            if (System.getenv("CI") == "true") {
                // No CI (GitHub Actions), usa release keystore
                storeFile = file("my-release-key.keystore")
                storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "Saymonsil098"
                keyAlias = System.getenv("KEY_ALIAS") ?: "auris"
                keyPassword = System.getenv("KEY_PASSWORD") ?: "Saymonsil098"
            } else {
                // Localmente, usa debug keystore padrão
                storeFile = file("debug.keystore")
                storePassword = "android"
                keyAlias = "androiddebugkey"
                keyPassword = "android"
            }
        }
        
        create("release") {
            storeFile = file("my-release-key.keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: "Saymonsil098"
            keyAlias = System.getenv("KEY_ALIAS") ?: "auris"
            keyPassword = System.getenv("KEY_PASSWORD") ?: "Saymonsil098"
        }
        
        // Mantido para compatibilidade (opcional)
        create("fixedDebug") {
            storeFile = file("debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".beta"
            // 🔥 TROCADO PARA INCLUIR O VERSION CODE
            versionNameSuffix = ".$appVersionCode-beta"
            // 🔥 AGORA USA A ASSINATURA DE DEBUG (que em CI usa release keystore)
            signingConfig = signingConfigs.getByName("debug")
        }
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
        create("benchmark") {
            initWith(getByName("release"))
            signingConfig = signingConfigs.getByName("debug")
            matchingFallbacks += listOf("release")
            isDebuggable = false
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "2.1.0"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    kotlinOptions {
        jvmTarget = "11"
        if (enableComposeCompilerReports) {
            freeCompilerArgs += listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=${project.layout.buildDirectory.get().asFile.absolutePath}/compose_compiler_reports"
            )
            freeCompilerArgs += listOf(
                "-P",
                "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=${project.layout.buildDirectory.get().asFile.absolutePath}/compose_compiler_metrics"
            )
        }
        freeCompilerArgs += listOf(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:stabilityConfigurationPath=${project.rootDir.absolutePath}/app/compose_stability.conf"
        )
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.all {
            it.useJUnitPlatform()
        }
    }

    splits {
        abi {
            isEnable = enableAbiSplits
            reset()
            if (enableAbiSplits) {
                include("arm64-v8a", "armeabi-v7a")
                isUniversalApk = true
            }
        }
    }

    bundle {
        abi {
            enableSplit = true
        }
        density {
            enableSplit = true
        }
        language {
            enableSplit = true
        }
    }
}

composeCompiler {
    enableStrongSkippingMode = true
    featureFlags = setOf(
        org.jetbrains.kotlin.compose.compiler.gradle.ComposeFeatureFlag.OptimizeNonSkippingGroups
    )
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.generateKotlin", "true")
}

kapt {
    correctErrorTypes = true
}

dependencies {

    //google firebase
    implementation(platform("com.google.firebase:firebase-bom:34.15.0"))
    implementation("com.google.firebase:firebase-analytics")
    
    //implementatinos
    implementation(libs.androidx.profileinstaller)
    implementation("com.google.zxing:core:3.5.3")
    implementation(libs.androidx.paging.common)
    baselineProfile(project(":baselineprofile"))
    coreLibraryDesugaring(libs.desugar.jdk.libs)

    implementation(libs.androidx.core.ktx)
    implementation("io.coil-kt:coil-video:2.6.0")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation("androidx.lifecycle:lifecycle-process:2.9.0")
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.generativeai)
    implementation(libs.androidx.navigation.runtime.ktx)
    testImplementation(libs.junit.jupiter.api)
    testImplementation(libs.junit.jupiter.params)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.junit)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.vintage.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.4")
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.truth)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.junit)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.kotlin.test.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.truth)
    androidTestImplementation(libs.mockk)
    androidTestImplementation("androidx.work:work-testing:2.10.1")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(platform(libs.androidx.compose.bom))
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    androidTestImplementation(libs.androidx.benchmark.macro.junit4)
    androidTestImplementation(libs.androidx.uiautomator)

    // ==================== HILT (USANDO KSP, NÃO KAPT) ====================
    implementation(libs.hilt.android)
    ksp(libs.hilt.android.compiler)          // <-- trocado de kapt para ksp
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)          // <-- trocado de kapt para ksp
    // ====================================================================

    // Room (já usa ksp, ok)
    implementation(libs.androidx.room.runtime)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.paging)

    // Paging 3
    implementation(libs.androidx.paging.runtime)
    implementation(libs.androidx.paging.compose)

    // Glance
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // Gson
    implementation(libs.gson)

    // Serialization
    implementation(libs.kotlinx.serialization.json)

    // Work
    implementation(libs.androidx.work.runtime.ktx)

    // Smooth corners shape
    implementation(libs.smooth.corner.rect.android.compose)
    implementation(libs.androidx.graphics.shapes)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Animations
    implementation(libs.androidx.animation)

    // Coil
    implementation(libs.coil.compose)

    // Capturable
    implementation(libs.capturable) {
        exclude(group = "androidx.compose.animation")
        exclude(group = "androidx.compose.foundation")
        exclude(group = "androidx.compose.material")
        exclude(group = "androidx.compose.runtime")
        exclude(group = "androidx.compose.ui")
    }

    // Reorderable List/Drag and Drop
    implementation(libs.reorderables)

    // CodeView
    implementation(libs.codeview)

    // AppCompat
    implementation(libs.androidx.appcompat)

    // Media3 ExoPlayer
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.mediarouter)
    implementation(libs.google.play.services.cast.framework)
    implementation(libs.androidx.media3.exoplayer.ffmpeg)

    // Palette API
    implementation(libs.androidx.palette.ktx)

    // Core Splashscreen
    implementation(libs.androidx.core.splashscreen)

    // ConstraintLayout
    implementation(libs.androidx.constraintlayout.compose)

    // Foundation
    implementation(libs.androidx.foundation)

    // Wavy slider
    implementation(libs.wavy.slider)

    // Icons
    implementation(libs.androidx.material.icons.core)
    implementation(libs.androidx.material.icons.extended)

    // Material library
    implementation(libs.material)

    // Kotlin Collections
    implementation(libs.kotlinx.collections.immutable)

    // Permissions
    implementation(libs.accompanist.permissions)

    // Audio editing
    implementation(libs.androidx.media3.transformer)

    // Checker framework
    implementation(libs.checker.qual)

    // Timber
    implementation(libs.timber)

    // TagLib
    implementation(libs.taglib)
    implementation(libs.jaudiotagger)
    implementation(libs.vorbisjava.core)

    // Retrofit & OkHttp
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor)

    // Ktor for HTTP Server
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.cio)
    implementation("io.ktor:ktor-server-cio:2.3.7")
    implementation(libs.kotlinx.coroutines.core)
    implementation("io.ktor:ktor-server-core:2.3.7")
    implementation("io.ktor:ktor-server-websockets:2.3.7")
    implementation("io.ktor:ktor-server-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.ktor:ktor-server-partial-content:2.3.7")
    implementation("io.ktor:ktor-server-compression:2.3.7")

    implementation(libs.androidx.ui.text.google.fonts)

    implementation(libs.accompanist.drawablepainter)
    implementation(kotlin("test"))

    // Android Auto
    implementation(libs.androidx.media)
    implementation(libs.androidx.app)
    implementation(libs.androidx.app.projected)

    // Wear OS Data Layer
    implementation(project(":shared"))
    implementation(libs.play.services.wearable)
    implementation(libs.kotlinx.coroutines.play.services)

    // Telegram TDLib
    implementation(libs.tdlib)

    // Google Sign-In
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Kuromoji
    implementation(libs.kuromoji.ipadic)

    // Pinyin
    implementation(libs.pinyin4j.core)

    // Encrypted credentials storage
    implementation(libs.androidx.security.crypto)

    // roku
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
   // Para DataStore com JSON (kotlinx.serialization)
    implementation("androidx.datastore:datastore:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}