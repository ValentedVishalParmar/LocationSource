plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.gms)

}

android {
    namespace = "com.libertyinfosace.locationsource"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.libertyinfosace.locationsource"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
        vectorDrawables {
            useSupportLibrary = true
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isDebuggable = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures{
        dataBinding = true
        buildConfig = true
        viewBinding = true
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    // KSP configuration
    ksp {
        arg("enabled", "true") // Pass KSP argument
    }
}

dependencies {

    // CORE
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.constraintlayout)

    // GOOGLE MAP PLACES & GOOGLE MAP
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)
    implementation(libs.android.maps.utils)
    implementation(libs.maps.utils.ktx)
    implementation(libs.places)

    //FIREBASE
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)

    //GLIDE
    implementation(libs.glide)

    // VIEW MODEL, LIFECYCLE & LIVE DATA
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.common.java8)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.coroutines.test)

    // KSP for Room (instead of kapt)
    implementation(libs.room.persistance)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.ksp.symbol.processing.api)
    ksp(libs.room.compiler)

    // TESTING
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}