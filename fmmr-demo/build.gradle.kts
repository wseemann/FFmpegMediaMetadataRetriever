plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "wseemann.media.demo"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        // applicationId = "wseemann.media.demo"
        applicationId = "wseemann.media.fmpdemo"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        ndkVersion = libs.versions.ndk.get()
        versionCode = libs.versions.versionCode.get().toInt()
        versionName = libs.versions.versionName.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(17)
        }
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-core:1.0.21")
    implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-native:1.0.21")
    //implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-native-armeabi-v7a:1.0.21")
    //implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-native-x86:1.0.21")
    //implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-native-x86_64:1.0.21")
    //implementation("com.github.wseemann:FFmpegMediaMetadataRetriever-native-arm64-v8a:1.0.21")

    //implementation(project(":native"))
    //implementation(project(":core"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}