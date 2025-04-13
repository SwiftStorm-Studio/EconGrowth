import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    alias(libs.plugins.runPaper)
    alias(libs.plugins.paperYAML)
    alias(libs.plugins.paperWeight)
}

dependencies {
    paperweight.paperDevBundle(libs.versions.paperApi.get())
    compileOnly(libs.paper.api)

    implementation("net.ririrfa:igf:+")
}

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    from(sourceSets.main.get().output)

    from(project(":integrations:core").sourceSets.main.get().output)

    from({
        configurations.runtimeClasspath.get()
            .filter { !it.name.contains("kotlin", ignoreCase = true) }
            .map { zipTree(it) }
    })
}

paper {
    name = "${rootProject.name}-${project.name}"
    description = project.description.toString()
    version = project.version.toString()
    apiVersion = "1.21"
    main = "dev.swiftstorm.econgrowth.paper.EconGrowth"
    generateLibrariesJson = true
    foliaSupported = false
    contributors = listOf("Lars", "cotrin_d8")
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD

    serverDependencies {
        register("Kotlin") {
            required = true
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }

        register("LuckPerms") {
            required = false
            load = PaperPluginDescription.RelativeLoadOrder.BEFORE
        }
    }
}