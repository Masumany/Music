plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {

    namespace = "com.example.lib_base"
    compileSdk = 35
    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    }
}

dependencies {
    implementation ("com.squareup.retrofit2:retrofit:2.9.0")
    // Retrofit Gson Converter
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    // Retrofit RxJava3 Adapter
    implementation ("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
    // RxJava
    implementation ("io.reactivex.rxjava3:rxjava:3.1.0")
    // RxAndroid
    implementation ("io.reactivex.rxjava3:rxandroid:3.0.2")
    // OkHttp 核心库，用于进行网络请求
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    // 日志拦截器，用于在开发过程中打印请求和响应的详细信息，方便调试，可选添加
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")
    // 协程库
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}