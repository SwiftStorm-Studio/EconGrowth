package net.rk4z.s1.econgrowth.paper

import net.rk4z.s1.swiftbase.core.CB
import net.rk4z.s1.swiftbase.core.Logger
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.DriverManager

@Suppress("SqlSourceToSinkFlow", "SqlNoDataSourceInspection", "ExposedReference", "unused")
class DataBase(private val plugin: EconGrowth) {
    private var memoryDb: Database? = null

    fun connectToDatabase(): Boolean {
        val filePath = "${plugin.dataFolder.absolutePath}/database.db"
        val memoryUrl = "jdbc:sqlite::memory:"

        return try {
            val file = File(filePath)

            if (!file.exists()) {
                initializeFileDatabase(filePath)
            }

            // インメモリDBの起動（効率向上のためファイルDBから読み込む）
            memoryDb = Database.connect(memoryUrl, driver = "org.sqlite.JDBC")

            // VACUUM INTOを利用して、ファイルDBのデータをインメモリDBにコピー
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

    private fun initializeFileDatabase(filePath: String) {
        CB.executor.execute {
            try {
                // URL構築 & ファイルDBの起動
                val fileDb = Database.connect("jdbc:sqlite:$filePath", driver = "org.sqlite.JDBC")

                // 必要なテーブルを作成
                transaction(fileDb) {
                    SchemaUtils.create(
                        Players,
                        PlacedBlockByPlayer
                    )
                    Logger.info("Initialized new database with required tables.")
                }
            } catch (e: Exception) {
                Logger.error("Failed to initialize the file database!")
                e.printStackTrace()
                throw e
            }
        }
    }

    fun syncToFile() {
        CB.executor.executeAsync {
            try {
                val filePath = "${plugin.dataFolder.absolutePath}/database.db"
                val memoryUrl = "jdbc:sqlite::memory:"

                // インメモリDBの内容をファイルDBに書き込む
                // インメモリは名前の通りプログラム終了時に消失するため、ファイルに書き込む必要がある
                DriverManager.getConnection(memoryUrl).use { memoryConnection ->
                    DriverManager.getConnection("jdbc:sqlite:$filePath").use { fileConnection ->
                        // VACUUM INTOでインメモリDBの内容をファイルDBに書き込む
                        memoryConnection.prepareStatement("VACUUM INTO '$filePath'").execute()
                    }
                }

                Logger.info("In-memory database synchronized to file successfully!")
            } catch (e: Exception) {
                Logger.error("Failed to synchronize in-memory database to file!")
                e.printStackTrace()
            }
        }
    }

//>========================= [Functions] ====================<\\

    //region Players
    fun insertNewPlayer(uuid: String) {
        DBTaskQueue {
            transaction(memoryDb!!) {
                Players.insert {
                    it[Players.uuid] = uuid
                    it[xp] = 0.0f
                    it[level] = 1
                    it[balance] = 0.0
                    it[lastLogin] = System.currentTimeMillis()
                }
            }
        }
    }

    fun updateLastLogin(uuid: String, lastLogin: Long) {
        DBTaskQueue {
            transaction(memoryDb!!) {
                Players.update({ Players.uuid eq uuid }) {
                    it[Players.lastLogin] = lastLogin
                }
            }
        }
    }

    fun updateXp(uuid: String, xp: Float) {
        DBTaskQueue {
            transaction(memoryDb!!) {
                Players.update({ Players.uuid eq uuid }) {
                    it[Players.xp] = xp
                }
            }
        }
    }

    fun updateLevel(uuid: String, level: Int) {
        DBTaskQueue {
            transaction(memoryDb!!) {
                Players.update({ Players.uuid eq uuid }) {
                    it[Players.level] = level
                }
            }
        }
    }

    fun updateBalance(uuid: String, balance: Double) {
        DBTaskQueue {
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
        DBTaskQueue {
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
        DBTaskQueue {
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

    object Players : Table() {
        // 基本情報
        val uuid = varchar("uuid", 22) // プレイヤーのUUID（ShortUUID形式）
        val xp = float("xp") // プレイヤーの総経験値
        val level = integer("level") // プレイヤーのレベル
        val balance = double("balance") // プレイヤーの残高
        val lastLogin = long("last_login") // 最終ログイン日時。正確にはログアウト時に記録される（タイムスタンプ形式)

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
