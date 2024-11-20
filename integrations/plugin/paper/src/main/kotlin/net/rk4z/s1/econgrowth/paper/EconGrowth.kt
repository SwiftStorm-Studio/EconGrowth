package net.rk4z.s1.econgrowth.paper

import net.rk4z.s1.swiftbase.core.CB
import net.rk4z.s1.swiftbase.paper.PluginEntry
import org.slf4j.LoggerFactory

class EconGrowth : PluginEntry(
    id = "econgrowth",
    packageName = "net.rk4z.s1.econgrowth",
    isDebug = false,
    configFile = "config.yml",
    availableLang = listOf("ja"),
    langDir = "lang",
    logger = LoggerFactory.getLogger(this::class.simpleName),
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

    override fun onLoadPre() {
        EGDB = EGDB(this)
        CB.executor.executeAsyncTimer({
            EGDB.syncToFile()
        }, 0, 10)
    }

}