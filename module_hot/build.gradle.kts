plugins {
    id("com.android.library")
//    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.module_hot"
    compileSdk = 35
    buildFeatures {
        viewBinding = true
    }

    defaultConfig {
//        applicationId = "com.example.module_hot"
        minSdk = 24
        targetSdk = 34
//        versionCode = 1
//        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_17  // 升级到 Java 17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"  // 升级到 Java 17
    }
}

dependencies {

    implementation(project(":lib_base"))
    kapt("cn.therouter:apt:1.2.2")
    implementation("cn.therouter:router:1.2.2")
    implementation ("androidx.viewpager2:viewpager2:1.1.0")
    implementation ("com.google.android.material:material:1.8.0")
    // OkHttp 核心库，用于进行网络请求
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    // 日志拦截器，用于在开发过程中打印请求和响应的详细信息，方便调试，可选添加
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")
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
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-common-java8:2.7.0")
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    // 内存泄漏检测
    debugImplementation ("com.squareup.leakcanary:leakcanary-android:2.8.1")
    // 替换 legacy.support.v4
    implementation("androidx.legacy:legacy-support-v4:1.0.0")  // 或使用更具体的 androidx 组件
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}