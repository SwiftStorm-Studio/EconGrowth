tasks.register<Jar>("buildCore") {
    dependsOn("clean")
    mustRunAfter("clean")

    archiveBaseName.set("core")
    archiveClassifier.set("")
    archiveVersion.set("")

    from(sourceSets.main.get().output)
}
