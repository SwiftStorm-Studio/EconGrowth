plugins {
    alias(libs.plugins.architectury)
    alias(libs.plugins.kotlin)
    alias(libs.plugins.loom) apply false
    alias(libs.plugins.shadow) apply false
}

architectury {
    minecraft = libs.versions.minecraft.get()
}

allprojects {
    val maven_group: String by project
    val mod_version: String by project
    group = maven_group
    version = mod_version
}

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")
    apply(plugin = "org.jetbrains.kotlin.jvm")

    base {
        val archives_name: String by project
        archivesName = "$archives_name-${project.name}"
    }
}
