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
            }
        }

        named("commonMain") {
            dependencies {
                api(project(":zoomimage-core"))
                api(compose.foundation)
                api(compose.ui)
                api(compose.uiTooling.replace("ui-tooling", "ui-util"))
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
    namespace = "com.github.panpf.zoomimage.compose"
    compileSdk = property("compileSdk").toString().toInt()

    defaultConfig {
        minSdk = property("minSdk21").toString().toInt()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}