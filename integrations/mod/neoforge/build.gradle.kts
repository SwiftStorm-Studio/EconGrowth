architectury {
    platformSetupLoomIde()
    neoForge()
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

    val developmentNeoForge by getting {
        extendsFrom(common)
    }

    val shadowBundle by creating {
        isCanBeResolved = true
        isCanBeConsumed = false
    }
}

repositories {
    maven {
        name = "NeoForged"
        url = uri("https://maven.neoforged.net/releases")
    }
    maven {
        name = "KotlinForForge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
        content { includeGroup("thedarkcolour") }
    }
}

@Suppress("UnstableApiUsage")
dependencies {
    minecraft(libs.minecraft)
    mappings(loom.layered {
        mappings(libs.fabric.yarn)
        mappings(libs.neoforge.yarn)
    })

    neoForge(libs.neoforge.api)
    modImplementation(libs.architectury.neoforge)
    implementation(libs.neoforge.kotlin) {
        exclude(group = "net.neoforged.fancymodloader", module = "loader")
    }

    "common"(project(path = ":integrations:mod:common", configuration = "namedElements")) { isTransitive = false }
    "shadowBundle"(project(path = ":integrations:mod:common", configuration = "transformProductionFabric"))
}