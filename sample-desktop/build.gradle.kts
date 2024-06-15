import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.org.jetbrains.kotlin.multiplatform)
    alias(libs.plugins.org.jetbrains.compose)
}

group = property("GROUP").toString()
version = property("versionName").toString()

kotlin {
    jvm {
        jvmToolchain(17)
        withJava()
    }
    sourceSets {
        val jvmMain by getting {
            dependencies {
                implementation(project(":sample-common"))
                implementation(project(":zoomimage-resources"))
                implementation(compose.desktop.currentOs)
                implementation(project(":zoomimage-compose-coil"))
//                implementation("io.github.qdsfdhvh:image-loader:1.6.8")
//                implementation("media.kamel:kamel-image:0.8.2")
//                implementation("io.ktor:ktor-client-java:2.3.5")    // for kamel-image
            }
        }
        val jvmTest by getting
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = property("GROUP").toString()
            packageVersion = property("versionName").toString().let {
                if (it.contains("-")) {
                    it.substring(0, it.indexOf("-"))
                } else {
                    it
                }
            }
        }
    }
}