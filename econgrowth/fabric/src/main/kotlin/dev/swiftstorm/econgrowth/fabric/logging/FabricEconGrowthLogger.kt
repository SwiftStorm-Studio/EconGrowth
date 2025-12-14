package dev.swiftstorm.econgrowth.fabric.logging

import dev.swiftstorm.econgrowth.core.logging.EconGrowthLogger
import dev.swiftstorm.econgrowth.fabric.EconGrowthFabric.Companion.logger

object FabricEconGrowthLogger : EconGrowthLogger {
    override fun info(message: String) {
        logger.info(message)
    }

    override fun warn(message: String) {
        logger.warn(message)
    }

    override fun error(message: String, throwable: Throwable?) {
        if (throwable != null) {
            logger.error(message, throwable)
        } else {
            logger.error(message)
        }
    }
}