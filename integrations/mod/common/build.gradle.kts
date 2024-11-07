architectury {
    val enabledPlatforms: String by project
    common(enabledPlatforms.split(','))
}

@Suppress("UnstableApiUsage")
dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        mappings(libs.fabric.yarn)
        mappings(libs.neoforge.yarn)
    })

    modImplementation(libs.fabric.loader)
    modImplementation(libs.architectury.api)
}