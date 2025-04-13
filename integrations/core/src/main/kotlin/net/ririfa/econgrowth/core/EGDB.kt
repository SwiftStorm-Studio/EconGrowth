package net.ririfa.econgrowth.core

import com.github.benmanes.caffeine.cache.Caffeine
import net.rk4z.s1.econgrowth.core.utils.ChangeInfo
import net.rk4z.s1.econgrowth.core.utils.DBTaskQueue
import net.rk4z.s1.econgrowth.core.utils.ShortUUID
import net.rk4z.s1.econgrowth.core.utils.getTimeByCountry
import net.rk4z.s1.swiftbase.core.Logger
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Suppress("SqlNoDataSourceInspection", "unused")
class EGDB(
    val dataFolder: String,
    val backupMaxSize: Int
) {
    private val fileURL = "$dataFolder/database.db"
    private val memoryURL = "jdbc:h2:mem:main;DB_CLOSE_DELAY=-1"
    var memoryDB: Database? = null
    var fileDB: Database? = null

    private val playersCache = Caffeine.newBuilder()
        .expireAfterWrite(20, TimeUnit.MINUTES)
        .build<String, Map<String, Any>>()

    private val blocksCache = Caffeine.newBuilder()
        .expireAfterWrite(20, TimeUnit.MINUTES)
        .build<String, Boolean>()

    fun setUpDatabase() {
        try {
            fileDB = Database.Companion.connect(
                "jdbc:h2:file:${dataFolder}/database;",
                driver = "org.h2.Driver"
            )

            memoryDB = Database.Companion.connect(
                "jdbc:h2:mem:main;DB_CLOSE_DELAY=-1;",
                driver = "org.h2.Driver"
            )

            transaction(fileDB!!) {
                SchemaUtils.create(Players, PlacedBlockByPlayer)
            }

            transaction(memoryDB!!) {
                SchemaUtils.create(Players, PlacedBlockByPlayer)
            }

            try {
                var filePlayersData: List<ResultRow> = listOf()
                var fileBlocksData: List<ResultRow> = listOf()

                transaction(fileDB!!) {
                    // ファイルDBからデータを取得
                    filePlayersData = Players.selectAll().toList()
                    fileBlocksData = PlacedBlockByPlayer.selectAll().toList()
                }

                transaction(memoryDB!!) {
                    filePlayersData.forEach { row ->
                        Players.insert {
                            it[uuid] = row[uuid]
                            it[totalXP] = row[totalXP]
                            it[xp] = row[xp]
                            it[level] = row[level]
                            it[balance] = row[balance]
                            it[lastLogin] = row[lastLogin]
                        }
                    }

                    fileBlocksData.forEach { row ->
                        PlacedBlockByPlayer.insert {
                            it[x] = row[x]
                            it[y] = row[y]
                            it[z] = row[z]
                            it[material] = row[material]
                            it[dim] = row[dim]
                        }
                    }
                }

                Logger.info("Data synchronized from File DB to Memory DB.")
            } catch (e: Exception) {
                Logger.error("Failed to synchronize data from File DB to Memory DB!")
                e.printStackTrace()
            }

            Logger.info("Databases initialized successfully! (File DB and Memory DB)")
        } catch (e: Exception) {
            Logger.error("Failed to setup databases!")
            e.printStackTrace()
        }
    }

    fun backupDatabase() {
        try {
            val backupDir = File("${dataFolder}/dataBackup")
            if (!backupDir.exists()) backupDir.mkdirs()

            val sourceFile = File("${dataFolder}/database.db")
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

        if (backupFiles.size > backupMaxSize) {
            val filesToDelete = backupFiles.sortedBy { it.lastModified() }.take(backupFiles.size - backupMaxSize)
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
    // 新規プレイヤーを登録
    fun insertNewPlayer(uuid: ShortUUID) {
        val p0 = uuid.toShortString()
        DBTaskQueue(
            ChangeInfo(
                table = Players,
                affectedColumns = listOf(
                    Players.uuid,
                    Players.totalXP,
                    Players.xp,
                    Players.level,
                    Players.balance,
                    Players.lastLogin
                )
            )
        ) {
            transaction(memoryDB!!) {
                Players.insert {
                    it[Players.uuid] = p0
                    it[totalXP] = 0.0f
                    it[xp] = 0.0f
                    it[level] = 1
                    it[balance] = 0.0
                    it[lastLogin] = getTimeByCountry()
                }

                playersCache.put(
                    p0, mapOf(
                        "level" to 1,
                        "totalXP" to 0.0f,
                        "xp" to 0.0f,
                        "balance" to 0.0,
                        "lastLogin" to getTimeByCountry()
                    )
                )
            }
        }
    }

    // プレイヤーが登録済みかどうかを確認
    fun isPlayerRegistered(uuid: ShortUUID): Boolean {
        val p0 = uuid.toShortString()
        if (playersCache.getIfPresent(p0)?.isNotEmpty() == true) {
            return true
        }

        return transaction(memoryDB!!) {
            Players.selectAll()
                .where { Players.uuid eq p0 }
                .singleOrNull() != null
        }
    }

    // プレイヤーの最終ログイン日時を更新
    fun updateLastLogin(uuid: ShortUUID, lastLogin: String) {
        val p0 = uuid.toShortString()
        DBTaskQueue(
            ChangeInfo(
                table = Players,
                affectedColumns = listOf(
                    Players.lastLogin
                )
            )
        ) {
            transaction(memoryDB!!) {
                Players.update({ Players.uuid eq p0 }) {
                    it[Players.lastLogin] = lastLogin
                }

                val cachedData = playersCache.getIfPresent(p0)?.toMutableMap() ?: mutableMapOf()
                cachedData["lastLogin"] = lastLogin
                playersCache.put(p0, cachedData)
            }
        }
    }

    // 現レベルのXPと総XPを更新し、必要であればレベルアップ処理を行う
    fun updateXp(uuid: String, operationWithValue: String) {
        DBTaskQueue(
            ChangeInfo(
                table = Players,
                affectedColumns = listOf(
                    Players.totalXP,
                    Players.xp
                )
            )
        ) {
            transaction(memoryDB!!) {
                val playerData = Players.selectAll()
                    .where { Players.uuid eq uuid }
                    .singleOrNull()
                val currentXp = playerData?.get(Players.xp) ?: 0f
                val currentTotalXp = playerData?.get(Players.totalXP) ?: 0f
                val currentLevel = playerData?.get(Players.level) ?: 1
            }
        }
    }

    // 現レベルのXPと総XPをPairで返す
    fun getXp(uuid: String): Pair<Float, Float> {
        val cachedData = playersCache.getIfPresent(uuid)
        if (cachedData != null) {
            val xp = cachedData["xp"] as? Float ?: 0f
            val totalXP = cachedData["totalXP"] as? Float ?: 0f
            return Pair(xp, totalXP)
        }

        return transaction(memoryDB!!) {
            Players
                .selectAll()
                .where { Players.uuid eq uuid }
                .singleOrNull()
                ?.let {
                    Pair(it[Players.xp], it[Players.totalXP])
                } ?: Pair(0f, 0f)
        }
    }

    // 現在のプレイヤーレベルを取得
    fun getLevel(uuid: ShortUUID): Int {
        val p0 = uuid.toShortString()
        val cachedData = playersCache.getIfPresent(p0)
        if (cachedData != null) {
            return cachedData["level"] as? Int ?: 0
        }

        return transaction(memoryDB!!) {
            Players
                .selectAll()
                .where { Players.uuid eq p0 }
                .singleOrNull()
                ?.get(Players.level) ?: 0
        }
    }

    // プレイヤーレベルを更新
    fun updateLevel(uuid: ShortUUID, level: Int) {
        DBTaskQueue(
            ChangeInfo(
                table = Players,
                affectedColumns = listOf(
                    Players.level
                )
            )
        ) {
            val p0 = uuid.toShortString()

            transaction(memoryDB!!) {
                Players.update({ Players.uuid eq p0 }) {
                    it[Players.level] = level
                }

                val cachedData = playersCache.getIfPresent(p0)?.toMutableMap() ?: mutableMapOf()
                cachedData["level"] = level
                playersCache.put(p0, cachedData)
            }
        }
    }

    // プレイヤーの残高を取得
    fun updateBalance(uuid: ShortUUID, balance: Double) {
        DBTaskQueue(
            ChangeInfo(
                table = Players,
                affectedColumns = listOf(
                    Players.balance
                )
            )
        ) {
            val p0 = uuid.toShortString()

            transaction(memoryDB!!) {
                Players.update({ Players.uuid eq p0 }) {
                    it[Players.balance] = balance
                }

                val cachedData = playersCache.getIfPresent(p0)?.toMutableMap() ?: mutableMapOf()
                cachedData["balance"] = balance
                playersCache.put(p0, cachedData)
            }
        }
    }
    //endregion

    //region PlacedBlockByPlayer
    fun insertNewBlock(x: Int, y: Int, z: Int, material: String, dim: String) {
        DBTaskQueue(
            ChangeInfo(
                table = PlacedBlockByPlayer,
                affectedColumns = listOf(
                    PlacedBlockByPlayer.x,
                    PlacedBlockByPlayer.y,
                    PlacedBlockByPlayer.z,
                    PlacedBlockByPlayer.material,
                    PlacedBlockByPlayer.dim
                )
            )
        ) {
            transaction(memoryDB!!) {
                PlacedBlockByPlayer.insert {
                    it[PlacedBlockByPlayer.x] = x
                    it[PlacedBlockByPlayer.y] = y
                    it[PlacedBlockByPlayer.z] = z
                    it[PlacedBlockByPlayer.material] = material
                    it[PlacedBlockByPlayer.dim] = dim
                }

                val cacheKey = "$x:$y:$z:$dim"
                blocksCache.put(cacheKey, true)
            }
        }
    }

    fun isPlayerPlacedBlock(x: Int, y: Int, z: Int, dim: String): Boolean {
        val cacheKey = "$x:$y:$z:$dim"

        if (blocksCache.getIfPresent(cacheKey) == true) {
            return true
        }

        return transaction(memoryDB!!) {
            PlacedBlockByPlayer
                .selectAll()
                .where {
                    (PlacedBlockByPlayer.x eq x) and
                            (PlacedBlockByPlayer.y eq y) and
                            (PlacedBlockByPlayer.z eq z) and
                            (PlacedBlockByPlayer.dim eq dim)
                }
                .singleOrNull() != null
        }
    }

    fun deleteBlockFromPlacedBlock(x: Int, y: Int, z: Int, dim: String) {
        DBTaskQueue(
            ChangeInfo(
                table = PlacedBlockByPlayer,
                affectedColumns = listOf(
                    PlacedBlockByPlayer.x,
                    PlacedBlockByPlayer.y,
                    PlacedBlockByPlayer.z,
                    PlacedBlockByPlayer.dim
                )
            )
        ) {
            transaction(memoryDB!!) {
                PlacedBlockByPlayer.deleteWhere {
                    (PlacedBlockByPlayer.x eq x) and
                            (PlacedBlockByPlayer.y eq y) and
                            (PlacedBlockByPlayer.z eq z) and
                            (PlacedBlockByPlayer.dim eq dim)
                }

                val cacheKey = "$x:$y:$z:$dim"
                blocksCache.invalidate(cacheKey)
            }
        }
    }
    //endregion

//>================================================================<\\

    object Players : Table() {
        // 基本情報
        val uuid = varchar("uuid", 22) // プレイヤーのUUID（ShortUUID形式）
        val totalXP = float("total_xp") // プレイヤーの総経験値
        val xp = float("xp") // プレイヤーの経験値(現在のレベルでの)
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