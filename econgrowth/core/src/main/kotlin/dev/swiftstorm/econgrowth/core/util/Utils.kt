package dev.swiftstorm.econgrowth.core.util

import java.nio.file.Path

val Path.dbDir: Path
    get() = this.resolve("database")

val Path.backupDir: Path
    get() = this.resolve("dataBackup")