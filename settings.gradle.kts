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
//include(":integrations:core")

include(":integrations:mod")
include(":integrations:mod:fabric")
include(":integrations:mod:neoforge")
include(":integrations:mod:common")
include(":integrations:plugin")