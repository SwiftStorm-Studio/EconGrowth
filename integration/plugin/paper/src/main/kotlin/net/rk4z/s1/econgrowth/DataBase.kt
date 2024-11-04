package net.rk4z.s1.econgrowth

import net.rk4z.s1.pluginBase.Logger
import net.rk4z.s1.pluginBase.PluginEntry
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

@Suppress("SqlNoDataSourceInspection", "SqlResolve")
class DataBase(
    private val plugin: PluginEntry
) {
    private var connection: Connection? = null

    fun connectToDatabase(): Boolean {
        val url = "jdbc:sqlite:${plugin.dataFolder.absolutePath}/database.db"

        try {
            connection = DriverManager.getConnection(url)
            Logger.info("Successfully connected to the SQLite database!")
            return true
        } catch (e: SQLException) {
            Logger.error("Could not connect to the SQLite database!")
            e.printStackTrace()
            return false
        } catch (e: Exception) {
            Logger.error("An unknown error occurred while connecting to the SQLite database!")
            e.printStackTrace()
            return false
        }
    }

    fun createRequiredTables() {
        if (connection == null) {
            Logger.error("Could not create the required tables because the connection to the SQLite database is null!")
            return
        }

        // The player's UUID is stored in a shortened form by ShortUUID. This saves a small amount of storage.
        val player = """
        CREATE TABLE IF NOT EXISTS players (
            uuid TEXT PRIMARY KEY NOT NULL UNIQUE CHECK(length(uuid) = 22),
            balance REAL NOT NULL DEFAULT 0.0,
            last_login DATETIME NOT NULL,
            level INTEGER DEFAULT 1 CHECK(level >= 1 AND level <= 200)
        );
    """.trimIndent()

        val playerJobs = """
        CREATE TABLE IF NOT EXISTS players_jobs (
            uuid TEXT NOT NULL CHECK(length(uuid) = 22),
            job_id INTEGER NOT NULL,
            level INTEGER DEFAULT 1,
            PRIMARY KEY (uuid, job_id),
            FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
        );
    """.trimIndent()

        val playerSpecializations = """
        CREATE TABLE IF NOT EXISTS players_specializations (
            uuid TEXT NOT NULL CHECK(length(uuid) = 22),
            job_id INTEGER NOT NULL,
            PRIMARY KEY (uuid, job_id),
            FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
        );
    """.trimIndent()

        val playersSkills = """
        CREATE TABLE IF NOT EXISTS players_skills (
            uuid TEXT NOT NULL CHECK(length(uuid) = 22),
            skill_id INTEGER NOT NULL,
            level INTEGER DEFAULT 1,
            PRIMARY KEY (uuid, skill_id),
            FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
        );
    """.trimIndent()

        val guilds = """
        CREATE TABLE IF NOT EXISTS guilds (
            guild_id INTEGER PRIMARY KEY AUTOINCREMENT,
            guild_name TEXT NOT NULL UNIQUE,
            guild_level INTEGER DEFAULT 1,
            guild_points INTEGER DEFAULT 0
        );
    """.trimIndent()

        val playersGuilds = """
        CREATE TABLE IF NOT EXISTS players_guilds (
            uuid TEXT NOT NULL CHECK(length(uuid) = 22),
            guild_id INTEGER NOT NULL,
            role TEXT DEFAULT 'member',
            PRIMARY KEY (uuid),
            FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE,
            FOREIGN KEY (guild_id) REFERENCES guilds(guild_id) ON DELETE CASCADE
        );
    """.trimIndent()

        try {
            connection?.autoCommit = false

            connection?.createStatement()?.use { statement ->
                statement.execute(player)
                statement.execute(playerJobs)
                statement.execute(playerSpecializations)
                statement.execute(playersSkills)
                statement.execute(guilds)
                statement.execute(playersGuilds)
            }

            connection?.commit()
            Logger.info("Required tables created successfully!")
        } catch (e: SQLException) {
            connection?.rollback()
            Logger.error("Could not create the required tables!")
            e.printStackTrace()
        } catch (e: Exception) {
            Logger.error("An unknown error occurred while creating the required tables!")
            e.printStackTrace()
        } finally {
            connection?.autoCommit = true
        }
    }


    fun closeConnection() {
        try {
            if (connection != null) {
                connection?.close()
                Logger.info("Successfully closed the SQLite database connection!")
            }
        } catch (e: SQLException) {
            Logger.error("Could not close the SQLite database connection!")
            e.printStackTrace()
        } catch (e: Exception) {
            Logger.error("An unknown error occurred while closing the SQLite database connection!")
            e.printStackTrace()
        } finally {
            connection = null
        }
    }
}