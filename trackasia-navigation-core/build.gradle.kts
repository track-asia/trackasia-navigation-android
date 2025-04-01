import org.jetbrains.kotlin.gradle.plugin.mpp.apple.XCFrameworkConfig

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.cocoapods)
    alias(libs.plugins.kotlinx.serialization)
    alias(libs.plugins.kotlin.dokka)
}

apply {
    from(file("${rootDir}/gradle/artifact-settings.gradle"))
    from(file("${rootDir}/gradle/publish-kmp.gradle"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

kotlin {
    jvmToolchain(17)

    androidTarget {
        publishLibraryVariants("release")
    }

    val xcf = XCFrameworkConfig(project)
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "trackasia-navigation-core"
            xcf.add(this)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.trackasia.geojson)
            api(libs.trackasia.geojson.turf)

            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.coroutines)
            implementation(libs.kermit)
        }

        // `commonTest` can not be used here, because `mockk` is only valid for JVM targets
        androidUnitTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.mockk)

            // For Android specific tests
            implementation(libs.robolectric)
        }

        androidMain.dependencies {
            implementation(libs.trackasia)

            // Location by Play Services
            // Will only used, when added by the client app
            compileOnly(libs.play.services.location)
        }
    }

    cocoapods {
        version = project.properties.get("versionName") as String? ?: "0.0.0"
        summary = "TrackAsia navigation core library"
        homepage = "https://github.com/trackasia/trackasia-navigation-android/"

        name = "TrackAsiaNavigationCore"

        framework {
            baseName = "TrackAsiaNavigationCore"
            isStatic = false

            transitiveExport = false
            export(libs.trackasia.geojson)
            export(libs.trackasia.geojson.turf)
        }
    }
}

android {
    namespace = "com.trackasia.navigation.core"

    defaultConfig {
        compileSdk = 34
        minSdk = 21

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "TRACKASIA_NAVIGATION_SDK_IDENTIFIER",
            String.format("\"%s\"", "trackasia-navigation-android")
        )
        buildConfigField(
            "String",
            "TRACKASIA_NAVIGATION_VERSION_NAME", String.format("\"%s\"", project.properties.get("versionName"))
        )
        buildConfigField(
            "String",
            "TRACKASIA_NAVIGATION_EVENTS_USER_AGENT", String.format(
                "\"trackasia-navigation-android/%s\"",
                project.properties.get("versionName")
            )
        )

        consumerProguardFiles("proguard-consumer.pro")
    }

    buildFeatures {
        buildConfig = true
    }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}

// Exclude old version of GeoJSON libs
// At the moment a newer version - that supports Kotlin Multiplatform - is required to run navigation
configurations {
    configureEach {
        exclude(group = "io.github.track-asia", module = "android-sdk-geojson")
        exclude(group = "io.github.track-asia", module = "android-sdk-turf")
    }
}
