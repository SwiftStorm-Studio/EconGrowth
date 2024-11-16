package net.rk4z.s1.econgrowth.paper

import net.rk4z.s1.swiftbase.core.Logger
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.sql.DriverManager

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

    fun syncToFile() {
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

    object Players : Table() {
        // 基本情報
        val uuid = varchar("uuid", 22) // プレイヤーのUUID（ShortUUID形式）
        val level = integer("level") // プレイヤーのレベル
        val balance = double("balance") // プレイヤーの残高
        val lastLogin = long("last_login") // 最終ログイン日時（タイムスタンプ形式）

        // 職業情報
        val specializations = varchar("specializations", 255) // 専門職（カンマ区切りで最大3つ）
        val eliteSpecialization = varchar("elite_specialization", 50).nullable() // 極職（null許容）

        // ギルド情報
        val guild = varchar("guild", 50).nullable() // 所属ギルド（null許容）

        // スキル情報
        val unlockedSkills = text("unlocked_skills").nullable() // 解放済みスキル（JSON文字列で格納）

        // ランキング情報
        val serverRank = integer("server_rank").nullable() // サーバー内順位（null許容）

        // フレンド情報
        val friends = text("friends").nullable() // 登録済みフレンド（UUIDのリストをJSON形式で格納）

        // 名誉/称号情報
        val titles = text("titles").nullable() // 獲得済みの名誉や称号（JSON形式）

        // ステータス情報
        val buffs = text("buffs").nullable() // バフ情報（JSON形式）
        val debuffs = text("debuffs").nullable() // デバフ情報（JSON形式）
    }

    object PlacedBlockByPlayer : Table() {
        val x = integer("x")
        val y = integer("y")
        val z = integer("z")
        val material = varchar("material", 255)
        val dim = varchar("dim", 255)
        val chunk = varchar("chunk", 255)
    }
}
