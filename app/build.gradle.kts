import org.gradle.internal.declarativedsl.parsing.main

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    // 루트에서 apply false 한 KSP를 모듈에서 활성화
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
        versionCode = 3
        versionName = "1.2"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "MDM_API_KEY", "\"c4Bz60gRwz\"")

        // 레거시와 동일한 ABI 필터
        ndk {
            abiFilters += listOf("armeabi-v7a", "arm64-v8a")
        }

        // 레거시 defaultConfig 의 cmake 인자
        externalNativeBuild {
            cmake {
                // 네온 사용
                arguments += listOf(
                    "-DANDROID_ARM_NEON=TRUE"
                )
            }
        }

        buildConfigField("String", "API_URL", "\"https://k50o0i0a90.execute-api.ap-northeast-2.amazonaws.com/prod/\"")
        buildConfigField("String", "API_KEY", "\"shtQsXianY2bELEiqxKIB7u9ZtfekTT287EC4jxJ\"")
    }


    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    splits {
        abi {
            isEnable =  false
        }
    }



    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "dev"

            buildConfigField("String", "PRESIGN_PATH", "\"display-report-presign-dev\"")
        }

        create("prod") {
            dimension = "environment"


            buildConfigField("String", "PRESIGN_PATH", "\"display-report-presign-dev\"")

//            buildConfigField("String", "PRESIGN_PATH", "\"display-report-presign-prod\"")
        }
    }

    buildTypes {

        debug {
            isDebuggable = true

            externalNativeBuild {
                cmake {
                    // 레거시: -DCMAKE_BUILD_TYPE=Debug
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
                    // 레거시: -DCMAKE_BUILD_TYPE=Release
                    arguments += listOf(
                        "-DCMAKE_BUILD_TYPE=Release"
                    )
                }
            }
        }
    }

    flavorDimensions += "environment"


    externalNativeBuild {
        cmake {
            // 레거시랑 동일 경로/버전
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

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
    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // logger
    implementation(libs.orhanobut.logger)

    // rtsp
    implementation(libs.rtsp.client.android)

    // mdm AAR (중첩 dependencies 블록 제거)
    implementation(files("libs/lib-hmdm.aar"))

    // Room (KSP만 사용, kapt 제거)
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Koin
    implementation(libs.koin.core)
    implementation(libs.koin.android)
    implementation(libs.koin.androidx.workmanager)

    // Retrofit/OkHttp
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)



    

    // local modules
    implementation(project(":libuvccamera"))
    implementation(project(":sdk"))


    //reflect
    implementation(libs.kotlin.reflect)


    implementation(libs.ted.permission)


}