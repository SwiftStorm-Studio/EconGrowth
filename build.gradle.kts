import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    alias(libs.plugins.kotlin)
    alias(libs.plugins.shadowJar)
    alias(libs.plugins.architectury.loom) apply false
    alias(libs.plugins.architectury.plugin) apply false
}

allprojects {
    apply(plugin = "kotlin")

    group = "dev.swiftstorm"
    version = "0.0.1"
    description = "A completely new project combining economic, skill and level elements."

    repositories {
        mavenCentral()
        mavenLocal()
        maven("https://repo.swiftstorm.dev/maven2")
        maven("https://jitpack.io")
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    afterEvaluate {
        dependencies {
            libs.apply {
                implementation(exposed.core)
                implementation(exposed.jdbc)
                implementation(exposed.dao)
                implementation(exposed.kotlin.datetime)
                implementation(caffeine)
            }
        }
    }
}

tasks.register<ShadowJar>("buildAll") {
    isZip64 = true

    val buildTasks = listOf(
        project(":integrations:core").tasks.named<Jar>("buildCore"),
        project(":integrations:plugin:paper").tasks.named<Jar>("buildPlatform"),
        project(":integrations:plugin:velocity").tasks.named<Jar>("buildPlatform"),
        project(":integrations:mod:common").tasks.named<Jar>("buildPlatform"),
        project(":integrations:mod:fabric").tasks.named<Jar>("buildPlatform"),
        project(":integrations:mod:quilt").tasks.named<Jar>("buildPlatform")
    )

    dependsOn(buildTasks)

    relocate("org.jetbrains.exposed", "net.ririfa.shadowed.exposed")

    exclude("kotlin/**")
    exclude("kotlinx/**")

    doLast {
        val outputFile: File = archiveFile.get().asFile
        println("Built JAR: $outputFile")
    }
}
