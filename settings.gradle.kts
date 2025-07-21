pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // 正确的阿里云镜像地址（必须带协议和完整路径）
//        maven { url = uri("https://maven.aliyun.com/repository/google/") } // 末尾带 /
//        maven { url = uri("https://maven.aliyun.com/repository/central/") }
//        maven { url = uri("https://maven.aliyun.com/repository/gradle-plugin/") }
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") } // Therouter需要的JitPack仓库
    }
    versionCatalogs {
        create("libs") {
            version("agp", "8.6.1")  // 指定兼容的 AGP 版本
            version("kotlin", "1.9.20")

            plugin("android-application", "com.android.application")
                .versionRef("agp")
            plugin("android-library", "com.android.library")
                .versionRef("agp")
            plugin("kotlin-android", "org.jetbrains.kotlin.android")
                .versionRef("kotlin")
        }
    }
}
rootProject.name = "Music"
include(":app")
include(":libs")
include(":lib_base")
include(":module_login_register")
include(":module_personage")
include(":module_mvplayer")
include(":module_hot")
