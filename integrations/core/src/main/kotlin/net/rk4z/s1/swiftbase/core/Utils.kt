package net.rk4z.s1.swiftbase.core

import org.slf4j.Logger
import java.io.File

fun String.toBooleanOrNull(): Boolean? {
    return when (this.trim().lowercase()) {
        "true", "1", "t" -> true
        "false", "0", "f" -> false
        else -> null
    }
}

fun String.isBlankOrEmpty(): Boolean {
    return this.isBlank() || this.isEmpty()
}

fun File.notExists(): Boolean {
    return !this.exists()
}

fun Logger.logIfDebug(message: String, level: LogLevel = LogLevel.INFO) {
    if (Core.getInstance().isDebug) {
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