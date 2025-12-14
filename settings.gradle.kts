pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.swiftstorm.dev/maven2/") { name = "SwiftStorm Repository" }
        maven("https://maven.fabricmc.net/") { name = "FabricMC" }
    }
}

rootProject.name = "EconGrowth"

fun safeInclude(name: String, path: String) {
    val dir = file(path)
    if (dir.exists()) {
        include(name)
        project(":$name").projectDir = dir
    }
}

safeInclude("econgrowth-builder", "econgrowth/builder")
safeInclude("econgrowth-api", "econgrowth/api")
safeInclude("econgrowth-core", "econgrowth/core")
safeInclude("econgrowth-fabric", "econgrowth/fabric")
safeInclude("econgrowth-paper", "econgrowth/paper")
safeInclude("econgrowth-velocity", "econgrowth/velocity")