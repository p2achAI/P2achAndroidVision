plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp")
}

android {
    namespace = "ai.p2ach.p2achandroidvision"

    compileSdk {
        version = release(36)
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
        dataBinding = true
    }

    sourceSets {
        getByName("main") {
            assets.srcDir("$buildDir/generated_assets")
        }
    }

    defaultConfig {
        applicationId = "ai.p2ach.p2achandroidvision"
        minSdk = 28
        targetSdk = 36
        versionCode = 4
        versionName = "1.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "MDM_API_KEY", "\"c4Bz60gRwz\"")

        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }

        externalNativeBuild {
            cmake {
                arguments += listOf(
                    "-DANDROID_ARM_NEON=TRUE"
                )
            }
        }

        buildConfigField("String", "API_URL", "\"https://k50o0i0a90.execute-api.ap-northeast-2.amazonaws.com/prod/\"")
        buildConfigField("String", "API_KEY", "\"shtQsXianY2bELEiqxKIB7u9ZtfekTT287EC4jxJ\"")
    }

    splits {
        abi {
            isEnable = false
        }
    }

    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "dev"

            buildConfigField("String", "PRESIGN_DISPLAY_REPORT", "\"display-report-presign-dev\"")
            buildConfigField("String", "PRESIGN_URL_GENERATOR", "\"presigned_url_generator\"")
        }

        create("prod") {
            dimension = "environment"

            buildConfigField("String", "PRESIGN_DISPLAY_REPORT", "\"display-report-presign-prod\"")
            buildConfigField("String", "PRESIGN_URL_GENERATOR", "\"presigned_url_generator\"")
        }
    }

    buildTypes {
        debug {
            isDebuggable = true

            externalNativeBuild {
                cmake {
                    arguments += listOf(
                        "-DCMAKE_BUILD_TYPE=Debug"
                    )
                }
            }
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            externalNativeBuild {
                cmake {
                    arguments += listOf(
                        "-DCMAKE_BUILD_TYPE=Release"
                    )
                }
            }
        }
    }

    flavorDimensions += "environment"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.orhanobut.logger)
    implementation(libs.rtsp.client.android)

    implementation(files("libs/lib-hmdm.aar"))

    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.workmanager)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.kotlin.reflect)
    implementation(libs.ted.permission)

    implementation(files("libs/libuvccamera-release.aar"))
    implementation("com.serenegiant:common:2.12.4") {
        exclude(module = "support-v4")
        exclude(group = "com.android.support", module = "support-compat")
    }
    implementation(libs.opencv)

    implementation(libs.p2achVisionSdk)
}