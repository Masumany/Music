plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.module_mvplayer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.module_mvplayer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        viewBinding = true
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    // 协程库
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    // Retrofit
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    // Retrofit Gson Converter
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    // Retrofit RxJava3 Adapter
    implementation ("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
    // RxJava
    implementation ("io.reactivex.rxjava3:rxjava:3.1.0")
    // RxAndroid
    implementation ("io.reactivex.rxjava3:rxandroid:3.0.2")
    // Glide核心库
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    // Glide注解处理器
    kapt ("com.github.bumptech.glide:compiler:4.12.0")
    // 内存泄漏检测
    debugImplementation ("com.squareup.leakcanary:leakcanary-android:2.8.1")
    implementation ("androidx.viewpager2:viewpager2:1.1.0")
    implementation ("com.google.android.material:material:1.8.0")
    // ExoPlayer
    implementation 'com.google.android.exoplayer:exoplayer-core:2.18.7'
    implementation 'com.google.android.exoplayer:exoplayer-ui:2.18.7'
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}