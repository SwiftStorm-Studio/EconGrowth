pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://files.minecraftforge.net/maven/")
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}

rootProject.name = "EconGrowth"

include(":integrations")
include(":integrations:core")

include(":integrations:mod")
include(":integrations:mod:common")
include(":integrations:mod:fabric")
include(":integrations:mod:neoforge")


include(":integrations:plugin")
include(":integrations:plugin:paper")
include(":integrations:plugin:bungee")
include(":integrations:plugin:velocity")