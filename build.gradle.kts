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

    val exposedVersion: String by project

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    afterEvaluate {
        dependencies {
            implementation(libs.swiftbase.core)
            implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
            implementation("org.jetbrains.exposed:exposed-crypt:$exposedVersion")
            implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
            implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
            implementation("org.jetbrains.exposed:exposed-kotlin-datetime:$exposedVersion")
            implementation("org.jetbrains.exposed:exposed-json:$exposedVersion")
            implementation("org.jetbrains.exposed:exposed-money:$exposedVersion")
            implementation("org.xerial:sqlite-jdbc:3.47.0.0")
        }
    }
}