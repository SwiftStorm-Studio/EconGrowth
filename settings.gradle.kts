@file:Suppress("UnstableApiUsage")

pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://files.minecraftforge.net/maven/")
    }
}

rootProject.name = "EconGrowth"

include(":integrations")
include(":integrations:core")

include(":integrations:mod")
include(":integrations:mod:common")
include(":integrations:mod:fabric")
include(":integrations:mod:quilt")


include(":integrations:plugin")
include(":integrations:plugin:paper")
include(":integrations:plugin:velocity")