plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.architectury.loom) apply false
    alias(libs.plugins.architectury.plugin) apply false
    alias(libs.plugins.runPaper) apply false
    alias(libs.plugins.paperYAML) apply false
    alias(libs.plugins.paperWeight) apply false
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "net.rk4z.s1"
    version = "1.0.0"

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    afterEvaluate {
        dependencies {
            implementation("org.jetbrains.kotlin:kotlin-reflect:2.0.21")
            implementation("org.slf4j:slf4j-api:2.1.0-alpha1")
            implementation("org.yaml:snakeyaml:2.3")
            implementation("org.reflections:reflections:0.10.2")
        }
    }
}