package net.rk4z.s1.swiftbase.core

@Suppress("unused", "DEPRECATION")
interface MessageKey<P : IPlayer, C> {
    fun c(): C {
        return LanguageManager.get<P, C>().textComponentFactory(this.javaClass.simpleName)
    }

    fun rc(): String {
        return this.javaClass.simpleName
    }

    fun log(level: LogLevel = LogLevel.INFO) {
        val message = this.rc()
        when (level) {
            LogLevel.INFO -> Logger.info(message)
            LogLevel.WARN -> Logger.warn(message)
            LogLevel.ERROR -> Logger.error(message)
            else -> Logger.debug(message)
        }
    }

    fun <P : IPlayer, C> MessageKey<P, C>.t(player: P): C {
        return LanguageManager.get<P, C>().getMessage(player, this)
    }
}

