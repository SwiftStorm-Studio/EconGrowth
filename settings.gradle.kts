@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.swiftstorm.dev/maven2")
        maven("https://jitpack.io")
        maven("https://repo.papermc.io/repository/maven-public/")
    }
}