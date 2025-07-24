plugins {
    id("com.android.library")
    //alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt")
}

android {
    namespace = "com.example.module_musicplayer"
    compileSdk = 35

    defaultConfig {
        // applicationId = "com.example.module_musicplayer"
        minSdk = 24
        targetSdk = 35
        //versionCode = 1
        //  versionName = "1.0"
        dependencies {
            kapt("org.jetbrains.kotlin:kotlin-annotation-processing:1.9.20")
        }
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding =true
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
    // 核心 Lifecycle 库（包含 ViewModel）
    implementation ("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    // 可选：如果需要使用 LiveData
    implementation( "androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
    // 可选：ViewModel 与 Activity/Fragment 集成
    implementation ("androidx.lifecycle:lifecycle-viewmodel-savedstate:2.6.2")
    implementation(project(":module_details"))
    implementation ("org.greenrobot:eventbus:3.3.1")
    kapt("cn.therouter:apt:1.2.2")
    implementation("cn.therouter:router:1.2.2")
    implementation(project(":lib_base"))
    implementation(project(":module_recommened"))
    implementation ("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("com.squareup.retrofit2:adapter-rxjava:2.1.0")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation ("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation ("com.squareup.retrofit2:adapter-rxjava3:2.9.0")
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}