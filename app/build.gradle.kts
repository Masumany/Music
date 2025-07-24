plugins {
    alias(libs.plugins.android.application)  // 使用版本目录中的定义
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    debugImplementation ("com.squareup.leakcanary:leakcanary-android:2.12")
    // Glide 核心库
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // 注解处理器（用于 Glide 的注解功能，如 @GlideModule）
    kapt("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("pl.droidsonroids.gif:android-gif-drawable:1.2.19")
    implementation ("org.greenrobot:eventbus:3.3.1")
    implementation ("com.google.android.material:material:1.0.0")
    implementation (project(":module_hot"))
    implementation (project(":module_personage"))
    implementation(project(":lib_base"))
    implementation(project(":module_details"))
    kapt("cn.therouter:apt:1.2.2")
    implementation("cn.therouter:router:1.2.2")
    implementation(project(":module_recommened"))
    implementation(project(":module_musicplayer"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}
