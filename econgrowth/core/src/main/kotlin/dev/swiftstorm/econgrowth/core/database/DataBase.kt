package dev.swiftstorm.econgrowth.core.database

import dev.swiftstorm.akkaradb.common.ShortUUID
import dev.swiftstorm.akkaradb.engine.AkkDSL
import dev.swiftstorm.akkaradb.engine.Id
import dev.swiftstorm.akkaradb.engine.PackedTable
import dev.swiftstorm.akkaradb.engine.StartupMode
import dev.swiftstorm.akkaradb.format.akk.parity.RSParityCoder
import dev.swiftstorm.econgrowth.core.Logger
import dev.swiftstorm.econgrowth.core.util.backupDir
import dev.swiftstorm.econgrowth.core.util.dbDir
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Path
import java.util.Date
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class DataBase(
    val dataFolder: Path,
    val backupMaxSize: Int
) {
    val players: PackedTable<Player, UUID> = AkkDSL.open(dataFolder.dbDir, StartupMode.ULTRA_FAST) {
        m = 2; parityCoder = RSParityCoder(2)
    }

    fun insertPlayer(player: Player) { players.put(player) }

    fun isPlayerExists(uuid: UUID): Boolean {
        return players.get(uuid) != null
    }

    fun updateLastOnline(uuid: UUID, date: Date = Date()) {
        val player = players.get(uuid) ?: return
        val updatedPlayer = player.copy(lastOnline = date)
        players.put(updatedPlayer)
    }

    fun updateXP(uuid: UUID, totalXP: Float, xp: Float) {
        val player = players.get(uuid) ?: return
        val updatedPlayer = player.copy(totalXP = totalXP, xp = xp)
        players.put(updatedPlayer)
    }

    fun getXP(uuid: UUID): Pair<Float, Float>? {
        val player = players.get(uuid) ?: return null
        return Pair(player.totalXP, player.xp)
    }

    fun getLevel(uuid: UUID): Int? {
        return players.get(uuid)?.level
    }

    fun updateLevel(uuid: UUID, level: Int) {
        val player = players.get(uuid) ?: return
        val updatedPlayer = player.copy(level = level)
        players.put(updatedPlayer)
    }

    fun updateBalance(uuid: UUID, balance: Double) {
        val player = players.get(uuid) ?: return
        val updatedPlayer = player.copy(balance = balance)
        players.put(updatedPlayer)
    }

    fun genBackup() {
        try {
            val backupDir = dataFolder.backupDir.toFile()
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
}

data class Player(
    @Id val uuid: ShortUUID,
    val totalXP: Float,
    val xp: Float,
    val level: Int,
    val balance: Double,
    val lastOnline: Date,

    //TODO: Add more fields here
)