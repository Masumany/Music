plugins {
    alias(libs.plugins.android.application)  // 使用版本目录中的定义
    alias(libs.plugins.kotlin.android)

}
apply (plugin= "therouter")

android {
    namespace = "com.example.music"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.music"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures{
        viewBinding=true
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
    implementation(project(":module_login_register"))
    implementation(project(":module_personage"))
    implementation(project(":module_mvplayer"))
    // OkHttp 核心库，用于进行网络请求
    implementation ("com.squareup.okhttp3:okhttp:4.11.0")
    // 日志拦截器，用于在开发过程中打印请求和响应的详细信息，方便调试，可选添加
    implementation ("com.squareup.okhttp3:logging-interceptor:4.11.0")
    // 协程库
    implementation ("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}
