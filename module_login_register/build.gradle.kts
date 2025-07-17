plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.module_login_register"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.module_login_register"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

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
    kapt ("com.github.bumptech.glide:compiler:4.12.0")
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-common-java8:2.7.0")
    // 内存泄漏检测
    debugImplementation ("com.squareup.leakcanary:leakcanary-android:2.8.1")
    // 兼容库
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.fragment.ktx)
    // 导航组件
    val nav_version = "2.7.7"
    implementation("androidx.navigation:navigation-fragment-ktx:$nav_version")
    implementation("androidx.navigation:navigation-ui-ktx:$nav_version")
    // 动态特性模块支持
    implementation("androidx.navigation:navigation-dynamic-features-fragment:$nav_version")
    implementation("com.google.android.play:feature-delivery:2.1.0") // 替换有问题的 2.0.1 版本，避免 Play Console 拒绝
    // 基础组件
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    // 测试依赖
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
