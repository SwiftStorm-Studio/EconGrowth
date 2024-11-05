import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("io.papermc.paperweight.userdev") version "1.7.1"
    id("net.minecrell.plugin-yml.bukkit") version "0.6.0"
    id("xyz.jpenilla.run-paper") version "2.3.0"
}

dependencies {
    paperweight.paperDevBundle("1.21.3-R0.1-SNAPSHOT")
    compileOnly("io.papermc.paper:paper-api:1.21.3-R0.1-SNAPSHOT")

   library("net.rk4z.s1:pluginbase:1.1.9")
}

bukkit {
    name = "EconGrowth"
    version = rootProject.version.toString()
    apiVersion = "1.21"
    main = "net.rk4z.s1.econgrowth.EconGrowth"
    description = "All-new mods/plugins incorporating economic, occupational, guild and levelling systems."
    author = "Lars"
    contributors = listOf("Lars", "cotrin_d8", "yumu25")
    website = ""
    foliaSupported = false
    load = BukkitPluginDescription.PluginLoadOrder.STARTUP
    generateLibrariesJson = true

    permissions {
        //T O D O!
    }
}