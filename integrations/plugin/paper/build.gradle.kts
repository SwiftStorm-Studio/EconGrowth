import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    alias(libs.plugins.runPaper)
    alias(libs.plugins.paperYAML)
    alias(libs.plugins.paperWeight)
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paper.get())
    compileOnly(libs.paper.api)

    implementation("net.ririfa:igf:1.5.9")
}

paper {
    name = "${rootProject.name}-${project.name}"
    description = project.description.toString()
    version = project.version.toString()
    apiVersion = "1.21"
    loader = "dev.swiftstorm.econgrowth.paper.loader.EconGrowthPluginLoader"
    main = "dev.swiftstorm.econgrowth.paper.EconGrowth"
    generateLibrariesJson = true
    foliaSupported = false
    contributors = listOf("RiriFa", "cotrin_d8")
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
}