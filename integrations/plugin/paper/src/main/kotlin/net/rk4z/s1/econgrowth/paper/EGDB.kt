package net.rk4z.s1.econgrowth.paper

import net.rk4z.beacon.IEventHandler
import net.rk4z.beacon.handler
import net.rk4z.s1.econgrowth.paper.events.DatabaseChangeEvent
import net.rk4z.s1.econgrowth.paper.utils.ChangeInfo
import net.rk4z.s1.econgrowth.paper.utils.DBTaskQueue
import net.rk4z.s1.econgrowth.paper.utils.castValue
import net.rk4z.s1.econgrowth.paper.utils.getTimeByCountry
import net.rk4z.s1.swiftbase.core.Logger
import net.rk4z.s1.swiftbase.core.logIfDebug
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.sql.DriverManager
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Suppress("SqlNoDataSourceInspection", "ExposedReference", "unused")
class EGDB(private val plugin: EconGrowth) : IEventHandler {
    private var memoryDb: Database? = null
    private var connection: Database? = null

    fun connectToDatabase(): Boolean {
        val filePath = "${plugin.dataFolder.absolutePath}/database.db"
        val memoryUrl = "jdbc:sqlite::memory:"

        return try {
            val file = File(filePath)

            if (!file.exists()) {
                Logger.logIfDebug("Database file not found. Initializing a new file database.")
                connection = Database.connect("jdbc:sqlite:$filePath", driver = "org.sqlite.JDBC").also { db ->
                    transaction(db) {
                        SchemaUtils.create(
                            Players,
                            PlacedBlockByPlayer
                        )
                        Logger.info("Initialized new database with required tables.")
                    }
                }
            } else {
                Logger.info("File database found. Connecting...")
                connection = Database.connect("jdbc:sqlite:$filePath", driver = "org.sqlite.JDBC")
            }

            // インメモリDBの起動
            memoryDb = Database.connect(memoryUrl, driver = "org.sqlite.JDBC")

            // ファイルDBのデータをインメモリDBに移行
            DriverManager.getConnection(memoryUrl).use { memoryConnection ->
                DriverManager.getConnection("jdbc:sqlite:$filePath").use { fileConnection ->
                    fileConnection.prepareStatement("VACUUM INTO ':memory:'").execute()
                }
            }

            Logger.info("Successfully loaded database into memory!")
            true
        } catch (e: Exception) {
            Logger.error("Could not connect to the SQLite database!")
            e.printStackTrace()
            false
        }
    }

    fun backupDatabase() {
        try {
            val backupDir = File("${plugin.dataFolder.absolutePath}/dataBackup")
            if (!backupDir.exists()) backupDir.mkdirs()

            val sourceFile = File("${plugin.dataFolder.absolutePath}/database.db")
            val backupFile = File(backupDir, "database_${System.currentTimeMillis()}.zip")

            FileOutputStream(backupFile).use { fos ->
                ZipOutputStream(fos).use { zos ->
                    FileInputStream(sourceFile).use { fis ->
                        val entry = ZipEntry(sourceFile.name)
                        zos.putNextEntry(entry)
                        fis.copyTo(zos)
                    }
                }
            }

            Logger.info("Database backup created successfully: ${backupFile.absolutePath}")

            manageBackups(backupDir)
        } catch (e: Exception) {
            Logger.error("Failed to create database backup!")
            e.printStackTrace()
        }
    }

    private fun manageBackups(backupDir: File) {
        val backupFiles = backupDir.listFiles()?.filter { it.isFile && it.extension == "zip" } ?: return

        if (backupFiles.size > plugin.backupMaxSize) {
            val filesToDelete = backupFiles.sortedBy { it.lastModified() }.take(backupFiles.size - plugin.backupMaxSize)
            filesToDelete.forEach { file ->
                if (file.delete()) {
                    Logger.info("Deleted old backup: ${file.absolutePath}")
                } else {
                    Logger.warn("Failed to delete old backup: ${file.absolutePath}")
                }
            }
        }
    }

//>========================= [Functions] ====================<\\

    //region Players
    fun insertNewPlayer(uuid: String) {
        DBTaskQueue(ChangeInfo(
            table = Players,
            changes = mapOf(
                Players.uuid to uuid,
                Players.xp to 0.0f,
                Players.level to 1,
                Players.balance to 0.0,
                Players.lastLogin to getTimeByCountry()
            )
        )) {
            transaction(memoryDb!!) {
                Players.insert {
                    it[Players.uuid] = uuid
                    it[xp] = 0.0f
                    it[level] = 1
                    it[balance] = 0.0
                    it[lastLogin] = getTimeByCountry()
                }
            }
        }
    }

    fun updateLastLogin(uuid: String, lastLogin: String) {
        DBTaskQueue(ChangeInfo(
            table = Players,
            changes = mapOf(
                Players.lastLogin to lastLogin
            )
        )) {
            transaction(memoryDb!!) {
                Players.update({ Players.uuid eq uuid }) {
                    it[Players.lastLogin] = lastLogin
                }
            }
        }
    }

    fun updateXp(uuid: String, operationWithValue: String) {
        DBTaskQueue(ChangeInfo(
            table = Players,
            changes = mapOf(
                Players.xp to 0.0f
            )
        )) {
            transaction(memoryDb!!) {
                val currentXp = Players
                    .selectAll()
                    .where { Players.uuid eq uuid }
                    .singleOrNull()?.get(Players.xp) ?: 0f

                val regex = """([+\-*/])([0-9.]+)""".toRegex()
                val match = regex.matchEntire(operationWithValue)
                    ?: throw IllegalArgumentException("Invalid format: $operationWithValue")

                val operator = match.groupValues[1]
                val value = match.groupValues[2].toFloat()

                val newXp = when (operator) {
                    "+" -> currentXp + value
                    "-" -> currentXp - value
                    "*" -> currentXp * value
                    "/" -> if (value != 0f) currentXp / value else currentXp // 0での除算を防ぐ
                    else -> throw IllegalArgumentException("Unsupported operator: $operator")
                }

                // データベースを更新
                Players.update({ Players.uuid eq uuid }) {
                    it[xp] = newXp
                }
            }
        }
    }

    fun getLevel(uuid: String): Int {
        return DBTaskQueue {
            transaction(memoryDb!!) {
                Players
                    .selectAll()
                    .where { Players.uuid eq uuid }
                    .singleOrNull()?.get(Players.level) ?: 0
            }
        }
    }

    fun updateLevel(uuid: String, level: Int) {
        DBTaskQueue(ChangeInfo(
            table = Players,
            changes = mapOf(
                Players.level to level
            )
        )) {
            transaction(memoryDb!!) {
                Players.update({ Players.uuid eq uuid }) {
                    it[Players.level] = level
                }
            }
        }
    }

    fun updateBalance(uuid: String, balance: Double) {
        DBTaskQueue(ChangeInfo(
            table = Players,
            changes = mapOf(
                Players.balance to balance
            )
        )) {
            transaction(memoryDb!!) {
                Players.update({ Players.uuid eq uuid }) {
                    it[Players.balance] = balance
                }
            }
        }
    }
    //endregion

    //region PlacedBlockByPlayer
    fun insertNewBlock(x: Int, y: Int, z: Int, material: String, dim: String) {
        DBTaskQueue(ChangeInfo(
            table = PlacedBlockByPlayer,
            changes = mapOf(
                PlacedBlockByPlayer.x to x,
                PlacedBlockByPlayer.y to y,
                PlacedBlockByPlayer.z to z,
                PlacedBlockByPlayer.material to material,
                PlacedBlockByPlayer.dim to dim
            )
        )) {
            transaction(memoryDb!!) {
                PlacedBlockByPlayer.insert {
                    it[PlacedBlockByPlayer.x] = x
                    it[PlacedBlockByPlayer.y] = y
                    it[PlacedBlockByPlayer.z] = z
                    it[PlacedBlockByPlayer.material] = material
                    it[PlacedBlockByPlayer.dim] = dim
                }
            }
        }
    }

    fun isPlayerPlacedBlock(x: Int, y: Int, z: Int, dim: String): Boolean {
        return DBTaskQueue {
            transaction(memoryDb!!) {
                PlacedBlockByPlayer
                    .selectAll()
                    .where {
                        (PlacedBlockByPlayer.x eq x) and
                                (PlacedBlockByPlayer.y eq y) and
                                (PlacedBlockByPlayer.z eq z) and
                                (PlacedBlockByPlayer.dim eq dim)
                    }
                    .limit(1)
                    .any()
            }
        }
    }

    fun deleteBlockFromPlacedBlock(x: Int, y: Int, z: Int, dim: String) {
        DBTaskQueue(ChangeInfo(
            table = PlacedBlockByPlayer,
            changes = mapOf(
                PlacedBlockByPlayer.x to x,
                PlacedBlockByPlayer.y to y,
                PlacedBlockByPlayer.z to z,
                PlacedBlockByPlayer.dim to dim
            )
        )) {
            transaction(memoryDb!!) {
                PlacedBlockByPlayer.deleteWhere {
                    (PlacedBlockByPlayer.x eq x) and
                            (PlacedBlockByPlayer.y eq y) and
                            (PlacedBlockByPlayer.z eq z) and
                            (PlacedBlockByPlayer.dim eq dim)
                }
            }
        }
    }
    //endregion

//>================================================================<\\

//>========================= [Event Handlers] ====================<\\

    init {
        handler<DatabaseChangeEvent> { event ->
            val changeInfo = event.changeInfo
            val table = changeInfo.table
            val changes = changeInfo.changes

            try {
                transaction(connection!!) {
                    if (changes.isEmpty()) {
                        Logger.warn("No changes provided for table ${table.tableName}")
                        return@transaction
                    }

                    val primaryKey = table.primaryKey?.columns?.firstOrNull()
                    if (primaryKey == null) {
                        Logger.error("No primary key defined for table ${table.tableName}")
                        return@transaction
                    }

                    val primaryKeyColumn = primaryKey as Column<Any>
                    val primaryKeyValue = changes[primaryKeyColumn] ?: run {
                        Logger.error("Primary key value is missing in changes for table ${table.tableName}")
                        return@transaction
                    }

                    val exists = table
                        .selectAll()
                        .where { (primaryKeyColumn).eq(primaryKeyValue) }
                        .any()

                    if (exists) {
                        table.update({ (primaryKeyColumn).eq(primaryKeyValue) }) {
                            changes.forEach { (column, value) ->
                                it[column as Column<Any>] = column.castValue(value)
                            }
                        }
                        Logger.info("Updated record in ${table.tableName}: $changes")
                    } else {
                        table.insert {
                            changes.forEach { (column, value) ->
                                it[column as Column<Any>] = column.castValue(value)
                            }
                        }
                        Logger.info("Inserted new record in ${table.tableName}: $changes")
                    }
                }
            } catch (e: Exception) {
                Logger.error("Failed to sync changes to file DB for table ${table.tableName}: $changes")
                e.printStackTrace()
            }
        }
    }

    object Players : Table() {
        // 基本情報
        val uuid = varchar("uuid", 22) // プレイヤーのUUID（ShortUUID形式）
        val xp = float("xp") // プレイヤーの総経験値
        val level = integer("level") // プレイヤーのレベル
        val balance = double("balance") // プレイヤーの残高
        val lastLogin = text("last_login") // 最終ログイン日時。正確にはログアウト時に記録される

        //TODO: まだまだあるよ！
    }

    object PlacedBlockByPlayer : Table() {
        val x = integer("x")
        val y = integer("y")
        val z = integer("z")
        val material = varchar("material", 255)
        val dim = varchar("dim", 255)
    }
}
