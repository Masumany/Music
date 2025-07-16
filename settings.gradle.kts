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
    // 仓库配置 - 必须直接放在dependencyResolutionManagement下
    repositories {
        // 字节跳动国内镜像（优先下载，加速TheRouter依赖）
        maven { url = uri("https://mirror.bytedance.com/repository/public/") }
        // 谷歌官方仓库
        google()
        // 中央仓库
        mavenCentral()
    }

    // 版本目录管理（仅管理版本和插件，不包含仓库）
    versionCatalogs {
        create("libs") {
            version("agp", "8.6.1")  // Android Gradle Plugin版本
            version("kotlin", "1.9.20")  // Kotlin版本

            // 插件配置
            plugin("android-application", "com.android.application")
                .versionRef("agp")
            plugin("android-library", "com.android.library")
                .versionRef("agp")
            plugin("kotlin-android", "org.jetbrains.kotlin.android")
                .versionRef("kotlin")
        }
    }

    // 仓库模式：禁止项目级别的仓库配置，统一由这里管理
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
}

// 项目模块配置
rootProject.name = "Music"
include(":app")
include(":lib_base")
include(":module_recommened")
include(":module_musicplayer")