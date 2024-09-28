architectury {
    val enabledPlatforms: String by project
    common(enabledPlatforms.split(','))
}

@Suppress("UnstableApiUsage")
dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        mappings("net.fabricmc:yarn:${libs.versions.yarnFabric.get()}")
        mappings(libs.yarnNeoforge)
    })

    modImplementation(libs.fabric.loader)
    modImplementation(libs.architectury)
}
