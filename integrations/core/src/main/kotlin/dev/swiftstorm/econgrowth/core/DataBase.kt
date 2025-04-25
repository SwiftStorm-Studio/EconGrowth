package dev.swiftstorm.econgrowth.core

import dev.swiftstorm.econgrowth.core.tables.Players
import dev.swiftstorm.econgrowth.core.util.DBAll
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class DataBase(
    val dataFolder: File,
) {
    private val logger: Logger = LoggerFactory.getLogger("[EconGrowth] ${this::class.simpleName}")

    companion object {
        internal lateinit var activeInstance: DataBase
    }

    lateinit var db: Database
    lateinit var memDb: Database

    fun start(): Boolean {
        activeInstance = this
        return try {
            // database.mv.db
            val dbFile = File(dataFolder, "database").absoluteFile.path
            val fileDbUrl = "jdbc:h2:$dbFile;AUTO_SERVER=TRUE"
            val memoryDbUrl = "jdbc:h2:mem:bulletinboard;DB_CLOSE_DELAY=-1"

            db = Database.connect(fileDbUrl, driver = "org.h2.Driver", user = "sa", password = "")
            memDb = Database.connect(memoryDbUrl, driver = "org.h2.Driver", user = "sa", password = "")

            createRequiredTables()
            true
        } catch (e: Exception) {
            logger.error("Failed to start H2 database: ${e.message}")
            false
        }
    }

    fun stop() {
        try {
            db.connector().close()
            memDb.connector().close()
        } catch (e: Exception) {
            logger.warn("Failed to close database connections: ${e.message}")
        }
    }

    private fun createRequiredTables() {
        DBAll {
            SchemaUtils.create(Players)
        }
    }
}