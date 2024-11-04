package net.rk4z.s1.econgrowth

import net.rk4z.s1.pluginBase.PluginEntry

@Suppress("unused")
class EconGrowth : PluginEntry(
    "assets/econgrowth",
    "net.rk4z.s1.econgrowth",
    false,
    true,
    23781,
    false,
    "z5vRAQMP",
    listOf("ja", "en")
) {
    companion object {
        lateinit var dataBase: DataBase
            private set

        fun get(): EconGrowth {
            return get<EconGrowth>()
        }
    }

    override fun onLoadPre() {
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
}