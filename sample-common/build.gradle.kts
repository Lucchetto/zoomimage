plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.org.jetbrains.compose)
    alias(libs.plugins.com.android.library)
}

group = property("GROUP").toString()
version = property("versionName").toString()

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations.configureEach {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    jvm("desktop") {
        compilations.configureEach {
            kotlinOptions {
                jvmTarget = "1.8"
            }
        }
    }

    sourceSets {
        named("androidMain") {
            dependencies {
                api(libs.panpf.sketch3.compose)
            }
        }

        named("commonMain") {
            dependencies {
                api(project(":zoomimage-compose"))
                api(compose.foundation)
                api(compose.ui)
                api(compose.preview)
                api(compose.uiTooling.replace("ui-tooling", "ui-util"))
                api(libs.telephoto)
                implementation(compose.material3)
                implementation(libs.panpf.tools4j)
            }
        }
        named("commonTest") {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.junit)
                implementation(libs.panpf.tools4j.test)
            }
        }
    }
}

compose {
    val compilerDependencyDeclaration =
        libs.androidx.compose.compiler.get().run { "$module:$version" }
    kotlinCompilerPlugin.set(compilerDependencyDeclaration)
}

android {
    namespace = "com.github.panpf.zoomimage.sample.common"
    compileSdk = property("compileSdk").toString().toInt()

    defaultConfig {
        minSdk = property("minSdk21").toString().toInt()

        buildConfigField("String", "VERSION_NAME", "\"${property("versionName").toString()}\"")
        buildConfigField("int", "VERSION_CODE", property("versionCode").toString())

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}