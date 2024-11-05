package net.rk4z.s1.econgrowth

import net.rk4z.s1.econgrowth.utils.System
import net.rk4z.s1.pluginBase.LanguageManager
import net.rk4z.s1.pluginBase.Logger
import net.rk4z.s1.pluginBase.PluginEntry
import org.bukkit.Bukkit

@Suppress("unused")
class EconGrowth : PluginEntry(
    "econgrowth",
    "net.rk4z.s1.econgrowth",
    false,
    true,
    23781,
    false,
    "z5vRAQMP",
    listOf("ja", "en"),
    true,
    true,
    true
) {
    companion object {
        lateinit var dataBase: DataBase
            private set

        fun get(): EconGrowth {
            return get<EconGrowth>()
        }
    }

    val version = description.version

    override fun onLoadPre() {
        val serverName = Bukkit.getServer().name

        if (serverName.contains("Paper", true).not() && serverName.contains("Purpur", true).not()) {
            Logger.error(LanguageManager.getSysMessage(System.Error.SERVER_SHOULD_BE_PAPER), serverName)
        }

        dataBase = DataBase(get())
    }

    override fun onEnablePre() {
        if (dataBase.connectToDatabase()) {
            dataBase.createRequiredTables()
        }
    }

    override fun onEnablePost() {
        server.pluginManager.apply {
            registerEvents(EconGrowthEventListener(), this@EconGrowth)

        }
    }

    override fun onDisablePre() {
        dataBase.closeConnection()
    }

    override fun onCheckUpdate() {
        Logger.info(LanguageManager.getSysMessage(System.Log.CHECKING_UPDATE))
    }

    override fun onAllVersionsRetrieved(versionCount: Int) {
        Logger.info(LanguageManager.getSysMessage(System.Log.ALL_VERSION_COUNT, versionCount.toString()))
    }

    override fun onNewVersionFound(latestVersion: String, newerVersionCount: Int) {
        Logger.info(LanguageManager.getSysMessage(System.Log.NEW_VERSION_COUNT, newerVersionCount.toString()))
        Logger.info(LanguageManager.getSysMessage(System.Log.LATEST_VERSION_FOUND, latestVersion, version))
        Logger.info(LanguageManager.getSysMessage(System.Log.VIEW_LATEST_VER, MODRINTH_DOWNLOAD_URL))
    }

    override fun onNoNewVersionFound() {
        Logger.info(LanguageManager.getSysMessage(System.Log.YOU_ARE_USING_LATEST))
    }

    override fun onUpdateCheckFailed(responseCode: Int) {
        Logger.warn(LanguageManager.getSysMessage(System.Log.FAILED_TO_CHECK_UPDATE, responseCode.toString()))
    }

    override fun onUpdateCheckError(e: Exception) {
        Logger.error(LanguageManager.getSysMessage(System.Log.ERROR_WHILE_CHECKING_UPDATE, e.message ?: LanguageManager.getSysMessage(System.Log.Other.UNKNOWN_ERROR)))
    }
}