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

    defaultConfig {
        applicationId = "ai.p2ach.p2achandroidvision"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "MDM_API_KEY", "\"c4Bz60gRwz\"")
    }

    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "dev"
            buildConfigField("String", "CONFIG_SVR_URL", "\"https://admin.dev.p2ach.io/\"")
            buildConfigField("String", "API_URL", "\"https://4j75oomboa.execute-api.ap-northeast-2.amazonaws.com/dev/\"")
            buildConfigField("String", "API_KEY", "\"CKQGF74aKl5Y8ArgPUpwF6Wt8fZvk3fM6ouAvGiU\"")
            buildConfigField("String", "PROD_API_URL", "\"https://k50o0i0a90.execute-api.ap-northeast-2.amazonaws.com/prod/\"")
            buildConfigField("String", "PROD_API_KEY", "\"shtQsXianY2bELEiqxKIB7u9ZtfekTT287EC4jxJ\"")
        }
        create("prod") {
            dimension = "environment"
            buildConfigField("String", "CONFIG_SVR_URL", "\"https://prod.p2ach.io/\"")
            buildConfigField("String", "API_URL", "\"https://k50o0i0a90.execute-api.ap-northeast-2.amazonaws.com/prod/\"")
            buildConfigField("String", "API_KEY", "\"shtQsXianY2bELEiqxKIB7u9ZtfekTT287EC4jxJ\"")
            buildConfigField("String", "PROD_API_URL", "\"https://k50o0i0a90.execute-api.ap-northeast-2.amazonaws.com/prod/\"")
            buildConfigField("String", "PROD_API_KEY", "\"shtQsXianY2bELEiqxKIB7u9ZtfekTT287EC4jxJ\"")
        }
    }

    buildTypes {

        debug {
            isDebuggable = true
        }

        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    flavorDimensions("environment")

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

// Room 스키마 경로(KSP 방식)
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
    implementation(project(":commonLibrary"))
    implementation(project(":libuvccamera"))

    //reflect

    implementation(libs.kotlin.reflect)
}