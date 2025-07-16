// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version libs.versions.agp.get() apply false
    id("org.jetbrains.kotlin.android") version libs.versions.kotlin.get() apply false
    alias(libs.plugins.android.library) apply false
}
// 根目录 build.gradle
buildscript {
    dependencies {
        classpath (libs.gradle)
    }
}