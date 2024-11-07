plugins {
    alias(libs.plugins.architectury.plugin)
}

architectury {
    minecraft = libs.versions.minecraft.get()
}

subprojects {
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "architectury-plugin")

//    dependencies {
//        compileOnly(project(":integrations:core"))
//    }
}