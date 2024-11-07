package net.rk4z.s1.econgrowth

import net.rk4z.s1.econgrowth.utils.PlayerRegInfo
import net.rk4z.s1.econgrowth.utils.ShortUUID
import net.rk4z.s1.swiftbase.Logger
import net.rk4z.s1.swiftbase.PluginEntry
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

@Suppress("SqlNoDataSourceInspection", "SqlResolve")
class DataBase(private val plugin: PluginEntry) {
    private var connection: Connection? = null

    fun connectToDatabase(): Boolean {
        val url = "jdbc:sqlite:${plugin.dataFolder.absolutePath}/database.db"
        return executeAndCatch({
            connection = DriverManager.getConnection(url)
            Logger.info("Successfully connected to the SQLite database!")
            true
        }, "Could not connect to the SQLite database!")?: false
    }

    fun createRequiredTables() {
        transaction { conn ->
            conn.createStatement().use { statement ->
                statement.execute(CREATE_PLAYER_TABLE)
                statement.execute(CREATE_PLAYER_JOBS_TABLE)
                statement.execute(CREATE_PLAYER_SPECIALIZATIONS_TABLE)
                statement.execute(CREATE_PLAYER_SKILLS_TABLE)
                statement.execute(CREATE_GUILDS_TABLE)
                statement.execute(CREATE_PLAYER_GUILDS_TABLE)
                statement.execute(CREATE_PLAYER_PLACED_BLOCKS_TABLE)
                statement.execute(CREATE_PLAYER_PLACED_BLOCKS_INDEX)
            }
            Logger.info("Required tables created successfully!")
        }
    }

    fun closeConnection() {
        executeAndCatch({
            connection?.close()
            Logger.info("Successfully closed the SQLite database connection!")
            connection = null
        }, "Could not close the SQLite database connection!")
    }

    fun playerIsRegistered(uuid: String): Boolean {
        return withConnection { conn ->
            val query = "SELECT COUNT(uuid) AS count FROM players WHERE uuid = ?;"
            conn.prepareStatement(query).use { statement ->
                statement.setString(1, ShortUUID.fromUUIDString(uuid).toShortString())
                statement.executeQuery().getInt("count") > 0
            }
        } ?: false
    }

    fun registerPlayer(data: PlayerRegInfo) {
        val query = "INSERT INTO players (uuid, balance, last_login, level) VALUES (?, ?, ?, ?);"
        withConnection { conn ->
            conn.prepareStatement(query).use { statement ->
                statement.setString(1, data.uuid.toShortString())
                statement.setDouble(2, data.balance)
                statement.setString(3, data.lastLogin)
                statement.setInt(4, data.level)
                statement.executeUpdate()
            }
        }
    }

    fun updateLastLogin(uuid: String, lastLogin: String) {
        val query = "UPDATE players SET last_login = ? WHERE uuid = ?;"
        executeAndCatch({
            withConnection { conn ->
                conn.prepareStatement(query).use { statement ->
                    statement.setString(1, lastLogin)
                    statement.setString(2, uuid)
                    statement.executeUpdate()
                }
            }
        }, "Could not update the last login!")
    }

    fun updateTotalXP(uuid: String, totalXP: Int) {
        val query = "UPDATE players SET current_total_xp = ? WHERE uuid = ?;"
        executeAndCatch({
            withConnection { conn ->
                conn.prepareStatement(query).use { statement ->
                    statement.setInt(1, totalXP)
                    statement.setString(2, uuid)
                    statement.executeUpdate()
                }
            }
        }, "Could not update the total XP!")
    }

    fun insertPlayerPlacedBlock(x: Int, y: Int, z: Int, dimension: String, material: String) {
        val query = "INSERT OR IGNORE INTO player_placed_blocks (x, y, z, dimension, material) VALUES (?, ?, ?, ?, ?);"
        executeAndCatch({
            withConnection { conn ->
                conn.prepareStatement(query).use { statement ->
                    statement.setInt(1, x)
                    statement.setInt(2, y)
                    statement.setInt(3, z)
                    statement.setString(4, dimension)
                    statement.setString(5, material)
                    statement.executeUpdate()
                    Logger.info("Player placed block at ($x, $y, $z) in $dimension inserted successfully.")
                }
            }
        }, "Could not insert the player placed block!")
    }

    fun isPlayerPlacedBlock(x: Int, y: Int, z: Int, dimension: String): Boolean {
        val query = "SELECT COUNT(*) AS count FROM player_placed_blocks WHERE x = ? AND y = ? AND z = ? AND dimension = ?;"
        return withConnection { conn ->
            conn.prepareStatement(query).use { statement ->
                statement.setInt(1, x)
                statement.setInt(2, y)
                statement.setInt(3, z)
                statement.setString(4, dimension)
                statement.executeQuery().getInt("count") > 0
            }
        } ?: false
    }

    fun deletePlayerPlacedBlock(x: Int, y: Int, z: Int, dimension: String) {
        val query = "DELETE FROM player_placed_blocks WHERE x = ? AND y = ? AND z = ? AND dimension = ?;"
        executeAndCatch({
            withConnection { conn ->
                conn.prepareStatement(query).use { statement ->
                    statement.setInt(1, x)
                    statement.setInt(2, y)
                    statement.setInt(3, z)
                    statement.setString(4, dimension)
                    statement.executeUpdate()
                    Logger.info("Player placed block at ($x, $y, $z) in $dimension deleted successfully.")
                }
            }
        }, "Could not delete the player placed block!")
    }

    // Helper functions for connection and transaction management
    private inline fun <T> withConnection(action: (Connection) -> T): T? {
        return connection?.let { action(it) } ?: run {
            Logger.error("SQLite database connection is null!")
            null
        }
    }

    private inline fun <T> transaction(action: (Connection) -> T): T? {
        return withConnection { conn ->
            try {
                conn.autoCommit = false
                val result = action(conn)
                conn.commit()
                result
            } catch (e: SQLException) {
                conn.rollback()
                Logger.error("Transaction failed and rolled back.")
                e.printStackTrace()
                null
            } finally {
                conn.autoCommit = true
            }
        }
    }

    private inline fun <T> executeAndCatch(action: () -> T, errorMessage: String): T? {
        return try {
            action()
        } catch (e: SQLException) {
            Logger.error(errorMessage)
            e.printStackTrace()
            null
        }
    }

    companion object {
        private const val CREATE_PLAYER_TABLE = """CREATE TABLE IF NOT EXISTS players (
            uuid TEXT PRIMARY KEY NOT NULL UNIQUE CHECK(length(uuid) = 22),
            balance REAL NOT NULL DEFAULT 0.0,
            last_login TEXT NOT NULL,
            current_total_xp INTEGER DEFAULT 0,
            level INTEGER DEFAULT 1 CHECK(level >= 1 AND level <= 200)
        );"""

        private const val CREATE_PLAYER_JOBS_TABLE = """CREATE TABLE IF NOT EXISTS players_jobs (
            uuid TEXT NOT NULL CHECK(length(uuid) = 22),
            job_id INTEGER NOT NULL,
            level INTEGER DEFAULT 1,
            PRIMARY KEY (uuid, job_id),
            FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
        );"""

        private const val CREATE_PLAYER_SPECIALIZATIONS_TABLE = """CREATE TABLE IF NOT EXISTS players_specializations (
            uuid TEXT NOT NULL CHECK(length(uuid) = 22),
            job_id INTEGER NOT NULL,
            PRIMARY KEY (uuid, job_id),
            FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
        );"""

        private const val CREATE_PLAYER_SKILLS_TABLE = """CREATE TABLE IF NOT EXISTS players_skills (
            uuid TEXT NOT NULL CHECK(length(uuid) = 22),
            skill_id INTEGER NOT NULL,
            level INTEGER DEFAULT 1,
            PRIMARY KEY (uuid, skill_id),
            FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE
        );"""

        private const val CREATE_GUILDS_TABLE = """CREATE TABLE IF NOT EXISTS guilds (
            guild_id INTEGER PRIMARY KEY AUTOINCREMENT,
            guild_name TEXT NOT NULL UNIQUE,
            guild_level INTEGER DEFAULT 1,
            guild_points INTEGER DEFAULT 0
        );"""

        private const val CREATE_PLAYER_GUILDS_TABLE = """CREATE TABLE IF NOT EXISTS players_guilds (
            uuid TEXT NOT NULL CHECK(length(uuid) = 22),
            guild_id INTEGER NOT NULL,
            role TEXT DEFAULT 'member',
            PRIMARY KEY (uuid),
            FOREIGN KEY (uuid) REFERENCES players(uuid) ON DELETE CASCADE,
            FOREIGN KEY (guild_id) REFERENCES guilds(guild_id) ON DELETE CASCADE
        );"""

        private const val CREATE_PLAYER_PLACED_BLOCKS_TABLE = """CREATE TABLE IF NOT EXISTS player_placed_blocks (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            x INTEGER NOT NULL,
            y INTEGER NOT NULL,
            z INTEGER NOT NULL,
            dimension TEXT NOT NULL,
            material TEXT NOT NULL,
            UNIQUE (x, y, z, dimension)
        );"""

        private const val CREATE_PLAYER_PLACED_BLOCKS_INDEX = """CREATE INDEX IF NOT EXISTS idx_player_placed_blocks_position
            ON player_placed_blocks (x, y, z, dimension);"""
    }
}
