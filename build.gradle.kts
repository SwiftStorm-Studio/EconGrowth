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

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}