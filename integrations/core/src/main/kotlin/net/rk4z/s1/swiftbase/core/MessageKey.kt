package net.rk4z.s1.swiftbase.core

@Suppress("unused", "DEPRECATION")
interface MessageKey<P : IPlayer<C>, C> {
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

    fun t(player: P): C {
        return player.getMessage(this)
    }
}

