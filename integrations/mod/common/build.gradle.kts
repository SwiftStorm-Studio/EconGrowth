architectury {
    val enabledPlatforms: String by project
    common(enabledPlatforms.split(','))
}

@Suppress("UnstableApiUsage")
dependencies {
    minecraft(libs.minecraft)
    mappings(loom.officialMojangMappings())

    modImplementation(libs.fabric.loader)
    modImplementation(libs.architectury.api)
}