package net.rk4z.s1.swiftbase.core

import net.rk4z.s1.swiftbase.core.Core.Companion.logger

interface MessageKey<P : IPlayer, C> {
    fun c(): C {
        return LanguageManager.get<P, C>().textComponentFactory(this.javaClass.simpleName)
    }

    fun rc(): String {
        return this.javaClass.simpleName
    }

    fun log(level: String = "INFO") {
        val message = rc()
        when (level.uppercase()) {
            "INFO" -> logger.info(message)
            "WARN" -> logger.warn(message)
            "ERROR" -> logger.error(message)
            else -> logger.debug(message)
        }
    }

    fun <P : IPlayer, C> MessageKey<P, C>.t(player: P): C {
        return LanguageManager.get<P, C>().getMessage(player, this)
    }
}

