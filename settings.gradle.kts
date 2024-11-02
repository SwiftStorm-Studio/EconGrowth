pluginManagement {
    repositories {
        maven("https://maven.fabricmc.net/")
        maven("https://maven.architectury.dev/")
        maven("https://files.minecraftforge.net/maven/")
        gradlePluginPortal()
    }
}

rootProject.name = "econgrowth"

include(":integration")
include(":integration:mod")
include(":integration:mod:common")
include(":integration:mod:fabric")
include(":integration:mod:neoforge")
include(":integration:plugin")
include(":integration:plugin:paper")
include(":integration:plugin:velocity")
include(":integration:plugin:bungee")