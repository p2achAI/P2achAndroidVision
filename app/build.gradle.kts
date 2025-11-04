plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)



}

android {
    namespace = "ai.p2ach.p2achandroidlibrary"
    compileSdk {
        version = release(36)
    }

    buildFeatures{
        viewBinding  =true
        dataBinding = true
    }



    defaultConfig {
        applicationId = "ai.p2ach.p2achandroidlibrary"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"


    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
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

dependencies {


    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.orhanobut.logger)

    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    implementation(project(":commonLibrary"))
    implementation(project(":libuvccamera"))


}