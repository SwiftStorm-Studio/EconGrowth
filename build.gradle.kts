plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.architectury.loom) apply false
    alias(libs.plugins.architectury.plugin) apply false
    alias(libs.plugins.runPaper) apply false
    alias(libs.plugins.paperYAML) apply false
    alias(libs.plugins.paperWeight) apply false
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "net.rk4z.s1"
    version = "1.0.0"
    description = "A completely new project combining economic, skill and level elements."

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    afterEvaluate {
        dependencies {
            libs.apply {
                implementation(kotlin.stdlib)
                implementation(exposed.core)
                implementation(exposed.jdbc)
                implementation(exposed.dao)
                implementation(exposed.json)
                implementation(exposed.kotlin.datetime)
                implementation(h2)
                implementation(beacon)
                implementation(caffeine)
                implementation(swiftbase.core)
            }
        }
    }
}