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
            repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
            repositories {
                google()
                mavenCentral()
            }
        }

        rootProject.name = "Music"
        include(":app")
        include(":lib_base")
        include(":module_musicplayer")
        include(":module_recommened")
        include(":module_details")
        include(":module_hot")
        include(":module_login")
        include(":module_mvplayer")
        include(":module_personage")
        include(":module_search")
    }
}

