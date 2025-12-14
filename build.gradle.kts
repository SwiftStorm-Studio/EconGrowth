@file:Suppress("PropertyName")

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.akkara.plugin) apply false
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.gradleup.shadow")

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/") { name = "PaperMC" }
        maven("https://repo.md-5.net/content/groups/public/") { name = "SpigotMC" }
        maven("https://oss.sonatype.org/content/repositories/snapshots") { name = "SonatypeOSS" }
        maven("https://repo.ririfa.net/maven2") { name = "RiriFaRepo" }
        maven("https://api.modrinth.com/maven") { name = "Modrinth" }
    }
}

val ECONGROWTH_BUILDER = "econgrowth-builder"
val ECONGROWTH_CORE = "econgrowth-core"
val ECONGROWTH_API = "econgrowth-api"
val ECONGROWTH_FABRIC = "econgrowth-fabric"
val ECONGROWTH_PAPER = "econgrowth-paper"
val ECONGROWTH_VELOCITY = "econgrowth-velocity"

val ECONGROWTH_FABRIC_VERSION = "0.0.1"
val ECONGROWTH_PAPER_VERSION = "0.0.1"
val ECONGROWTH_VELOCITY_VERSION = "0.0.1"

subprojects {
    when (name) {
        ECONGROWTH_BUILDER -> {

        }

        ECONGROWTH_CORE -> {
            apply(plugin = "dev.swiftstorm.akkaradb-plugin")
        }

        ECONGROWTH_API -> {
            dependencies {
                implementation(project(":$ECONGROWTH_CORE"))
            }
        }

        ECONGROWTH_FABRIC -> {
            version = ECONGROWTH_FABRIC_VERSION
        }

        ECONGROWTH_PAPER -> {
            version = ECONGROWTH_PAPER_VERSION
        }

        ECONGROWTH_VELOCITY -> {
            version = ECONGROWTH_VELOCITY_VERSION
        }
    }
}