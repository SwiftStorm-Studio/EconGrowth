package net.rk4z.s1.econgrowth.paper

import net.rk4z.beacon.EventBus
import net.rk4z.s1.econgrowth.core.EGDB
import net.rk4z.s1.econgrowth.paper.listeners.EconGrowthEventListener
import net.rk4z.s1.swiftbase.paper.PluginEntry
import org.slf4j.LoggerFactory

class EconGrowth : PluginEntry(
    id = "econgrowth",
    packageName = "net.rk4z.s1.econgrowth",
    isDebug = true,
    configFile = "config.yml",
    availableLang = listOf("ja", "en"),
    langDir = "lang",
    logger = LoggerFactory.getLogger(EconGrowth::class.simpleName),
    enableUpdateChecker = false,
    modrinthID = "z5vRAQMP",
    serviceId = 23781
) {
    companion object {
        lateinit var EGDB: EGDB
            private set

        fun get(): EconGrowth? {
            return get<EconGrowth>()
        }
    }

    var backupMaxSize: Int = 20

    override fun onLoadPre() {
        EGDB = EGDB(
            this.dataFolder.absolutePath,
            backupMaxSize
        )
        EventBus.initialize(
            packageNames = arrayOf("net.rk4z.s1.econgrowth"),
            threadPoolSize = 2
        )
        EGDB.setUpDatabase()
    }

    override fun onLoadPost() {
        lc<Int>("database.backup.maxamount")?.let {
            backupMaxSize = it
        }
    }

    override fun onEnablePost() {
        server.pluginManager.apply {
            registerEvents(EconGrowthEventListener(), this@EconGrowth)
        }
    }

    override fun onDisablePre() {
        EGDB.backupDatabase()
    }
}