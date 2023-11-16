import com.google.protobuf.gradle.id

@Suppress("DSL_SCOPE_VIOLATION") // TODO: Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.com.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.protobuf)
    alias(libs.plugins.androidx.baselineprofile)
}

android {
    namespace = "com.programmersbox.testing"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.programmersbox.testing"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }

        create("beta") {
            initWith(getByName("debug"))
            matchingFallbacks.add("debug")
            isDebuggable = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)
    implementation(libs.navigation.compose)
    implementation(libs.composeMaterialIconsExtended)
    implementation(libs.coil.compose)
    implementation(libs.androidx.palette)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.viewModelCompose)
    implementation(libs.bundles.ktor)
    implementation(libs.bundles.landscapist)
    implementation(libs.profileinstaller)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    "baselineProfile"(project(":baselineprofiletest"))
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
    implementation(libs.bundles.roomLibs)
    annotationProcessor(libs.roomAnnotationProcessor)
    ksp(libs.roomCompiler)
    implementation(libs.bundles.pagingLibs)
    implementation(libs.pagingCompose)
    implementation(libs.radarny)
    implementation(libs.permissions)
    implementation(libs.androidx.datastore)
    implementation(libs.bundles.protobuf)

    implementation(libs.androidx.legacy.support.v4)

    // For media playback using ExoPlayer
    implementation(libs.androidx.media3.exoplayer)
    // For DASH playback support with ExoPlayer
    implementation(libs.androidx.media3.exoplayer.dash)
    // For HLS playback support with ExoPlayer
    implementation(libs.androidx.media3.exoplayer.hls)
    // For RTSP playback support with ExoPlayer
    implementation(libs.androidx.media3.exoplayer.rtsp)
    // For ad insertion using the Interactive Media Ads SDK with ExoPlayer
    implementation(libs.androidx.media3.exoplayer.ima)

    // For loading data using the Cronet network stack
    implementation(libs.androidx.media3.datasource.cronet)
    // For loading data using the OkHttp network stack
    implementation(libs.androidx.media3.datasource.okhttp)
    // For loading data using librtmp
    implementation(libs.androidx.media3.datasource.rtmp)
    // For exposing and controlling media sessions
    implementation(libs.androidx.media3.session)
    // For building media playback UIs
    implementation(libs.androidx.media3.ui)

    implementation(projects.dynamiccodeloading)
    implementation(projects.extensionLoader)

    implementation(libs.androidx.activity.ktx)

    implementation("androidx.compose.material3:material3-adaptive:1.0.0-alpha01")
}

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:3.23.0" }
    plugins {
        id("javalite") { artifact = libs.protobufJava.get().toString() }
        id("kotlinlite") { artifact = libs.protobufKotlin.get().toString() }
    }
    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("java") { option("lite") }
                create("kotlin") { option("lite") }
            }
        }
    }
}
