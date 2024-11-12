architectury {
    platformSetupLoomIde()
    fabric()
}

configurations {
    val common by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
    val compileClasspath by getting {
        extendsFrom(common)
    }

    val runtimeClasspath by getting {
        extendsFrom(common)
    }

    val developmentFabric by getting {
        extendsFrom(common)
    }
}

@Suppress("UnstableApiUsage")
dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        mappings(libs.fabric.yarn)
        mappings(libs.neoforge.yarn)
    })
    modImplementation(libs.fabric.loader)
    modImplementation(libs.fabric.api)
    modImplementation(libs.fabric.kotlin)

    "common"(project(path = ":integrations:mod:common", configuration = "namedElements")) { isTransitive = false }
}