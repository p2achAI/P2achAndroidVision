pluginManagement {
    repositories {
        gradlePluginPortal()
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven { url =uri("https://raw.githubusercontent.com/saki4510t/libcommon/master/repository/" )}
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
        maven { url = uri("https://raw.githubusercontent.com/saki4510t/libcommon/master/repository/") }
    }
}

rootProject.name = "P2achAndroidLibrary"
include(":app")
include(":commonLibrary")
include(":libuvccamera")
project(":libuvccamera").projectDir = file("libuvccamera")

include(":sdk")
project(":sdk").projectDir = file("sdk")
