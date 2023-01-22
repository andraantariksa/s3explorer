import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
//    kotlin("plugin.serialization") version "1.7.20"
    id("org.jetbrains.compose")
//    id("com.android.library")
}

group = "com.gadostudio"
version = "1.0-SNAPSHOT"

kotlin {
//    android()
    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "11"
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material)

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.6.4")
                implementation("aws.sdk.kotlin:s3:0.18.0-beta")
                implementation("cafe.adriel.voyager:voyager-navigator:1.0.0-rc03")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
//        val androidMain by getting {
//            dependencies {
//                api("androidx.appcompat:appcompat:1.6.0")
//                api("androidx.core:core-ktx:1.9.0")
//            }
//        }
//        val androidTest by getting {
//            dependencies {
//                implementation("junit:junit:4.13.2")
//            }
//        }
        val desktopMain by getting {
            dependencies {
                api(compose.preview)
            }
        }
        val desktopTest by getting
    }
}

//android {
//    compileSdk = 33
//    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
//    defaultConfig {
//        minSdk = 24
//        targetSdkVersion(33)
//    }
//    compileOptions {
//        sourceCompatibility = JavaVersion.VERSION_1_8
//        targetCompatibility = JavaVersion.VERSION_1_8
//    }
//}