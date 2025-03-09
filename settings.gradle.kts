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
        maven { setUrl("https://jitpack.io/") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven(
            url = "/Users/dingpeihua/AndroidStudioProjects/Flutter_Module/build/host/outputs/repo"
        )
        maven(url = "https://storage.googleapis.com/download.flutter.io")
        maven { setUrl("https://jitpack.io/") }
    }
}

rootProject.name = "ComposeDemo"
include(":app")
include(":flutter_android")
