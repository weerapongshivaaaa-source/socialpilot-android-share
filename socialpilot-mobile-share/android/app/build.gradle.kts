plugins { id("com.android.application"); id("org.jetbrains.kotlin.android") }

android {
    namespace = "ai.socialpilot.share"
    compileSdk = 36

    defaultConfig {
        applicationId = "ai.socialpilot.share"
        minSdk = 26
        targetSdk = 36
        versionCode = 4
        versionName = "1.3.0"
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}
