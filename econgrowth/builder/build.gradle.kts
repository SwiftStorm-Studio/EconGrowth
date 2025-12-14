import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.util.Locale

val generateFabric = registerMergedJar("fabric", ":econgrowth-fabric")
val generatePaper = registerMergedJar("paper", ":econgrowth-paper")
val generateVelocity = registerMergedJar("velocity", ":econgrowth-velocity")

tasks.register("generateAll") {
    group = "EconGrowth"
    dependsOn(generateFabric, generatePaper, generateVelocity)
}

fun registerMergedJar(platform: String, projectPath: String) =
    tasks.register<Jar>("generate${platform.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }}") {
        val version = project(projectPath).version
        group = "EconGrowth"
        description = "Merge core and $platform shaded jars"
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
        archiveBaseName = "${rootProject.name}-$platform-$version"
        destinationDirectory.set(layout.projectDirectory.dir("Artifacts"))

        val coreShadow = project(":econgrowth-core").tasks.named<ShadowJar>("shadowJar")
        val platformShadow = project(projectPath).tasks.named<ShadowJar>("shadowJar")
        dependsOn(coreShadow, platformShadow)

        from(zipTree(coreShadow.get().archiveFile))
        from(zipTree(platformShadow.get().archiveFile))

        archiveClassifier.set("merged")
    }