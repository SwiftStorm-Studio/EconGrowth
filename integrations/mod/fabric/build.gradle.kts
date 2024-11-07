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

    val shadowBundle by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

@Suppress("UnstableApiUsage")
dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        mappings(libs.fabric.yarn)
        mappings(libs.neoforge.yarn)
    })

    "common"(project(path = ":integrations:mod:common", configuration = "namedElements")) { isTransitive = false }
    "shadowBundle"(project(path = ":integrations:mod:common", configuration = "transformProductionFabric"))
}