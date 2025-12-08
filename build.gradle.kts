plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    id("com.google.devtools.ksp") version "2.2.10-2.0.2" apply false
}

allprojects {
    repositories {
        google()
        mavenCentral()

        maven {
            url = uri("https://jitpack.io")
        }

        maven {
            url = uri("https://raw.github.com/saki4510t/libcommon/master/repository/")
        }

        maven {
            url = uri("https://maven.pkg.github.com/p2achAI/p2ach-vision-sdk/")
            credentials {
                val gprUser = findProperty("gpr.user") as String?
                    ?: System.getenv("GPR_USER")
                    ?: System.getenv("GITHUB_ACTOR")
                val gprToken = findProperty("gpr.token") as String?
                    ?: System.getenv("GPR_TOKEN")
                    ?: System.getenv("GITHUB_TOKEN")

                username = gprUser
                password = gprToken
            }
        }

        flatDir {
            dirs("libs")
        }
    }

    configurations.all {
        resolutionStrategy.eachDependency {
            if (requested.group == "org.jetbrains.kotlin") {
                useVersion("2.2.0")
                because("Align Kotlin compiler & stdlib with AGP 8.13.0 / databinding-ktx 8.13.0")
            }
        }
    }
}