package net.rk4z.s1.swiftbase.core

import org.slf4j.Logger

fun String.toBooleanOrNull(): Boolean? {
    return when (this.trim().lowercase()) {
        "true", "1", "t" -> true
        "false", "0", "f" -> false
        else -> null
    }
}

fun Logger.logIfDebug(message: String, level: LogLevel = LogLevel.INFO) {
    if (Core.get().isDebug) {
        when (level) {
            LogLevel.INFO -> info(message)
            LogLevel.WARN -> warn(message)
            LogLevel.ERROR -> error(message)
        }
    } else {
        debug(message)
    }
}

enum class LogLevel {
    INFO, WARN, ERROR
}