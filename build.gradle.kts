// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version libs.versions.agp.get() apply false
    id("org.jetbrains.kotlin.android") version libs.versions.kotlin.get() apply false
    id("cn.therouter.agp8")version "1.2.2" apply false
}

// 根目录 build.gradle
//buildscript {
//    dependencies {
//        classpath (libs.gradle)
//    }
//}
// build.gradle.kts（项目级）

allprojects {
    configurations.all {
        resolutionStrategy {
            // 强制统一版本
            force("org.jetbrains.kotlin:kotlin-stdlib:1.9.20")
            force("androidx.core:core-ktx:1.12.0")
        }
    }
}