plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.multiplatform")
    id("org.jetbrains.kotlinx.atomicfu")
}

addAllMultiplatformTargets()

androidLibrary(nameSpace = "com.github.panpf.zoomimage.test.core")

kotlin {
    sourceSets {
        commonMain.dependencies {
            api(projects.internal.images)
            api(projects.zoomimageCore)
            api(libs.kotlin.test)
            api(libs.kotlinx.coroutines.test)
        }
        jvmCommonMain.dependencies {
            api(libs.kotlin.test.junit)
            api(libs.panpf.tools4j.reflect)
        }
        androidMain.dependencies {
            api(libs.androidx.test.runner)
            api(libs.androidx.test.rules)
            api(libs.androidx.test.ext.junit)
            api(libs.panpf.tools4a.device)
            api(libs.panpf.tools4a.dimen)
            api(libs.panpf.tools4a.display)
            api(libs.panpf.tools4a.network)
            api(libs.panpf.tools4a.run)
            api(libs.panpf.tools4a.test)
        }
        desktopMain.dependencies {
            api(skikoAwtRuntimeDependency(libs.versions.skiko.get()))
        }
    }
}